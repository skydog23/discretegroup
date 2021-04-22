/**
 *
 * This package is open source software, made available under a BSD license:
 *
 * Copyright (c) 2009, Charles Gunn
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of jReality nor the names of its contributors nor the
 *   names of their associated organizations may be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */


package de.jtem.discretegroup.core;

import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.pick.Graphics3D;


/**
 * @author Charles Gunn
 *
 */
public class DiscreteGroupViewportConstraint extends DiscreteGroupSimpleConstraint {
	Graphics3D objectToNDCContext;
	double[] objectToNDC;
	double fudge = 1.2, ztlate = .5;
	boolean valid = true;
//	DiscreteGroupSimpleConstraint minimal;
	double[] cameraPosition = new double[4];
	double[][] points, tpoints = new double[1][4];
	double minDistance;
	int minWordLength;
	public DiscreteGroupViewportConstraint(double mind, int minw, double maxd, int maxw, Graphics3D gc) {
		super(maxd, maxw);
		minDistance = mind;
		minWordLength = minw;
		objectToNDCContext = gc;
//		minimal = new DiscreteGroupSimpleConstraint(mind, minw);
		if (objectToNDCContext != null)
			objectToNDC = (objectToNDCContext.getObjectToNDC());
	}

	/* The logic is a bit complicated.  There are basically viewport constraints implemented
	 * in this class, and other, simpler constraints implemented in the super class.
	 * There is first a <i> minimal </i> constraint; if it is satisfied, then this method
	 * returns true.  The idea here, is we don't want to prune the tree before we have the
	 * major branches identified.
	 * Then the superclass acceptElement method is called; if it returns false, we return false.
	 * Finally, we test against the "modelToNDC" transformation deduced from the Graphics3D
	 * supplied with the constructor.
	 * @see discreteGroup.DiscreteGroupConstraint#acceptElement(discreteGroup.DiscreteGroupElement)
	 */
	public boolean acceptElement(DiscreteGroupElement dge) {
		boolean ret = true;
//		boolean minimalVal = minimal.acceptElement(dge);
//		if (minimalVal) return true;
		// check minimal conditions first: if either is satisfied return true
		if (minWordLength > 0 && dge.getWord().length() <= minWordLength) return true;
		double[] mat = dge.getArray();
		if (minDistance > 0) {
			tmp = Rn.matrixTimesVector(null, mat, centerPoint);
			double d = Pn.distanceBetween(tmp, centerPoint, dge.getMetric());
			if (d <= minDistance) return true;
		}

		// this value is determined by a "distance moved" and a word length
		// if it isn't accepted, we don't accept; otherwise we go on to the
		// viewport test
		boolean superVal = super.acceptElement(dge);
		if (!superVal) return false;

		if (objectToNDC == null)	//throw new IllegalStateException("No object to NCD matrix");
			return true;
		// we look at the image of the origin (0,0,0,1)
		//TODO allow other points to be used for this purpose
		mat = Rn.times(null, objectToNDC, dge.getArray());
		if (points == null) {
			Rn.matrixTimesVector(tpoints[0], mat, centerPoint);
		}
		else {
			Rn.matrixTimesVector(tpoints, mat, points);
		}
		for (int i = 0; i<tpoints.length; ++i)	{
			double[] tmp2 = tpoints[i];
			//tmp2[0] = mat[3];  tmp2[1] = mat[7];  tmp2[2] = mat[11];  tmp2[3] = mat[15];
			Pn.dehomogenize(tmp2,tmp2);
//			System.err.println("dge2ndc = "+Rn.matrixToString(mat));
//			System.err.println("c = "+Rn.toString(centerPoint));
//			System.err.println("ndc = "+Rn.toString(tmp2));
			// normalized device coordinates are a cube of side length 2 centered at the origin
			// if any element of the array points is inside the xy slice, return true
			if (Math.abs(tmp2[2]) > 1.0) return false;	
//			System.err.println(fudge+"  clipping "+tmp2[0]+":"+tmp[1]);
			if (Math.abs(tmp2[0]) <= fudge && Math.abs(tmp2[1]) <= fudge) return true;
		}
		return false;
	}

	public double[] getModelToNDC()		{
		return objectToNDCContext.getObjectToNDC();
	}
	
	public void update()	{
		objectToNDC = (objectToNDCContext.getObjectToNDC());
//		System.err.println("dgvc: o2ndc = "+Rn.matrixToString(objectToNDC));
//		double[] cameraToObject = objectToNDCContext.getCameraToObject();
//		Rn.matrixTimesVector(centerPoint, cameraToObject, Pn.originP3 );
	}
	 
	public String toString()	{
		StringBuffer sb = new StringBuffer();
		sb.append(String.format("mind %g\t", getMinDistance()));
		sb.append(String.format("maxd %g\t", getMaxDistance()));
		sb.append(String.format("tlate %g\t", getZtlate()));
		sb.append(String.format("o2ndc = \t"));
		sb.append(Rn.matrixToString(objectToNDC));
		return sb.toString();
	}
	public Graphics3D getGraphicsContext() {
		return objectToNDCContext;
	}
	
	public void setGraphicsContext(Graphics3D gc)	{
		objectToNDCContext = gc;
		update();
	}
	
	public double getFudge() {
		return fudge;
	}

	public void setFudge(double fudge) {
		this.fudge = fudge;
	}

	public double getZtlate() {
		return ztlate;
	}

	public void setZtlate(double ztlate) {
		this.ztlate = ztlate;
	}

	public double[][] getPoints() {
		return points;
	}

	public void setPoints(double[][] points) {
		this.points = points;
		tpoints = new double[points.length][points[0].length];
	}

	public double getMinDistance() {
		return minDistance;
	}

	public int getMinWordLength() {
		return minWordLength;
	}

	public void setMinDistance(double minDistance) {
		this.minDistance = minDistance;
	}

	public void setMinWordLength(int minWordLength) {
		this.minWordLength = minWordLength;
	}
	
	public DiscreteGroupSimpleConstraint getSimpleConstraint()	{
			return new DiscreteGroupSimpleConstraint(getMaxDistance(),
					getMaxWordLength(),
					getMaxNumberElements());

	}
}

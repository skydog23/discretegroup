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

import java.util.List;
import java.util.logging.Level;

import de.jreality.geometry.GeometryUtility;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;
import de.jtem.discretegroup.util.WingedEdge;
import de.jtem.discretegroup.util.WingedEdgeUtility;

/**
 * @author Charles Gunn
 *
 */
public class DiscreteGroupCameraFollower  { 
	SceneGraphPath shipPath, dgPath;
	WingedEdge standardDD;
	SceneGraphComponent shipNode;
	Viewer theViewer;
	SceneGraphComponent rep = new SceneGraphComponent("dd");
	boolean writeImmediately = true;
	double[] newShipMatrix = Rn.identityMatrix(4);
//	DiscreteGroupSceneGraphRepresentation dgsgr = null;
	int metric = Pn.EUCLIDEAN;
	public DiscreteGroupCameraFollower(WingedEdge dd, SceneGraphPath ap, SceneGraphPath dp, DiscreteGroupSceneGraphRepresentation dg) {
		super();
		shipPath = ap;
		dgPath = dp;
		System.err.println("Camera follower path is "+dgPath.toString());
		System.err.println("Ship  path is "+shipPath.toString());
		//theViewer = gc.getViewer();
		standardDD = dd;
		shipNode = shipPath.getLastComponent();
		shipNode.getTransformation().getMatrix(newShipMatrix);
		Object foo = standardDD.getGeometryAttributes(GeometryUtility.METRIC);
		if (foo != null && foo instanceof Integer) {
			metric = ((Integer)foo).intValue();
		}
	}

	boolean moveShip = true;
	public void update()	{
		if (dgPath == null || standardDD == null) {
			throw new IllegalStateException("DiscreteGroupCameraFollower.update(): Invalid values");
		}
		SceneGraphComponent cameraFollower = dgPath.getLastComponent();	
		if (cameraFollower.getTransformation() == null ) {
			throw new IllegalStateException("No transformation");
		}
		double[] shipToWorld = null;
		shipToWorld = shipPath.getMatrix(null);
		double[] dgToWorld = dgPath.getMatrix(null);
		double[] worldToDG = Rn.inverse(null, dgToWorld);
		double[] shipToDG = Rn.times(null, worldToDG, shipToWorld);
		double[] imageOfOrigin = Rn.matrixTimesVector(null, shipToDG, P3.originP3);
		List<WingedEdge.Face> out = WingedEdgeUtility.pointLiesOutsideFace(standardDD, imageOfOrigin, .001);
		shipNode.getTransformation().getMatrix(newShipMatrix);
		if (out.size() > 0 && out.get(0) instanceof WingedEdge.Face)		{
			WingedEdge.Face face = (WingedEdge.Face) out.get(0);
			DiscreteGroupElement t = (DiscreteGroupElement) face.source;
			if (t==null) return;
			if (moveShip)	{
				double[] dgToShip = Rn.inverse(null, shipToDG);
				double[] foo = Rn.conjugateByMatrix(null, Rn.inverse(null,t.getArray()), dgToShip);
				if (writeImmediately) shipNode.getTransformation().multiplyOnRight(foo);
				else {
					Rn.times(newShipMatrix, shipNode.getTransformation().getMatrix(), foo);
				}
//				double[] shipM = shipNode.getTransformation().getMatrix();
//				double det = Rn.determinant(shipM);
//				System.err.println("ship node matrix det = "+det);
				DiscreteGroupUtility.logger.log(Level.INFO,"Lies outside "+out.size()+" half-spaces");
			} else {
				cameraFollower.getTransformation().setReadOnly(false);
				cameraFollower.getTransformation().multiplyOnRight(t.getArray());
//				P3.orthonormalizeMatrix(null, cameraFollower.getTransformation().getMatrix(), 10E-6, metric );
				DiscreteGroupUtility.logger.log(Level.INFO,"Sig is "+metric+"\tLies outside "+out.size()+" half-spaces");
			}
		}
	}

	/**
	 * @param dgPath
	 */
	public void setDiscreteGroupPath(SceneGraphPath dgPath) {
		this.dgPath = dgPath;
	}

	public double[] getNewShipMatrix() {
		return newShipMatrix;
	}

	public void setWriteImmediately(boolean writeImmediately) {
		this.writeImmediately = writeImmediately;
	}

}

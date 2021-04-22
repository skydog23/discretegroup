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


package de.jtem.discretegroup.groups;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupUtility;

public class PointGroup3S2 extends TriangleGroup {
    double[][] origVertices = new double[3][];
    static double[][] texCoord = {{0,1},{0,0},{1,0},{0,0}};
    public PointGroup3S2() {
		super();
		dimension = 3;
		metric = Pn.EUCLIDEAN;
		isFinite = true;
		mirrorGroup = false;
		vertices = new double[3][4];
		for (int i =0;i<3;++i) vertices[i][3] = 1.0;
		reflectionPlanes = new double[3][];
		name = "3*2";
		update();
	}

	public void calculateGenerators() {
		double d = 1.0/ Math.sqrt(3.0);
		vertices[0][0] = vertices[0][1] =  vertices[0][2] = d;
		vertices[1][0] = 1.0;  vertices[1][1] = 0.0; vertices[1][2] = 0.0;
		vertices[2][0] = 0.0;  vertices[2][1] = 1.0; vertices[2][2] = 0.0;
		generators = new DiscreteGroupElement[2];
		// reflection in the z=0 plane
		generators[0] = new DiscreteGroupElement();
		generators[0].setWord(DiscreteGroupUtility.genNames[0]);
		generators[0].setArray(P3.makeReflectionMatrix(null, new double[]{0,0,1,0}, Pn.EUCLIDEAN ));
		generators[1] = new DiscreteGroupElement();
		generators[1].setWord(DiscreteGroupUtility.genNames[1]);
		generators[1].setArray(P3.makeRotationMatrix(null, new double[]{1,1,1}, 4*Math.PI/3.0));

	}

	public TriangleGroup convertToProjective() {
		PointGroup3S2 cp = new PointGroup3S2();
		return _convertToProjective(cp);
	}

//	private double[][] faceColors = {{.9,.2,.1,1},{.4, 1,0,.5}};
;
	double[] tll = {0,0}, tlr = {1,0}, tur = {1,1}, tul = {0,1};
	double[][] textures = {
		tll,	// 0
		tlr,	// 1
		tur,	// 2 first triangle
		tlr,	// 3 
		tur,	// 4
		tul,	// 5
		tur,	// 6
		tul,	// 7
		tll,	// 8
		tll,	// 9
		tlr		// 10
	};
	public Geometry getSplitFundamentalRegion( IndexedFaceSet foo) {
		double[][] quads = null;
		int[][] indices = null, edgeIndices = null;
		double[][] edgeColors = null;
		double[][] textureCoordinates = null;
		if (constrained)	{
			quads = new double[4][4];
			indices = new int[][]{{0,1,2},{0,2,3}};
			edgeIndices= new int[][]{{0,2}}; //,{0,0}};
//			edgeColors = new double[][]{{.9, .2, .1,1}}; //,{.4,.4,1,1}};
			textureCoordinates = texCoord;
		} else {
			quads = new double[11][4];
			indices = new int[3][];
			indices[0] = new int[3];
			indices[1] = new int[4];
			indices[2] = new int[4];
			quads[7] = quads[0];
			quads[8] = quads[2];
			quads[9] = quads[0];
			quads[10] = quads[4];
		}
		boolean flipped = false;
		double[] centerPoint3 = new double[4];
		if (metric == Pn.ELLIPTIC && dimension == 2)	{
			centerPoint3 = Rn.matrixTimesVector(null, swapZW, centerPoint);
		} else  {
			System.arraycopy(centerPoint, 0, centerPoint3, 0, 4);
		}
		// centerpoint3 is now a point in euclidean 3-space
//		if (metric == Pn.EUCLIDEAN && dimension == 3)	{
//			Pn.setToLength(centerPoint, centerPoint, 1.0, Pn.EUCLIDEAN);
//			setCenterPoint(centerPoint);
//		}
		Pn.dehomogenize(centerPoint3, centerPoint3);
		System.err.println("center point is "+Rn.toString(centerPoint3));

		for (int i = 0; i<3; ++i) 
			if (centerPoint3[i] < 0) {
				centerPoint3[i] *= -1;
				flipped = !flipped;
			}
//		System.err.println("CP: "+Rn.toString(centerPoint3));
		double x = centerPoint3[0]; 
		double y = centerPoint3[1];
		double z = centerPoint3[2];
		if (constrained)	{
			Pn.setToLength(centerPoint3, centerPoint3, 1.0, Pn.EUCLIDEAN);
			// bring the center point into the 45-120-45 triangle bounded by arc between (1,0,0,1) and (0,1,0,1)
			if (x > y && z >= y) {centerPoint3[0] = z; centerPoint3[1] = x; centerPoint3[2]=y;}
			if (z > x && y >= x) {centerPoint3[0] = y; centerPoint3[1] = z; centerPoint3[2]=x;}			
		} else {
			// bring the center point into a kite with one corner at (1,1,1) and the other at (0,1,0)
			if (x > y && x > z) {centerPoint3[0] = z; centerPoint3[1] = x; centerPoint3[2]=y;}
			if (z > x && z > y) {centerPoint3[0] = y; centerPoint3[1] = z; centerPoint3[2]=x;}
		}

		// allow point to move only along edges of octahedron
		if (constrained)  {
			x = centerPoint3[0]; 
			y = centerPoint3[1];
			centerPoint3[2] = 0.0;
			double d = Math.sqrt(2/(2-2*x*y));

//			System.err.println("Jitterbug factor: "+d+"CP: "+Rn.toString(centerPoint3));
			for (int i =0;i<3; ++i) centerPoint3[i] *= d;
			setCenterPoint(centerPoint3);
		}
		// put it back in the proper coordinate system
		if (metric == Pn.ELLIPTIC && dimension == 2)	{
			centerPoint3 = Rn.matrixTimesVector(null, swapZW, centerPoint3);
		} 
		System.arraycopy(centerPoint3, 0, quads[0], 0, 4);
//		setCenterPoint(quads[0]);
		System.arraycopy(vertices[0], 0, quads[1], 0, 3);
		quads[1][3] = 1.0;
		// rotation of -120 degrees around the (1,1,1) axis.
		Rn.matrixTimesVector(quads[2],generators[1].getArray(), quads[0]);
		Pn.dehomogenize(quads[2], quads[2]);
		if (dimension == 3 && metric == Pn.EUCLIDEAN)	{
			double[] p3 = Rn.matrixTimesVector(null, generators[1].getArray(), quads[2]);
			double[] plane =  P3.planeFromPoints(null, quads[0], quads[2], p3);
			P3.lineIntersectPlane(quads[1], P3.originP3, quads[1], plane );			
		}
		
		if (constrained)	{
			quads[3][1] = quads[3][2] = 0.0; 
			quads[3][0] = quads[2][0]; 
			quads[3][3] = 1.0;
			if (Math.abs(x-y) < 10E-4) {
				//edgeIndices[1][0] = 0; edgeIndices[1][1] = 2; 
			}
			else if (x < y) { 			
				quads[3][0] = quads[2][0]; 
				//edgeIndices[1][0] = 2; edgeIndices[1][1] = 3; 
			}
			else if (x > y) { 
				quads[3][0] = quads[0][0]; 
				//edgeIndices[1][0] = 0; edgeIndices[1][1] = 3; 
			}
		}
		else	{
			indices[0][0] = 0; 
			indices[0][1] = 1;  
			indices[0][2] = 2;
			// reflection in the z= 0 plane
			Rn.matrixTimesVector(quads[4], generators[0].getArray(), quads[0]);
			Pn.linearInterpolation(quads[4], quads[0], quads[4], .5, Pn.EUCLIDEAN);
			Pn.dehomogenize(quads[4], quads[4]);
			
			Rn.matrixTimesVector(quads[3], generators[0].getArray(), quads[2]);
			Pn.linearInterpolation(quads[3], quads[2], quads[3], .5, Pn.EUCLIDEAN);
			Pn.dehomogenize(quads[3], quads[3]);
			indices[1][0] = 7; 
			indices[1][1] = flipped?8:4;  
			indices[1][2] = 3;  
			indices[1][3] = flipped? 4:8;
			
			double[] reflection = P3.makeReflectionMatrix(null, new double[]{1,0,0,0}, Pn.EUCLIDEAN );
			Rn.matrixTimesVector(quads[5], reflection, quads[0]);
			Pn.linearInterpolation(quads[5], quads[0], quads[5], .5, Pn.EUCLIDEAN);
			Pn.dehomogenize(quads[5], quads[5]);
			
			Rn.matrixTimesVector(quads[6], reflection, quads[4]);
			Pn.linearInterpolation(quads[6], quads[4], quads[6], .5, Pn.EUCLIDEAN);
			Pn.dehomogenize(quads[6], quads[6]);
			indices[2][0] = 9; 
			indices[2][1] = flipped ? 10 : 5;  
			indices[2][2] = 6;  
			indices[2][3] = flipped ? 5:10;
			//for (int i = 0; i<7; ++i)	System.err.println(Rn.toString(quads[i]));			
		}
		if (metric == Pn.ELLIPTIC && dimension == 2)	{
			System.err.println("Verts = "+Rn.toString(quads));
//			for (int i = 0; i<7; ++i) Rn.matrixTimesVector(quads[i], swapZW, quads[i]);
		}
		foo = null;
		if ( foo == null)	{
			IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
			ifsf.setMetric(metric);
			ifsf.setVertexCount(quads.length);
			ifsf.setFaceCount(indices.length);
			ifsf.setVertexCoordinates(quads);
			ifsf.setFaceIndices(indices);
			if (constrained)	{
				ifsf.setGenerateEdgesFromFaces(false);
				ifsf.setEdgeCount(edgeIndices.length);
				ifsf.setEdgeIndices(edgeIndices);				
			} else	{
				ifsf.setGenerateEdgesFromFaces(true);
				if (textures != null) ifsf.setVertexTextureCoordinates(textures);
			}
			ifsf.setGenerateFaceNormals(true);
			if (edgeColors!= null) ifsf.setEdgeColors(edgeColors);
			if (faceColors!= null && faceColors.length == indices.length) 
				ifsf.setFaceColors(faceColors);
			ifsf.update();
			foo = ifsf.getIndexedFaceSet(); 			
		} else {
//			if (constrained && edgeIndices != null) 
//				foo.setEdgeCountAndAttributes(Attribute.INDICES, StorageModel.INT_ARRAY.array().createReadOnly(edgeIndices));
			if (faceColors!= null && faceColors.length == foo.getNumFaces()) 
				foo.setFaceAttributes(Attribute.COLORS, StorageModel.DOUBLE_ARRAY.array(4).createReadOnly(faceColors));
			foo.setVertexAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(4).createReadOnly(quads));
			IndexedFaceSetUtility.calculateAndSetFaceNormals(foo);
		}
		return foo;
	}
	
	public static  Geometry getSplitFundamentalRegion(PointGroup3S2 tg, IndexedFaceSet foo)	{
		return tg.getSplitFundamentalRegion(foo);
	}
	
	public void setFaceColors(double[][] ec)	{
		faceColors = ec;
	}
}

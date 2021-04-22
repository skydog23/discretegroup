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

import java.awt.Color;
import java.util.Hashtable;

import de.jreality.geometry.BallAndStickFactory;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.TubeUtility;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.ResourceClass;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupUtility;
import de.jtem.discretegroup.core.FiniteStateAutomaton;


/**
 * A class for describing the discrete groups associated to the 10 Euclidean 3-manifolds
 * ("platycosms" according to John Conway).
 * 
 * @author Charles Gunn
 *
  */
public class Platycosm extends EuclideanGroup {
	
	public Platycosm() {
		super();
		dimension = 3;
	}
	
	public static String[] names = {"c1","c2","c3","c4","c6","c22","+a1","-a1","+a2","-a2"};
	public static final int C1 = 0;
	public static final int C2 = 1;
	public static final int C3 = 2;
	public static final int C4 = 3;
	public static final int C6 = 4;
	public static final int C22 = 5;
	public static final int PA1 = 6;
	public static final int MA1 = 7;
	public static final int PA2 = 8;
	public static final int MA2 = 9;
	public static int numGenerators = 0;
	
	static Hashtable<String, Integer> nameTable = new Hashtable<String, Integer>();
	
	static {
		for (int i =0; i<10; ++i)	{
			nameTable.put(names[i], new Integer(i));
			}
	}

	public static Platycosm instanceOfGroup(String name) {	
		int which = nameTable.get(name);
		return instanceOfGroup(which);
	}
	
	static double[] standardTlate = {2,0,0}, st01 = {2,0,2}, st02 = {2,0,-2};
	static double[] xGlidePlane = {1,0,0,1}, yGlidePlane = {0,1,0,-1}, yGlideVector = {0,-2,0};
	
	static Matrix st = new Matrix(), st1 = new Matrix(), st2 = new Matrix();
	static Matrix gr = new Matrix(), gr1 = new Matrix(), gr2 = new Matrix();
	
	static {
		MatrixBuilder.euclidean().translate(standardTlate).assignTo(st);
		MatrixBuilder.euclidean().translate(st01).assignTo(st1);
		MatrixBuilder.euclidean().translate(st02).assignTo(st2);
		MatrixBuilder.euclidean().reflect(xGlidePlane).translate(yGlideVector).assignTo(gr);		
		MatrixBuilder.euclidean().reflect(yGlidePlane).translate(st01).assignTo(gr1);		
		MatrixBuilder.euclidean().reflect(yGlidePlane).translate(st02).assignTo(gr2);		
	}
	
	public String[] getNames() {
		return names;
	}
	
	public static Platycosm instanceOfGroup(int which)	{
		Platycosm theGroup = null;
		DiscreteGroupElement[] generators = null;
//		DiscreteGroupColorPicker colorPicker = null;
//		int allowedChangeOfBasis = 0;
		switch (which)	{
			case C1:
//				allowedChangeOfBasis = COB_ALL;
				numGenerators = 3;
				generators = new DiscreteGroupElement[2*numGenerators];
				for (int i = 0; i<numGenerators; ++i)	{
					generators[i] = new DiscreteGroupElement();
					generators[i].setWord(DiscreteGroupUtility.genNames[i]);
				}
				MatrixBuilder.euclidean().translate(2,0,0).assignTo(generators[0].getArray());
				MatrixBuilder.euclidean().translate(0,2,0).assignTo(generators[1].getArray());
				MatrixBuilder.euclidean().translate(0,0,2).assignTo(generators[2].getArray());
				for (int i = 0; i<numGenerators; ++i) 
					generators[i+numGenerators] = (DiscreteGroupElement) generators[i].getInverse();
				
			break;
				
			case C2:
//				allowedChangeOfBasis = COB_ALL;
				numGenerators = 3;
				generators = new DiscreteGroupElement[2*numGenerators];
				for (int i = 0; i<numGenerators; ++i)	{
					generators[i] = new DiscreteGroupElement();
					generators[i].setWord(DiscreteGroupUtility.genNames[i]);
				}
				MatrixBuilder.euclidean().translate(2,0,0).assignTo(generators[0].getArray());
				MatrixBuilder.euclidean().translate(0,2,0).assignTo(generators[1].getArray());
				generators[2].setArray(screwMotion(new double[]{0,0,0}, new double[]{0,0,2}, Math.PI));
				for (int i = 0; i<numGenerators; ++i) 
					generators[i+numGenerators] = (DiscreteGroupElement) generators[i].getInverse();
				
			break;
				
			case C3:
//				allowedChangeOfBasis = COB_ALL;
				numGenerators = 3;
				generators = new DiscreteGroupElement[2*numGenerators];
				for (int i = 0; i<numGenerators; ++i)	{
					generators[i] = new DiscreteGroupElement();
					generators[i].setWord(DiscreteGroupUtility.genNames[i]);
				}
				MatrixBuilder.euclidean().translate(2,0,0).assignTo(generators[0].getArray());
				MatrixBuilder.euclidean().translate(2*Math.cos(Math.PI/3),2*Math.sin(Math.PI/3),0).assignTo(generators[1].getArray());
				generators[2].setArray(screwMotion(new double[]{0,0,0}, new double[]{0,0,2}, 2*Math.PI/3));
				for (int i = 0; i<numGenerators; ++i) 
					generators[i+numGenerators] = (DiscreteGroupElement) generators[i].getInverse();
				
			break;
				
			case C4:
//				allowedChangeOfBasis = COB_ALL;
				numGenerators = 3;
				generators = new DiscreteGroupElement[2*numGenerators];
				for (int i = 0; i<numGenerators; ++i)	{
					generators[i] = new DiscreteGroupElement();
					generators[i].setWord(DiscreteGroupUtility.genNames[i]);
				}
				MatrixBuilder.euclidean().translate(2,0,0).assignTo(generators[0].getArray());
				MatrixBuilder.euclidean().translate(2*Math.cos(Math.PI/2),2*Math.sin(Math.PI/2),0).assignTo(generators[1].getArray());
				generators[2].setArray(screwMotion(new double[]{0,0,0}, new double[]{0,0,2}, Math.PI/2));
				for (int i = 0; i<numGenerators; ++i) 
					generators[i+numGenerators] = (DiscreteGroupElement) generators[i].getInverse();
				
			break;
				
			case C6:
//				allowedChangeOfBasis = COB_ALL;
				numGenerators = 3;
				generators = new DiscreteGroupElement[2*numGenerators];
				for (int i = 0; i<numGenerators; ++i)	{
					generators[i] = new DiscreteGroupElement();
					generators[i].setWord(DiscreteGroupUtility.genNames[i]);
				}
				MatrixBuilder.euclidean().translate(2,0,0).assignTo(generators[0].getArray());
				MatrixBuilder.euclidean().translate(2*Math.cos(Math.PI/3),2*Math.sin(Math.PI/3),0).assignTo(generators[1].getArray());
				generators[2].setArray(screwMotion(new double[]{0,0,0}, new double[]{0,0,1}, Math.PI/3));
				for (int i = 0; i<numGenerators; ++i) 
					generators[i+numGenerators] = (DiscreteGroupElement) generators[i].getInverse();
				
			break;
			
			case C22:
				numGenerators = 12;
				generators = new DiscreteGroupElement[12];				
				double[][] points = {
						{1,1,0},{1,-1,0},
						{-1,1,0},{-1,-1,0}
				};
				double[] rotator = P3.makeRotationMatrix(null, new double[]{1,1,1}, 2*Math.PI/3.0);
				double[][][] pointsAll = new double[3][][];
				pointsAll[0] = points;
				pointsAll[1] = Rn.matrixTimesVector(null, rotator, points);
				pointsAll[2] = Rn.matrixTimesVector(null, rotator, pointsAll[1]);
				for (int i = 0; i< 3; ++i)	{
					for (int j = 0; j< 2; ++j)	{
						generators[2*i+j] = new DiscreteGroupElement();
						generators[2*i+j].setArray(P3.makeScrewMotionMatrix(null, 
								pointsAll[i][2*j],
								pointsAll[i][2*j+1], Math.PI, Pn.EUCLIDEAN));
						generators[2*i+j].setWord(DiscreteGroupUtility.genNames[2*i+j]);
					}
				}
				for (int i = 0; i< 6; ++i)	generators[i+6] = (DiscreteGroupElement) generators[i].getInverse();
				break;
				
			
			case PA1:
//				allowedChangeOfBasis = COB_ALL;
				numGenerators = 3;
				generators = new DiscreteGroupElement[2*numGenerators];
				for (int i = 0; i<numGenerators; ++i)	{
					generators[i] = new DiscreteGroupElement();
					generators[i].setWord(DiscreteGroupUtility.genNames[i]);
				}
				st.assignTo(generators[0].getArray());
				gr.assignTo(generators[1].getArray());
				MatrixBuilder.euclidean().translate(new double[]{0,0,2}).assignTo(generators[2].getArray());
				//generators[2].setMatrix(glideReflection(new double[]{1,0,0,1}, new double[]{-1,0,0}, new double[]{-1,0,2}));	// W-Transformation
				for (int i = 0; i<numGenerators; ++i) 
					generators[i+numGenerators] = (DiscreteGroupElement) generators[i].getInverse();
				
			break;
			
			case MA1:
//				allowedChangeOfBasis = COB_ALL;
				numGenerators =  3;
				generators = new DiscreteGroupElement[2*numGenerators];
				for (int i = 0; i<numGenerators; ++i)	{
					generators[i] = new DiscreteGroupElement();
					generators[i].setWord(DiscreteGroupUtility.genNames[i]);
				}
				st1.assignTo(generators[0].getArray());
				st2.assignTo(generators[1].getArray());
				gr.assignTo(generators[2].getArray());
				for (int i = 0; i<numGenerators; ++i) 
					generators[i+numGenerators] = (DiscreteGroupElement) generators[i].getInverse();
				
			break;
			
			case PA2:
//				allowedChangeOfBasis = COB_ALL;
				numGenerators = 3;
				generators = new DiscreteGroupElement[2*numGenerators];
				for (int i = 0; i<numGenerators; ++i)	{
					generators[i] = new DiscreteGroupElement();
					generators[i].setWord(DiscreteGroupUtility.genNames[i]);
				}
				MatrixBuilder.euclidean().translate(new double[]{4,0,0}).assignTo(generators[0].getArray());
				gr.assignTo(generators[1].getArray());
				generators[2].setArray(P3.makeScrewMotionMatrix(null, 
						new double[]{0,0,0}, new double[]{0,0,2}, Math.PI, Pn.EUCLIDEAN));
				for (int i = 0; i<numGenerators; ++i) 
					generators[i+numGenerators] = (DiscreteGroupElement) generators[i].getInverse();
				
			break;
			
			case MA2:
//				allowedChangeOfBasis = COB_ALL;
				numGenerators = 3;
				generators = new DiscreteGroupElement[2*numGenerators];
				for (int i = 0; i<numGenerators; ++i)	{
					generators[i] = new DiscreteGroupElement();
					generators[i].setWord(DiscreteGroupUtility.genNames[i]);
				}
				st.assignTo(generators[0].getArray());
				gr.assignTo(generators[1].getArray());
				generators[2].setArray(P3.makeScrewMotionMatrix(null, 
						new double[]{-1,0,0}, new double[]{-1,0,2}, Math.PI, Pn.EUCLIDEAN));
				for (int i = 0; i<numGenerators; ++i) 
					generators[i+numGenerators] = (DiscreteGroupElement) generators[i].getInverse();
				
			break;
			
			default:
				System.err.println("Not yet implemented "+names[which]);
		}
//		for (int i = 0; i<generators.length; ++i)	{
//			System.err.println("Generator "+i+" is \n"+Rn.matrixToString(generators[i].getArray()));
//		}
		theGroup = new Platycosm();
		theGroup.setGenerators(generators);
		FiniteStateAutomaton fsa = FiniteStateAutomaton.fsaForName(names[which]+".wa", ResourceClass.class);
		if (fsa != null) {
			theGroup.setFsa(fsa);
//			fsa.debugPrint();
		} 
		theGroup.setName(names[which]);
		return theGroup;
	}
	

	// A copy of the method for the 2D groups: needs to be rewritten for the 3D case
	public void setChangeOfBasis(double a, double sc, double x, double y)	{
		double[] cob = Rn.identityMatrix(4);
		double angle, scale, ynewx, ynewy;
		if ((getAllowedChangeOfBasis() & COB_ROTATE) != 0) angle = a;
		else angle = 0.0;
		if ((getAllowedChangeOfBasis() & COB_XSCALE) != 0)	{
			ynewx  = x; 
		} else {
			ynewx = 0.0;
		}
		if ((getAllowedChangeOfBasis() & COB_YSCALE) != 0)	{
			ynewy = y;
		} else {
			ynewy = 1.0;
		}
		if ((getAllowedChangeOfBasis() & COB_SCALE) != 0)	scale = sc;
		else											scale = 1.0;	
		double c, s;
		c = scale * Math.cos(angle);
		s = scale * Math.sin(angle);
		cob[0] = c;
		cob[4] = s;
		cob[1] = ynewx * c  - ynewy * s;
		cob[5] = ynewx * s  + ynewy * c;
		setChangeOfBasis(cob);
	}
	
	// function for creating a screw motion through 'from' and 'to' in direction 'to', 
	// rotation by the angle phi
	static public double[] screwMotion(double[] from, double[] to, double phi) {
		
		return Rn.times(null,	P3.makeRotationMatrix(null, from, to, phi, Pn.EUCLIDEAN), 
								P3.makeTranslationMatrix(null, from, to, Pn.EUCLIDEAN));
	}
	
	// function for creating a glide reflection through 'from' and 'to' in direction 'to', 
	static public double[] glideReflection(double[] plane, double[] from, double[] to) {
		return Rn.times(null,	P3.makeReflectionMatrix(null, plane, Pn.EUCLIDEAN),
								P3.makeTranslationMatrix(null, from, to, Pn.EUCLIDEAN));
	}
	
	private static final Color glideReflectionArrowColor = new Color(0,255,0);
	private static final Color screwMotionArrowColor = new Color(0,155,255);
	private static final Color translationArrowColor  = new Color(255,255,0);
	static public SceneGraphComponent getGeneratorsAsSGC(SceneGraphComponent generators, String name, double thickness) {
		
		double points[][] = null;
		
		if (generators == null)
			generators = new SceneGraphComponent("generators");
		if (nameTable.get(name) == null) return generators;
		int which = nameTable.get(name);
		switch (which) {
		case C1: 
			points = new double[2][3];

			for (int i = 0; i < numGenerators; ++i) {
				points[0] = new double[]{0,0,0};
				points[1] = new double[]{i==0?2:0,i==1?2:0,i==2?2:0};

				makeTranslationGenerator(points, generators, thickness, translationArrowColor);
			}
			
		break;
		
		case C2:
			makeTranslationGenerator(new double[][]{{0,0,0}, {2,0,0}}, generators, thickness, translationArrowColor);
			makeTranslationGenerator(new double[][]{{0,0,0}, {0,2,0}}, generators, thickness, translationArrowColor);
			makeScrewGenerator(new double[][]{{0,0,0}, {0,0,2}}, Math.PI, generators, thickness);
		break;
		
		case C3:
			makeTranslationGenerator(new double[][]{{0,0,0}, {2,0,0}}, generators, thickness, translationArrowColor);
			makeTranslationGenerator(new double[][]{{0,0,0}, {2*Math.cos(Math.PI/3),2*Math.sin(Math.PI/3),0}}, generators, thickness, translationArrowColor);
			makeScrewGenerator(new double[][]{{0,0,0}, {0,0,2}}, 2*Math.PI/3, generators, thickness);
		break;
		
		case C4:
			makeTranslationGenerator(new double[][]{{0,0,0}, {2,0,0}}, generators, thickness, translationArrowColor);
			makeTranslationGenerator(new double[][]{{0,0,0}, {2*Math.cos(Math.PI/2),2*Math.sin(Math.PI/2),0}}, generators, thickness, translationArrowColor);
			makeScrewGenerator(new double[][]{{0,0,0}, {0,0,2}}, Math.PI/2, generators, thickness);
		break;
		
		case C6:
			makeTranslationGenerator(new double[][]{{0,0,0}, {2,0,0}}, generators, thickness, translationArrowColor);
			makeTranslationGenerator(new double[][]{{0,0,0}, {2*Math.cos(Math.PI/3),2*Math.sin(Math.PI/3),0}}, generators, thickness, translationArrowColor);
			makeScrewGenerator(new double[][]{{0,0,0}, {0,0,2}}, Math.PI/3, generators, thickness);
		break;
		
		case C22:
			
			points = new double[][]{
					{1,1,0},{1,-1,0},
					{-1,1,0},{-1,-1,0}
			};
			double[] rotator = P3.makeRotationMatrix(null, new double[]{1,1,1}, 2*Math.PI/3.0);
			double[][][] pointsAll = new double[3][][];
			pointsAll[0] = points;
			pointsAll[1] = Rn.matrixTimesVector(null, rotator, points);
			pointsAll[2] = Rn.matrixTimesVector(null, rotator, pointsAll[1]);
			for (int i = 0; i< 3; ++i)	{
				for (int j = 0; j< 2; ++j)	{
					makeScrewGenerator(new double[][]{pointsAll[i][2*j], pointsAll[i][2*j+1]}, Math.PI, generators, thickness);
				}
			}
		break;
	
		case PA1:
			makeTranslationGenerator(new double[][]{{0,0,0}, {0,0,2}}, generators, thickness, translationArrowColor);
			makeGlideReflectionGenerator(xGlidePlane, new double[][]{{0,0,0}, yGlideVector}, generators, thickness);
			makeTranslationGenerator(new double[][]{{0,0,0}, standardTlate}, generators, thickness, translationArrowColor);
		break;

		case MA1:
			makeTranslationGenerator(new double[][]{{0,0,0}, st01}, generators, thickness, translationArrowColor);
			makeTranslationGenerator(new double[][]{{0,0,0}, st02}, generators, thickness, translationArrowColor);
			makeGlideReflectionGenerator(xGlidePlane, new double[][]{{0,0,0}, yGlideVector}, generators, thickness);
		break;
		
		case PA2:
			makeTranslationGenerator(new double[][]{{0,0,0}, {4,0,0}}, generators, thickness, translationArrowColor);
			makeGlideReflectionGenerator(xGlidePlane, new double[][]{{0,0,0}, yGlideVector}, generators, thickness);
			makeScrewGenerator(new double[][]{{0,0,0}, {0,0,2}}, Math.PI, generators, thickness);
		break;
		
		case MA2:
			makeTranslationGenerator(new double[][]{{0,0,0}, standardTlate}, generators, thickness, translationArrowColor);
			makeGlideReflectionGenerator(xGlidePlane, new double[][]{{0,0,0}, yGlideVector}, generators, thickness);
			makeScrewGenerator(new double[][]{{-1,0,0}, {-1,0,2}}, Math.PI, generators, thickness);
		break;
		
			default:
				System.err.println(which + " Is not an implemented Group");
		}
		
		return generators;
	}
	
	static public void makeTranslationGenerator(double [][]vector, SceneGraphComponent sgc, double thickness, Color c) {
		
		IndexedLineSetFactory ilsf = new IndexedLineSetFactory();
		BallAndStickFactory basf = null;
		int indices[][] = new int[1][2];
		
		indices[0][0] = 0;
		indices[0][1] = 1;
		
		ilsf.setEdgeCount(1);
		ilsf.setVertexCount(2);
		ilsf.setEdgeIndices(indices);
		ilsf.setVertexCoordinates(vector);
		ilsf.update();
		basf = new BallAndStickFactory(ilsf.getIndexedLineSet());
		basf.setShowArrows(true);
		basf.setShowBalls(false);
		basf.setShowSticks(true);
		basf.setStickColor(c);
		basf.setStickRadius(thickness);
		basf.setArrowScale(2*thickness);
		basf.setArrowPosition(1.00+thickness);
		basf.update();
		sgc.addChild(basf.getSceneGraphComponent());
	}
	
	static public void makeScrewGenerator(double [][]vector, double phi, SceneGraphComponent sgc, double thickness) {
		SceneGraphComponent container = SceneGraphUtility.createFullSceneGraphComponent("screw motion 1");
		TubeUtility.tubeOneEdge(container, vector[0], vector[1], .2, null, Pn.EUCLIDEAN);
		SceneGraphComponent spiralSGC = SceneGraphUtility.createFullSceneGraphComponent("screw motion  2");
		container.setGeometry(null);
		spiralSGC.setGeometry( getUrSpiral(phi));
		container.addChild(spiralSGC);
		makeTranslationGenerator(vector, sgc, thickness, screwMotionArrowColor);
		sgc.addChild(container);
	}
	
	static public void makeGlideReflectionGenerator(double[] plane, double[][] tlate, SceneGraphComponent sgc, double thickness) {
		
		SceneGraphComponent faceSGC = new SceneGraphComponent("face");
		
		Appearance ap = new Appearance();
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
		ap.setAttribute(CommonAttributes.OPAQUE_TUBES_AND_SPHERES, true);
		ap.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,Color.white);
		ap.setAttribute(CommonAttributes.FACE_DRAW, true);
		
		faceSGC.setAppearance(ap);
		
//		LineUtility.sceneGraphForPlane(faceSGC, plane, tlate[0], 1.0);
		makeTranslationGenerator(tlate, sgc, thickness, glideReflectionArrowColor);
		sgc.addChild(faceSGC);
	}
	
	static public int getIndex(String string) {
		int which = nameTable.get(string);
		return which;
	}
	private static int numSteps = 20;
	static public IndexedFaceSet getUrSpiral(double angle)	{
		double[][] verts = new double[numSteps*2][3];
		int[][] indices = new int[numSteps-1][4];
		for (int i = 0; i<numSteps; ++i)	{
			double dt = i/(numSteps-1.0);
			double dangle = angle * dt;
			verts[i][0] = Math.cos(dangle);
			verts[i][1] = Math.sin(dangle);
			verts[i][2] = verts[i+numSteps][2] = -.5+dt;
			for (int j = 0; j<2; ++j) verts[i+numSteps][j] = -verts[i][j];
			if (i<numSteps-1) {
				indices[i][0] = i;
				indices[i][1] = i+1;
				indices[i][2] = i+numSteps+1;
				indices[i][3] = i+numSteps;
			}
		}
		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		ifsf.setVertexCount(2*numSteps);
		ifsf.setVertexCoordinates(verts);
		ifsf.setFaceCount(numSteps-1);
		ifsf.setFaceIndices(indices);
		ifsf.setGenerateFaceNormals(true);
		ifsf.setGenerateVertexNormals(true);
		ifsf.update();
		return ifsf.getIndexedFaceSet();
	}
	
}

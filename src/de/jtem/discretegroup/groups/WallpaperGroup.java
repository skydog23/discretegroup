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

import java.util.HashMap;
import java.util.Hashtable;
import java.util.logging.Level;

import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.Scene;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupColorPicker;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.discretegroup.core.DiscreteGroupUtility;
import de.jtem.discretegroup.core.FiniteStateAutomaton;




/**
 * @author gunn
 *
 */
public class WallpaperGroup extends EuclideanGroup {

	public static String[] oldNames = {"P","P2",  "P3", "P4", "P6", "PG","PM","PMM",  "PMG","PGG","CM","CMM", "P31M","P3M1","P4G","P4M", "P6M"};
	public static String[] names = {   "O","2222","333","244","236","XX","**","*2222","22*","22X","*X","2*22","3*3", "*333","4*2","*244","*236", "c12", "d24"};
	public static final int P = 0;
	public static final int P2 = 1;
	public static final int _P3 = 2;	// name conflict with de.jreality.util.P3!
	public static final int P4 = 3;
	public static final int P6 = 4;
	public static final int PG = 5;
	public static final int PM = 6;
	public static final int PMM = 7;
	public static final int PMG = 8;
	public static final int PGG = 9;
	public static final int CM = 10;
	public static final int CMM = 11;
	public static final int P31M = 12;
	public static final int P3M1 = 13;
	public static final int P4G = 14;
	public static final int P4M = 15;
	public static final int P6M = 16;
	// same with conway names. using 'S' to represent '*', which is not allowed in Java names
	public static final int _O = 0;
	public static final int _2222= 1;
	public static final int _333 = 2;	// name conflict with de.jreality.util.P3!
	public static final int _244 = 3;
	public static final int _236 = 4;
	public static final int _XX = 5;
	public static final int _SS = 6;
	public static final int _S2222 = 7;
	public static final int _22S = 8;
	public static final int _22X = 9;
	public static final int _SX = 10;
	public static final int _2S22 = 11;
	public static final int _3S3 = 12;
	public static final int _S333 = 13;
	public static final int _4S2 = 14;
	public static final int _S244 = 15;
	public static final int _S236 = 16;
	public static final int C12 = 17;
	public static final int D24 = 18;
	
	public static Hashtable<String,Integer> nameTable = new Hashtable<String,Integer>();
	static {
		for (int i =0; i<names.length; ++i)	{
			nameTable.put(names[i], new Integer(i));
			}
	}

		
	double[] changeOfBasisParameters  = {0,1,0,1};
	
	public WallpaperGroup() {			
		super();
		dimension = 2;
	}

	public static WallpaperGroup instanceOfGroup(String name) {			
		Integer lookup = (Integer) nameTable.get(name);
		if (lookup == null) return null;
		return instanceOfGroup(lookup.intValue());
	}
	
	public static SceneGraphComponent representationForRotation(double scale, int order, double x, double y)	{
		SceneGraphComponent sgc = SceneGraphUtility.createFullSceneGraphComponent();
		MatrixBuilder.euclidean().translate(x,y,0).scale(scale).assignTo(sgc);
		if (order == 2) sgc.setGeometry(almond());
		else if (order > 2)	{
			sgc.setGeometry(Primitives.regularPolygon(order));
		} else {
			DiscreteGroupUtility.logger.log(Level.WARNING,"Bad order "+order);
			return null;
		}
		return sgc;
	}
	
	public static SceneGraphComponent representationForTranslation(double scale, double x0, double y0, double x1, double y1)	{
		SceneGraphComponent sgc = SceneGraphUtility.createFullSceneGraphComponent();
		sgc.getAppearance().setAttribute(CommonAttributes.DIFFUSE_COLOR, java.awt.Color.BLACK);
		sgc.getAppearance().setAttribute(CommonAttributes.FACE_DRAW, false);
		sgc.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, true);
		
		MatrixBuilder.euclidean().translate(x0,y0,0).assignTo(sgc);
		sgc.setGeometry(Primitives.arrow(0.0, 0.0, x1-x0, y1-y0, scale));
		return sgc;
	}
	
	public static SceneGraphComponent representationForReflection(double scale, double x0, double y0, double x1, double y1)	{
		SceneGraphComponent sgc = SceneGraphUtility.createFullSceneGraphComponent();
		sgc.getAppearance().setAttribute(CommonAttributes.DIFFUSE_COLOR, java.awt.Color.BLACK);
		sgc.getAppearance().setAttribute(CommonAttributes.FACE_DRAW, false);
		sgc.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, true);
		
		double[][] verts = new double[2][];
		double[] p0 = {x0, y0, 0.0};
		double[] p1 = {x1, y1, 0.0};
		double[] dd = Rn.subtract(null, p1, p0);
		verts[0] = Rn.linearCombination(null, 1.0, p0, -(scale - 1.0)*.5, dd);
		verts[1] = Rn.linearCombination(null, 1.0, p1, (scale - 1.0)*.5, dd);
		sgc.setGeometry(IndexedLineSetUtility.createCurveFromPoints(verts, false));
		return sgc;
	}
	
	public static SceneGraphComponent representationForGlideReflection(double scale, double x0, double y0, double x1, double y1)	{
		SceneGraphComponent sgc = SceneGraphUtility.createFullSceneGraphComponent();
		sgc.getAppearance().setAttribute(CommonAttributes.DIFFUSE_COLOR, java.awt.Color.BLACK);
		sgc.getAppearance().setAttribute(CommonAttributes.FACE_DRAW, false);
		sgc.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, true);
		
		MatrixBuilder.euclidean().translate(x0,y0,0).assignTo(sgc);
//	sgc.getTransformation().setTranslation(x0, y0, 0.0);
		sgc.setGeometry(Primitives.arrow(0.0, 0.0, x1-x0, y1-y0,scale, true));
		return sgc;
	}
	
	private static IndexedFaceSet almond()	{
		double[][] verts = new double[40][3];
		for (int i = 0; i<20; ++i)	{
			double angle = ((i-9.5)/19.0 )*Math.PI/2.0;
			verts[i+20][0] = -(verts[i][0] = Math.cos(angle)-Math.sqrt(.5) );
			verts[i+20][1] = -(verts[i][1] = Math.sin(angle));
			verts[i+20][2] = verts[i][2] = 0.0;
		}
		return IndexedFaceSetUtility.constructPolygon(verts);
	}
	static double globalScale = 0.1,
		globalReflectionScale = 1.5;
	
	public static WallpaperGroup instanceOfGroup(int num)	{
		WallpaperGroup theGroup = null;
		DiscreteGroupElement[] generators = null;
		SceneGraphComponent[] generatorRepresentations = null;
		DiscreteGroupColorPicker colorPicker = null;
		int allowedChangeOfBasis = 0;
		double[] origin = {0d, 0d, 0d, 1d}; 
		double[] zpoint = {0d, 0d, 1d, 1d}; 
		double tx1 = 1.0, ty1 = 0.0, tx2 = 0.0, ty2 = 1.0, x0 = 0, y0 = 0;
		switch (num)	{
			case _O:
				allowedChangeOfBasis = COB_SHEAR | COB_SCALE | COB_ROTATE;
				int numGenerators = 4;
				generators = new DiscreteGroupElement[4];
				generatorRepresentations = new SceneGraphComponent[2];
				for (int i = 0; i<2; ++i)	{
					generators[i] = new DiscreteGroupElement();
					generators[i].setWord(DiscreteGroupUtility.genNames[i]);
				}
				if (debug) for (int i = 0; i<2; ++i)	DiscreteGroupUtility.logger.log(Level.FINE,"Gen "+i+" "+generators[i].getWord());
				double z = -0.0;
				MatrixBuilder.euclidean().translate(tx1, ty1, z).assignTo(generators[0].getArray());
				MatrixBuilder.euclidean().translate(tx2, ty2, z).assignTo(generators[1].getArray());
//				generators[0].setTranslation( tx1, ty1,z);
//				generators[1].setTranslation(tx2, ty2,z);
				generators[2] = (DiscreteGroupElement) generators[0].getInverse();
				generators[3] = (DiscreteGroupElement) generators[1].getInverse();
				generatorRepresentations[0] = representationForTranslation(globalScale,0.0, 0.0, tx1, ty1);
				generatorRepresentations[1] = representationForTranslation(globalScale,0.0, 0.0, tx2, ty2);
				if (debug) for (int i = 0; i<4; ++i)	DiscreteGroupUtility.logger.log(Level.FINE,"Gen "+i+" "+generators[i].getWord());
				int[] weights = {1,1,1,1};
				colorPicker = new DiscreteGroupColorPicker.LinearFunctionColorPicker(generators, weights);				
				break;
				
			case _2222:
				allowedChangeOfBasis = COB_SCALE | COB_ROTATE | COB_SHEAR;
				numGenerators = 4;
				generators = new DiscreteGroupElement[numGenerators];
				generatorRepresentations = new SceneGraphComponent[numGenerators];
				for (int i = 0; i<numGenerators; ++i)	{
					generators[i] = new DiscreteGroupElement();
					generators[i].setWord(DiscreteGroupUtility.genNames[i]);
				}
				double[] pt1 = {0d, 0d, 0d, 1d};
				double[] pt2 = {0d, 0d, 1d, 1d};
				double[] mat = new double[16];
				P3.makeRotationMatrix(mat, pt1, pt2, Math.PI, Pn.EUCLIDEAN);
				generators[0].setArray(mat);
				pt1[0] = pt2[0] = .5;
				P3.makeRotationMatrix(mat, pt1, pt2, Math.PI, Pn.EUCLIDEAN);
				generators[1].setArray(mat);
				pt1[0] = pt2[0] = 0.0;
				pt1[1] = pt2[1] = .5;
				P3.makeRotationMatrix(mat, pt1, pt2, Math.PI, Pn.EUCLIDEAN);
				generators[2].setArray(mat);
				pt1[0] = pt2[0] = .5;
				P3.makeRotationMatrix(mat, pt1, pt2, Math.PI, Pn.EUCLIDEAN);
				generators[3].setArray(mat);
				generatorRepresentations[0] = representationForRotation(globalScale, 2, 0.0, 0.0); 
				generatorRepresentations[1] = representationForRotation(globalScale, 2, 0.5, 0.0); 
				generatorRepresentations[2] = representationForRotation(globalScale, 2, 0.0, 0.5); 
				generatorRepresentations[3] = representationForRotation(globalScale, 2, 0.5, 0.5); 
				colorPicker = new DiscreteGroupColorPicker.RotationColorPicker(2);	
				break;
				
			case _333:
				x0 = .5;
				y0 = Math.sqrt(3.0)/2.0;
				allowedChangeOfBasis = COB_ROTATE | COB_SCALE;
				numGenerators = 6;
				generators = new DiscreteGroupElement[numGenerators];
				generatorRepresentations = new SceneGraphComponent[3];
				for (int i = 0; i<3; ++i)	{
					generators[i] = new DiscreteGroupElement();
					generators[i].setWord(DiscreteGroupUtility.genNames[i]);
				}
				double[] foo = {x0, 0d, 0d, 1d};
				pt1 = foo;
				double[] bar = {x0, 0d, 1d, 1d};
				pt2 = bar;
				mat = new double[16];
				P3.makeRotationMatrix(mat, pt1, pt2, 2*Math.PI/3.0, Pn.EUCLIDEAN);
				generators[0].setArray(mat);
				pt1[0] = pt2[0] = 0;
				pt1[1] = pt2[1] = y0;
				P3.makeRotationMatrix(mat, pt1, pt2, 2*Math.PI/3.0, Pn.EUCLIDEAN);
				generators[1].setArray(mat);
				pt1[0] = pt2[0] = -x0;
				pt1[1] = pt2[1] = 0;
				P3.makeRotationMatrix(mat, pt1, pt2, 2*Math.PI/3.0, Pn.EUCLIDEAN);
				generators[2].setArray(mat);
				generatorRepresentations[0] = representationForRotation(globalScale, 3, x0, 0.0);
				generatorRepresentations[1] = representationForRotation(globalScale, 3, 0, y0);
				generatorRepresentations[2] = representationForRotation(globalScale, 3, -x0, 0.0);
				for (int i = 0 ; i<3; ++i)	generators[i+3] = (DiscreteGroupElement) generators[i].getInverse();
				colorPicker = new DiscreteGroupColorPicker.RotationColorPicker(3);	
				break;
				
			case _244:
				allowedChangeOfBasis = COB_SCALE | COB_ROTATE;
				numGenerators = 6;
				generators = new DiscreteGroupElement[numGenerators];
				generatorRepresentations = new SceneGraphComponent[3];
				for (int i = 0; i<3; ++i)	{
					generators[i] = new DiscreteGroupElement();
					generators[i].setWord(DiscreteGroupUtility.genNames[i]);
				}
				mat = new double[16];
				P3.makeRotationMatrix(mat,  new double[] {0,0,0}, new double[] {0,0,1}, Math.PI/2.0, Pn.EUCLIDEAN);
				generators[0].setArray(mat);
				P3.makeRotationMatrix(mat,new double[] {.5,.5,0}, new double[] {.5,.5,1}, Math.PI, Pn.EUCLIDEAN);
				generators[1].setArray(mat);
				P3.makeRotationMatrix(mat, new double[] {0,1,0}, new double[] {0,1,1}, Math.PI/2.0, Pn.EUCLIDEAN);
				generators[2].setArray(mat);
				generatorRepresentations[0] = representationForRotation(globalScale, 4, 0.0, 0.0);
				generatorRepresentations[1] = representationForRotation(globalScale, 2, .5, .5);
				generatorRepresentations[2] = representationForRotation(globalScale, 4, 0.0, 1.0);
				for (int i = 0; i<3; ++i)	generators[i+3] = (DiscreteGroupElement) generators[i].getInverse();
				colorPicker = new DiscreteGroupColorPicker.RotationColorPicker(4);				
				break;
				
			case _236:
				x0 = .5;
				y0 =   Math.sqrt(3.0)/2.0;
				allowedChangeOfBasis = COB_SCALE | COB_ROTATE;
				numGenerators = 6;
				generators = new DiscreteGroupElement[numGenerators];
				generatorRepresentations = new SceneGraphComponent[3];
				for (int i = 0; i<3; ++i)	{
					generators[i] = new DiscreteGroupElement();
					generators[i].setWord(DiscreteGroupUtility.genNames[i]);
				}
				pt1 = origin;
				pt2 = zpoint;
				mat = new double[16];
				P3.makeRotationMatrix(mat, pt1, pt2, Math.PI, Pn.EUCLIDEAN);
				generators[0].setArray(mat);
				pt1[0] = pt2[0] = x0;
				P3.makeRotationMatrix(mat, pt1, pt2, 2*Math.PI/3.0, Pn.EUCLIDEAN);
				generators[1].setArray(mat);
				pt1[0] = pt2[0] = 0.0;
				pt1[1] = pt2[1] = y0;
				P3.makeRotationMatrix(mat, pt1, pt2, Math.PI/3.0, Pn.EUCLIDEAN);
				generators[2].setArray(mat);
				for (int i = 0; i<3; ++i)	generators[i+3] = (DiscreteGroupElement) generators[i].getInverse();
				generatorRepresentations[0] = representationForRotation(globalScale, 2,0.0, 0.0);
				generatorRepresentations[1] = representationForRotation(globalScale, 3, x0, 0);
				generatorRepresentations[2] = representationForRotation(globalScale, 6, 0, y0);
				colorPicker = new DiscreteGroupColorPicker.RotationColorPicker(6);	
				break;
				
			case _SS:
				allowedChangeOfBasis = COB_SCALE | COB_ROTATE | COB_YSCALE;
				numGenerators = 4;
				generators = new DiscreteGroupElement[numGenerators];
				generatorRepresentations = new SceneGraphComponent[3];
				for (int i = 0; i<3; ++i)	{
					generators[i] = new DiscreteGroupElement();
					generators[i].setWord(DiscreteGroupUtility.genNames[i]);
				}
//				generators[0].setTranslation( 0.0, 1.0,0.0);
				MatrixBuilder.euclidean().translate(0.0, 1.0,0.0).assignTo(generators[0].getArray());

				double[] plane = {1d, 0d, 0d, 0};
				mat = new double[16];
				P3.makeReflectionMatrix(mat, plane, Pn.EUCLIDEAN);
				generators[1].setArray(mat);

				plane[3] = -1.0;
				P3.makeReflectionMatrix(mat, plane, Pn.EUCLIDEAN);
				generators[2].setArray(mat);

				generators[3] = (DiscreteGroupElement) generators[0].getInverse();
				generatorRepresentations[0] = representationForReflection(globalReflectionScale, 0.0, 0.0, 0.0, 1.0);
				generatorRepresentations[1] = representationForReflection(globalReflectionScale, 1.0, 0.0, 1.0, 1.0);
				generatorRepresentations[2] = representationForTranslation(globalScale, 0.5, 0.0, 0.5, 1.0);
				colorPicker = new DiscreteGroupColorPicker.ReflectionColorPicker();		
				break;
				
			case _XX:
				allowedChangeOfBasis = COB_SCALE | COB_ROTATE | COB_YSCALE;
				numGenerators = 4;
				generators = new DiscreteGroupElement[numGenerators];
				generatorRepresentations = new SceneGraphComponent[2];
				for (int i = 0; i<2; ++i)	{
					generators[i] = new DiscreteGroupElement();
					generators[i].setWord(DiscreteGroupUtility.genNames[i]);
				}
				MatrixBuilder.euclidean().translate( 1.0, 0.0,0.0).assignTo(generators[0].getArray());
//				generators[0].setTranslation( 1.0, 0.0,0.0);

				plane = new double[]{1d, 0d, 0d, 0};
				mat = new double[16];
				P3.makeReflectionMatrix(mat, plane, Pn.EUCLIDEAN);

				double[] tvec = {0d, 1.0, 0d};
				double[] trans = P3.makeTranslationMatrix(null, tvec, Pn.EUCLIDEAN);
				Rn.times(mat,mat,trans);
				generators[1].setArray(mat);

				generators[2] = (DiscreteGroupElement) generators[0].getInverse();
				generators[3] = (DiscreteGroupElement) generators[1].getInverse();
				//generatorRepresentations[0] = representationForTranslation(globalScale, 0.0, 0.0, 1.0, 0.0);
				generatorRepresentations[0] = representationForGlideReflection(globalScale, 0.0, 0.0, 0.0, 1.0);
				generatorRepresentations[1] = representationForGlideReflection(globalScale, 0.5, 0.0, 0.5, 1.0);
				colorPicker = new DiscreteGroupColorPicker.ReflectionColorPicker();				
				break;
				
			case _S2222:
				allowedChangeOfBasis = COB_SCALE | COB_ROTATE | COB_YSCALE;
				numGenerators = 4;
				generators = new DiscreteGroupElement[numGenerators];
				generatorRepresentations = new SceneGraphComponent[numGenerators];
				for (int i = 0; i<numGenerators; ++i)	{
					generators[i] = new DiscreteGroupElement();
					generators[i].setWord(DiscreteGroupUtility.genNames[i]);
				}
				plane = new double[] {1d, 0d, 0d, -1};
				mat = P3.makeReflectionMatrix(null, plane, Pn.EUCLIDEAN);
				generators[0].setArray(mat);
				plane[3] =  0;
				P3.makeReflectionMatrix(mat, plane, Pn.EUCLIDEAN);
				generators[1].setArray(mat);
				plane[0] = 0.0; plane[1] = 1.0; plane[2] = 0.0; plane[3] = -1;
				P3.makeReflectionMatrix(mat, plane, Pn.EUCLIDEAN);
				generators[2].setArray(mat);
				plane[3] = 0;
				P3.makeReflectionMatrix(mat, plane, Pn.EUCLIDEAN);
				generators[3].setArray(mat);
				generatorRepresentations[0] = representationForReflection(globalReflectionScale, 0.0, 0.0, 0.0, 1.0);
				generatorRepresentations[1] = representationForReflection(globalReflectionScale, 1.0, 0.0, 1.0, 1.0);
				generatorRepresentations[2] = representationForReflection(globalReflectionScale, 0.0, 0.0, 1.0, 0.0);
				generatorRepresentations[3] = representationForReflection(globalReflectionScale, 0.0, 1.0, 1.0, 1.0);
				colorPicker = new DiscreteGroupColorPicker.ReflectionColorPicker();				
				break;
				
			case _22S:
				allowedChangeOfBasis = COB_SCALE | COB_ROTATE | COB_YSCALE;
				numGenerators = 3;
				generators = new DiscreteGroupElement[numGenerators];
				generatorRepresentations = new SceneGraphComponent[3];
				for (int i = 0; i<3; ++i)	{
					generators[i] = new DiscreteGroupElement();
					generators[i].setWord(DiscreteGroupUtility.genNames[i]);
				}
				plane = new double[] {1d, 0d, 0d, 0d};
				mat = new double[16];
				P3.makeReflectionMatrix(mat, plane, Pn.EUCLIDEAN);
				generators[0].setArray(mat);
				
				{
					double[] p0 = {.5,0,0}, p1 = {.5,0,1};
					P3.makeRotationMatrix(mat, p0, p1, Math.PI, Pn.EUCLIDEAN);
					generators[1].setArray(mat);
				}
				
				{
					double[] p0 = {.5,1,0}, p1 = {.5,1,1};
					P3.makeRotationMatrix(mat, p0, p1, Math.PI, Pn.EUCLIDEAN);
					generators[2].setArray(mat);
				}
				
				generatorRepresentations[0] = representationForReflection(globalReflectionScale, 0.0, 0.0, 0.0, 1.0);
				generatorRepresentations[1] = representationForRotation(globalScale, 2, 0.5, 0.0);
				generatorRepresentations[2] = representationForRotation(globalScale, 2,0.5, 1.0);
				colorPicker = new DiscreteGroupColorPicker.ReflectionColorPicker();		
				break;
				
			case _22X:
				allowedChangeOfBasis = COB_SCALE | COB_ROTATE | COB_YSCALE;
				numGenerators = 4;
				generators = new DiscreteGroupElement[numGenerators];
				generatorRepresentations = new SceneGraphComponent[3];
				for (int i = 0; i<2; ++i)	{
					generators[i] = new DiscreteGroupElement();
					generators[i].setWord(DiscreteGroupUtility.genNames[i]);
				}
				plane = new double[] {1d, 0d, 0d, 0};
				mat = new double[16];
				P3.makeReflectionMatrix(mat, plane, Pn.EUCLIDEAN);

				tvec = new double[]{0d, 1.0, 0d};
				trans = P3.makeTranslationMatrix(null, tvec, Pn.EUCLIDEAN);
				Rn.times(mat,mat,trans);
				generators[0].setArray(mat);

				plane[0] = 0.0; plane[1] = 1.0; plane[2] = 0.0; plane[3] = -.5d;
				P3.makeReflectionMatrix(mat, plane, Pn.EUCLIDEAN);
				tvec = new double[] {1.0, 0d,0d};
				trans = P3.makeTranslationMatrix(null, tvec, Pn.EUCLIDEAN);
				Rn.times(mat,mat,trans);
				generators[1].setArray(mat);
				
				generators[2] = (DiscreteGroupElement) generators[0].getInverse();
				generators[3] = (DiscreteGroupElement) generators[1].getInverse();
				generatorRepresentations[0] = representationForGlideReflection(globalScale, 0.0, 0.0, 0.0, 1.0);
				generatorRepresentations[1] = representationForRotation(globalScale, 2, 0.5, 0.0);
				generatorRepresentations[2] = representationForRotation(globalScale, 2,0.5, 1.0);
				colorPicker = new DiscreteGroupColorPicker.ReflectionColorPicker();				
				break;
				
			case _SX:
				allowedChangeOfBasis = COB_SCALE | COB_YSCALE | COB_ROTATE;
				numGenerators = 3;
				generators = new DiscreteGroupElement[numGenerators];
				generatorRepresentations = new SceneGraphComponent[2];
				for (int i = 0; i<2; ++i)	{
					generators[i] = new DiscreteGroupElement();
					generators[i].setWord(DiscreteGroupUtility.genNames[i]);
				}
				// "a" is vertical glide reflection of length 1 on line x = .5
				plane = new double[] {1d, 0d, 0d, -.5d};
				double[] p1 = {.5,0,0,1};
				double[] p2 = {.5,1,0,1};
				mat = new double[16];
				P3.makeGlideReflectionMatrix(mat, p1, p2, plane, Pn.EUCLIDEAN);
				generators[0].setArray(mat);
				
				// "b" is reflection in line x = 1
				plane[3] = 0.0d;
				P3.makeReflectionMatrix(mat, plane, Pn.EUCLIDEAN);
				generators[1].setArray(mat);
				
				generators[2] = (DiscreteGroupElement) generators[0].getInverse();
				generatorRepresentations[0] = representationForGlideReflection(globalScale, 0.5,-.5, 0.5, 0.5);
				generatorRepresentations[1] = representationForReflection(globalReflectionScale, 0.0, 0.0, 0.0, 1.0);
				colorPicker = new DiscreteGroupColorPicker.ReflectionColorPicker();		
				break;
				
			case _2S22:
				allowedChangeOfBasis = COB_SCALE | COB_YSCALE | COB_ROTATE;
				numGenerators = 3;
				generators = new DiscreteGroupElement[numGenerators];
				generatorRepresentations = new SceneGraphComponent[3];
				for (int i = 0; i<3; ++i)	{
					generators[i] = new DiscreteGroupElement();
					generators[i].setWord(DiscreteGroupUtility.genNames[i]);
				}
				double[] plane1 = {1d, 0d, 0d, 0};
				MatrixBuilder.euclidean().reflect(plane1).assignTo(generators[0].getArray());
				double[] plane2 = {0d, 1d, 0d, 0};
				MatrixBuilder.euclidean().reflect(plane2).assignTo(generators[1].getArray());
				double[] pnt1 =  {.5, .5, 0, 1};
				double[] pnt2  = {.5, .5, 1, 1};
				double[] rot = P3.makeRotationMatrix(null, pnt1, pnt2, Math.PI, Pn.EUCLIDEAN);
				generators[2].setArray(rot);
//				for (int i = 0; i<3; ++i)	
//					generators[3+i] = (DiscreteGroupElement) generators[i].getInverse();
				generatorRepresentations[0] = representationForRotation(globalScale, 2, .5, .5);
				generatorRepresentations[1] = representationForReflection(globalReflectionScale, 0.0, 0.0, 1.0, 0.0);
				generatorRepresentations[2] = representationForReflection(globalReflectionScale, 0.0, 0.0, 0.0, 1.0);
				colorPicker = new DiscreteGroupColorPicker.ReflectionColorPicker();
				break;
				
			case _3S3:
				x0 = 1; 
				y0 = Math.sqrt(3.0);
				allowedChangeOfBasis = COB_SCALE | COB_ROTATE;
				numGenerators = 3;
				generators = new DiscreteGroupElement[numGenerators];
				generatorRepresentations = new SceneGraphComponent[2];
				for (int i = 0; i<2; ++i)	{
					generators[i] = new DiscreteGroupElement();
					generators[i].setWord(DiscreteGroupUtility.genNames[i]);
				}
				double[][] points = new double[4][4];
				plane = new double[4];
				mat = new double[16];
				points[0][3] = points[1][3] = points[2][3] = 1.0;
				points[0][0] = x0;
				points[1][1] = y0;
				points[3][2] = 1.0;
				points[3][3] = 0.0;
				P3.planeFromPoints(plane, points[0], points[1], points[3]);
				if (debug) DiscreteGroupUtility.logger.log(Level.FINE,"Plane:"+Rn.toString(plane));
				P3.makeReflectionMatrix(mat, plane, Pn.EUCLIDEAN);
				if (debug) DiscreteGroupUtility.logger.log(Level.FINE,"Refl:"+Rn.matrixToString(mat));
				generators[0].setArray(mat);
				
				points[2][1] = y0/3;
				points[3][1] = y0/3.0;
				points[3][3] = 1.0;
				P3.makeRotationMatrix(mat, points[2], points[3], 2*Math.PI/3.0, Pn.EUCLIDEAN);
				if (debug) DiscreteGroupUtility.logger.log(Level.FINE,"Rot:"+Rn.matrixToString(mat));
				generators[1].setArray(mat);
				generators[2] = (DiscreteGroupElement) generators[1].getInverse();
				generatorRepresentations[0] = representationForReflection(globalReflectionScale, -x0, 0, x0, 0);
				generatorRepresentations[1] = representationForRotation(globalScale, 3, 0.0, y0/3.0);
				colorPicker = new DiscreteGroupColorPicker.ReflectionColorPicker();		
				break;
				
			case _S333:
				x0 = .5;
				y0 = Math.sqrt(3.0)/2.0;
				allowedChangeOfBasis = COB_SCALE | COB_ROTATE;
				numGenerators = 3;
				generators = new DiscreteGroupElement[numGenerators];
				generatorRepresentations = new SceneGraphComponent[numGenerators];
				for (int i = 0; i<numGenerators; ++i)	{
					generators[i] = new DiscreteGroupElement();
					generators[i].setWord(DiscreteGroupUtility.genNames[i]);
				}
				points = new double[4][4];
				plane = new double[4];
				mat = new double[16];
				for (int i = 0; i<4; ++i)	for (int j=0; j<4; j++)	points[i][j] = 0.0;
				points[0][3] = points[1][3] = points[2][3] = 1.0;
				points[0][0] = x0;
				points[1][0] = 0;
				points[1][1] = y0;
				points[2][0] = -x0;
				points[3][2] = 1.0;
				points[3][3] = 0.0;
				P3.planeFromPoints(plane, points[0], points[1], points[3]);
				P3.makeReflectionMatrix(mat, plane, Pn.EUCLIDEAN);
				generators[0].setArray(mat);
				
				P3.planeFromPoints(plane, points[1], points[2], points[3]);
				P3.makeReflectionMatrix(mat, plane, Pn.EUCLIDEAN);
				generators[1].setArray(mat);
				
				P3.planeFromPoints(plane, points[2], points[0], points[3]);
				P3.makeReflectionMatrix(mat, plane, Pn.EUCLIDEAN);
				generators[2].setArray(mat);
				generatorRepresentations[0] = representationForReflection(globalReflectionScale, x0, 0, 0, y0);
				generatorRepresentations[1] = representationForReflection(globalReflectionScale, -x0, 0, x0, 0);
				generatorRepresentations[2] = representationForReflection(globalReflectionScale, -x0, 0, 0, y0);
				colorPicker = new DiscreteGroupColorPicker.ReflectionColorPicker();				
				break;
				
			case _4S2:
				allowedChangeOfBasis = COB_SCALE | COB_ROTATE;
				numGenerators = 3;
				generators = new DiscreteGroupElement[numGenerators];
				generatorRepresentations = new SceneGraphComponent[2];
				for (int i = 0; i<2; ++i)	{
					generators[i] = new DiscreteGroupElement();
					generators[i].setWord(DiscreteGroupUtility.genNames[i]);
				}
				pt1 = new double[] {0d, 0d, 0d, 1d};
				pt2 = new double[] {0d, 0d, 1d, 1d};
				mat = new double[16];
				//Pn.makeRotationMatrix(mat, pt1, pt2, 2*Math.PI/3.0, Pn.EUCLIDEAN);
				P3.makeRotationMatrix(mat, pt2, Math.PI/2.0);
				generators[0].setArray(mat);
				plane = new double[] {1,1,0,-1.0};
				P3.makeReflectionMatrix(mat,plane, Pn.EUCLIDEAN);
				generators[1].setArray(mat);
				generators[2] = (DiscreteGroupElement) generators[0].getInverse();
				generatorRepresentations[0] = representationForRotation(globalScale, 4, 0.0, 0.0);
				generatorRepresentations[1] = representationForReflection(globalReflectionScale,1.0, 0.0, 0.0, 1.0);
				colorPicker = new DiscreteGroupColorPicker.ReflectionColorPicker();		
				break;
				
			case _S244:
				allowedChangeOfBasis = COB_SCALE | COB_ROTATE;
				numGenerators = 3;
				generators = new DiscreteGroupElement[numGenerators];
				generatorRepresentations = new SceneGraphComponent[numGenerators];
				for (int i = 0; i<numGenerators; ++i)	{
					generators[i] = new DiscreteGroupElement();
					generators[i].setWord(DiscreteGroupUtility.genNames[i]);
				}
				double[][] verts = {{0,0,0,1},{1.0,0,0,1}, {0.0,1.0,0,1},{0,0,1,0}};
				plane1 = P3.planeFromPoints(null, verts[0], verts[1], verts[3]);
				if (debug) DiscreteGroupUtility.logger.log(Level.FINE,"Reflection plane: "+Rn.toString(plane1));
				
				mat = P3.makeReflectionMatrix(null, plane1, Pn.EUCLIDEAN);
				generators[0].setArray(mat);
				P3.planeFromPoints(plane1, verts[1], verts[2], verts[3]);
				if (debug) DiscreteGroupUtility.logger.log(Level.FINE,"Reflection plane: "+Rn.toString(plane1));
				P3.makeReflectionMatrix(mat, plane1, Pn.EUCLIDEAN);
				generators[1].setArray(mat);
				P3.planeFromPoints(plane1, verts[2], verts[0], verts[3]);
				if (debug) DiscreteGroupUtility.logger.log(Level.FINE,"Reflection plane: "+Rn.toString(plane1));
				P3.makeReflectionMatrix(mat, plane1, Pn.EUCLIDEAN);
				generators[2].setArray(mat);
				generatorRepresentations[0] = representationForReflection(globalReflectionScale,0.0, 0.0, 1.0, 0.0);
				generatorRepresentations[1] = representationForReflection(globalReflectionScale,1.0, 0.0,0.0, 1.0);
				generatorRepresentations[2] = representationForReflection(globalReflectionScale,0.0, 1.0,0.0, 0.0);
				colorPicker = new DiscreteGroupColorPicker.ReflectionColorPicker();				
				break;
				
			case _S236:
				x0 = 1;
				y0 =   Math.sqrt(3.0);
				allowedChangeOfBasis = COB_SCALE | COB_ROTATE;
				numGenerators = 3;
				generators = new DiscreteGroupElement[numGenerators];
				generatorRepresentations = new SceneGraphComponent[numGenerators];
				for (int i = 0; i<numGenerators; ++i)	{
					generators[i] = new DiscreteGroupElement();
					generators[i].setWord(DiscreteGroupUtility.genNames[i]);
				}
				verts = new double[][] {{0,0,0,1},{x0,0,0,1}, {0,y0,0,1},{0,0,1,0}};
				plane1 =  P3.planeFromPoints(null, verts[0], verts[1], verts[3]);
				if (debug) DiscreteGroupUtility.logger.log(Level.FINE,"Reflection plane: "+Rn.toString(plane1));
				
				mat = P3.makeReflectionMatrix(null, plane1, Pn.EUCLIDEAN);
				generators[0].setArray(mat);
				P3.planeFromPoints(plane1, verts[1], verts[2], verts[3]);
				if (debug) DiscreteGroupUtility.logger.log(Level.FINE,"Reflection plane: "+Rn.toString(plane1));
				P3.makeReflectionMatrix(mat, plane1, Pn.EUCLIDEAN);
				generators[1].setArray(mat);
				P3.planeFromPoints(plane1, verts[2], verts[0], verts[3]);
				if (debug) DiscreteGroupUtility.logger.log(Level.FINE,"Reflection plane: "+Rn.toString(plane1));
				P3.makeReflectionMatrix(mat, plane1, Pn.EUCLIDEAN);
				generators[2].setArray(mat);
				generatorRepresentations[0] = representationForReflection(globalReflectionScale,0.0, 0.0, x0, 0.0);
				generatorRepresentations[1] = representationForReflection(globalReflectionScale,x0, 0.0,0.0,y0);
				generatorRepresentations[2] = representationForReflection(globalReflectionScale,0.0,y0,0.0, 0.0);
				colorPicker = new DiscreteGroupColorPicker.ReflectionColorPicker();				
				break;
				
			case C12:
				x0 = Math.cos(Math.PI/6);
				y0 = Math.sin(Math.PI/6);
				allowedChangeOfBasis = COB_SCALE | COB_ROTATE;
				numGenerators = 1;
				generators = new DiscreteGroupElement[numGenerators];
				generatorRepresentations = new SceneGraphComponent[numGenerators];
				for (int i = 0; i<numGenerators; ++i)	{
					generators[i] = new DiscreteGroupElement();
					generators[i].setWord(DiscreteGroupUtility.genNames[i]);
				}
				mat = P3.makeRotationMatrixZ(null, Math.PI/6);
				generators[0].setArray(mat);
				generatorRepresentations[0] = representationForRotation(globalScale, 12, 0.0, 0.0); 
				colorPicker = new DiscreteGroupColorPicker.ReflectionColorPicker();				
				break;
				
			case D24:
				x0 = Math.cos(Math.PI/12);
				y0 = Math.sin(Math.PI/12);
				allowedChangeOfBasis = COB_SCALE | COB_ROTATE;
				numGenerators = 2;
				generators = new DiscreteGroupElement[numGenerators];
				generatorRepresentations = new SceneGraphComponent[numGenerators];
				for (int i = 0; i<numGenerators; ++i)	{
					generators[i] = new DiscreteGroupElement();
					generators[i].setWord(DiscreteGroupUtility.genNames[i]);
				}
				verts = new double[][] {{0,0,0,1},{x0,0,0,1}, {x0,y0,0,1},{0,0,1,0}};
				plane1 = new double[]{0,1,0,0};
				if (debug) DiscreteGroupUtility.logger.log(Level.FINE,"Reflection plane: "+Rn.toString(plane1));
				
				mat = P3.makeReflectionMatrix(null, plane1, Pn.EUCLIDEAN);
				generators[0].setArray(mat);
				P3.planeFromPoints(plane1, verts[2], verts[0], verts[3]);
				if (debug) DiscreteGroupUtility.logger.log(Level.FINE,"Reflection plane: "+Rn.toString(plane1));
				P3.makeReflectionMatrix(mat, plane1, Pn.EUCLIDEAN);
				generators[1].setArray(mat);
				P3.planeFromPoints(plane1, verts[1], verts[2], verts[3]);
				if (debug) DiscreteGroupUtility.logger.log(Level.FINE,"Reflection plane: "+Rn.toString(plane1));
				P3.makeReflectionMatrix(mat, plane1, Pn.EUCLIDEAN);
//				generators[2].setMatrix(mat);
				generatorRepresentations[0] = representationForReflection(globalReflectionScale,0.0, 0.0, x0, 0.0);
				generatorRepresentations[1] = representationForReflection(globalReflectionScale,x0, 0.0,x0,y0);
//				generatorRepresentations[2] = representationForReflection(globalReflectionScale,x0,y0,0.0, 0.0);
				colorPicker = new DiscreteGroupColorPicker.ReflectionColorPicker();				
				break;
			default:
				DiscreteGroupUtility.logger.log(Level.WARNING,"No such wallpaper group named: "+names[num]);
				break;

		}
		theGroup = new WallpaperGroup();
		theGroup.setGenerators(generators);
		if ( num < 19) { //generatorRepresentations != null)	{
			SceneGraphComponent theReps = new SceneGraphComponent();
			theReps.setAppearance(repAp);
			for (int k = 0; k< generatorRepresentations.length; ++k)	theReps.addChild(generatorRepresentations[k]);
//			DefaultMatrixSupport.getSharedInstance().storeDefaultMatrices(theReps);
			theGroup.setGeneratorRepresentations(theReps);	
		}
//		if (num != CM && num<=16)
		if (num<=16)
			theGroup.setFsa(	 new FiniteStateAutomaton(oldNames[num]+".wa"));

		theGroup.setColorPicker(colorPicker);
		theGroup.setAllowedChangeOfBasis(allowedChangeOfBasis);
		theGroup.setName(names[num]);
		return theGroup;
	}
	
	static Appearance  repAp = null;
	static {
		repAp = new Appearance();
		repAp.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.BLACK);
		repAp.setAttribute(CommonAttributes.FACE_DRAW, false);
		repAp.setAttribute(CommonAttributes.EDGE_DRAW, true);
		repAp.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		repAp.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_STIPPLE_PATTERN, 0x5555);		
		repAp.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_STIPPLE, false);		
		repAp.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_WIDTH, 2.0);		
		repAp.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, false);		
	}
	private static double ww = Math.sqrt(3.0)/2.0, w2 = .5/(2*ww);
	private static final double[][] square = {{1,0,0},{1,1,0},{0,1,0},{0,0,0}},
					triangle244 = {{1,0,0},{0,1,0},{0,0,0}},
//					triangle244x2 = {{1,0,0},{0,1,0},{-1,0,0}},
					triangle236 = {{1,0,0},{0,2*ww, 0.0},{0,0,0}},
					triangle333 = {{.5,0,0},{0,ww, 0.0},{-.5,0,0}},
					triangle333x2 = {{.5,0,0},{0,ww, 0.0},{-.5,0,0}, {0, -ww, 0}},
					triangle114 = {{1,0,0},{0,2*ww/3.0,0}, {-1,0.0, 0.0}},
					triangleIsoc = {{0,1,0},{0,-1,0},{1,0,0}};
	private static final double[][] tcsquare = {{1,0,0},{1,1,0},{0,1,0},{0,0,0}},
		tctriangle244 = {{1,0,0},{0,1,0},{0,0,0}},
//		tctriangle244x2 = {{1,0,0},{.5,.5,0},{0,0,0}},
		tctriangle236 = {{.5+w2,0,0},{.5-w2,1, 0.0},{.5-w2,0,0}},		
		tctriangle333 = {{1,0,0},{.5,ww, 0.0},{0,0,0}},
		tctriangle333x2 = {{.5+1.0/(Math.sqrt(3)*2),.5,0},{.5,1, 0.0},{.5-1.0/(Math.sqrt(3)*2),.5,0}, {.5, 0, 0}},
		tctriangle114 = {{1,0,0},{.5,ww/3.0,0}, {0,0.0, 0.0}},
		tctriangleIsoc = {{.25,1,0},{.25,0,0},{.75,.5,0}};

	/* (non-Javadoc)
	 * @see DiscreteGroup#getFundamentalRegion()
	 */
	public  Geometry getDefaultFundamentalRegion() {
		int groupID = ((Integer) nameTable.get(name)).intValue();
		double[][] vertices1 = null;
		double[][] texCoords = null;
		
		switch (groupID)	{
			// square
			case _O:
			case _SS:
			case _XX:
			case _S2222:
			case _22S:
			case _22X:
			case _SX:
				vertices1 = square;
				texCoords = tcsquare;
				break;
				
			// 45-45-90 triangle
			case _2222:
			case _S244:
			case _4S2:
			case _2S22:
			case _244:
				vertices1 = triangle244;
				texCoords = tctriangle244;
				break;
				
			// 45-45-90 triangle consisting of 2 of previous 45-45-90 triangles
//			case _244:
//				vertices1 = triangle244x2;
//				texCoords = tctriangle244x2;
//				break;
			
			// 60-60-60 triangle
			case _S333:
			case _236:
				vertices1 = triangle333;
				texCoords = tctriangle333;
				break;
			
			// not a triangle at all, but two of previous glued along horizontal edge
			case _333:
				vertices1 = triangle333x2;
				texCoords = tctriangle333x2;
				break;
			
			// one-third of the S333 triangle
			case _3S3:
				texCoords = tctriangle114;
				vertices1 = triangle114;
				break;
			
			// 30-60-90 triangle
			case _S236:
				vertices1 = triangle236;
				texCoords = tctriangle236;
				break;
		
			// a 
//			case _SX:
//				vertices1 = triangleIsoc;
//				texCoords = tctriangleIsoc;
//				break;
						
			case C12:
				vertices1 = new double[][]{{0,0,0},{1,-Math.tan(Math.PI/12),0},{1,Math.tan(Math.PI/12),0}};
				texCoords = new double[][]{{0,Math.tan(Math.PI/12),0},{1,0,0},{1,2*Math.tan(Math.PI/12),0}};
				break;
			case D24:
				vertices1 = new double[][]{{1,0,0},{1,Math.tan(Math.PI/12),0},{0,0,0}};
				texCoords = vertices1;
				break;
			default:
				DiscreteGroupUtility.logger.log(Level.WARNING,"No such wallpaper group named: "+names[groupID]);
				//return null;
		}
		double[][] vertices = vertices1;
		
		if (vertices == null)	return null;
		Rn.average(centerPoint, vertices);
		centerPoint[3]=1.0;
		final IndexedFaceSet ifs = IndexedFaceSetUtility.constructPolygon(vertices);
		storeEdgeIds(this, ifs);
		final double[][] tmp = texCoords;
		Scene.executeWriter(ifs, new Runnable() {
			public void run() {
				ifs.setVertexAttributes(Attribute.TEXTURE_COORDINATES, 
				StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(tmp));
			}
		});
		return ifs;
	}

	public static void storeEdgeIds(DiscreteGroup dg, final IndexedFaceSet ifs) {
		double[][] vertices = ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		DiscreteGroupElement[] edgeids = edgeIdentifications(dg, vertices);
		HashMap<Integer, Integer> pairs = getPairedEdges(edgeids);
		ifs.setGeometryAttributes("edgePairs", pairs);
		boolean[] open = new boolean[edgeids.length];
		for (int i = 0; i<edgeids.length; ++i)	{
			open[i] = pairs.containsKey(i);
		}
		ifs.setGeometryAttributes("edgePairs", pairs);
		ifs.setGeometryAttributes("open", open);
		double[][] edgeMats = new double[edgeids.length][];
		String[] edgeMatNames = new String[edgeids.length];
		for (int i = 0; i<edgeids.length; ++i)	{
			edgeMats[i] = edgeids[i].getArray();
			edgeMatNames[i] = edgeids[i].getWord();
		}
		final DataList dl = StorageModel.DOUBLE_ARRAY.array(16).createReadOnly(edgeMats);
		final DataList dls = StorageModel.STRING_ARRAY.createReadOnly(edgeMatNames);
		Scene.executeWriter(ifs, new Runnable() {
			
			public void run() {
				ifs.setVertexAttributes(Attribute.attributeForName("edgeIds"),dl);
				ifs.setVertexAttributes(Attribute.attributeForName("edgeIdsWords"),dls);
			}
		});
	}
	
	public static DiscreteGroupElement[] edgeIdentifications(DiscreteGroup dg, double[][] polygon)	{
		
		DiscreteGroupSimpleConstraint dgsc = new DiscreteGroupSimpleConstraint(30);
		DiscreteGroupElement[] list = DiscreteGroupUtility.generateElements(dg, dgsc);
		double[][][] images = new double[list.length][][];
		int n = polygon.length;
		int[] edgeIds = new int[n];
		for (int i = 0; i<n; ++i) edgeIds[i] = -1;
		for (int i = 1; i<list.length; ++i)	{
			images[i] = Rn.matrixTimesVector(null, list[i].getArray(), polygon);
		}
		System.err.println("Polygon = "+Rn.toString(polygon));
		double tol = 10E-4;
		for (int i = 0; i<n; ++i)	{			// for each edge e
			for (int j=1; j<list.length; ++j)	{	// for each group element g
				for (int k = 0; k<n; ++k) {		// compare e with g(e)
					double d1 = Rn.euclideanDistance(polygon[k], images[j][i]);
					double d2 = Rn.euclideanDistance(polygon[(k+1)%n], images[j][(i+1)%n]);
					double d3 = Rn.euclideanDistance(polygon[(k+n-1)%n], images[j][(i+1)%n]);
					if (d1 < tol && (d2 < tol || d3 < tol) )	{
						edgeIds[i] = j;
						System.err.println("matched "+i+" and "+k);
//						System.err.println("polygon is "+Rn.toString(images[j]));
						System.err.println("Matrix = "+list[j].getWord());
						break;
					}
				}
				if (edgeIds[i] != -1) break;
			}
		}
		DiscreteGroupElement mats[] = new DiscreteGroupElement[n];
		for (int i = 0; i<n; ++i) {
			System.err.println(i+" = "+edgeIds[i]);
			if (edgeIds[i] < 0)	{
				double[] plane = P3.planeFromPoints(null,polygon[i], polygon[(i+1)%n], 
						Rn.add(null, polygon[i], Pn.zDirectionP3));
				mats[i] = new DiscreteGroupElement();
				mats[i].setWord("X");
				mats[i].setArray(P3.makeReflectionMatrix(null, plane, Pn.EUCLIDEAN));
			}
			else mats[i] = list[edgeIds[i]].getInverse();
		}
		return mats;
	}
	
	public static HashMap< Integer, Integer> getPairedEdges(DiscreteGroupElement[] matlist)	{
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (int i = 0; i<matlist.length; ++i) {
			double[] mat1 = matlist[i].getArray();
			for (int j = 0; j<matlist.length; ++j) {
				Integer foo = map.get(new Integer(j));
				if (foo != null && foo.intValue() == i) continue;
				double[] mat2 = matlist[j].getArray();
				if (Rn.isIdentityMatrix(Rn.times(null, mat1, mat2), 10E-8)) {
					map.put(i, j);
					System.err.println("Pair "+i+":"+j);
//					if (i != j) map.put(j,i);
				}
			}
		}
		System.err.println("Found "+map.size()+" pairs");
		return map;
	}

	public void setChangeOfBasis(double a, double sc, double x, double y)	{
		double[] cob = Rn.identityMatrix(4);
		double angle = changeOfBasisParameters[0];
		double scale = changeOfBasisParameters[1];
		double basisVector10 = changeOfBasisParameters[2];
		double basisVector11 = changeOfBasisParameters[3];
		if ((getAllowedChangeOfBasis() & COB_ROTATE) != 0) angle = a;
		if ((getAllowedChangeOfBasis() & COB_XSCALE) != 0)	basisVector10  = x; 
		if ((getAllowedChangeOfBasis() & COB_YSCALE) != 0)	basisVector11 = y;
		if ((getAllowedChangeOfBasis() & COB_SCALE) != 0)	scale = sc;
		double c, s;
		c = scale * Math.cos(angle);
		s = scale * Math.sin(angle);
		cob[0] =c;
		cob[4] = s;
		cob[1] = basisVector10 * c  - basisVector11 * s;
		cob[5] = basisVector10 *s  + basisVector11 * c;
		changeOfBasisParameters[0] = angle;
		changeOfBasisParameters[1] = scale;
		changeOfBasisParameters[2] = basisVector10;
		changeOfBasisParameters[3] = basisVector11;
		setChangeOfBasis(cob);
	}
	
	/**
	 * The four parameters are returned in an array: <i>(angle, scale, e2x, e2y)</i> where: 
	 * <ul>
	 * <li>    angle 	is the global rotation of the pattern around the origin
	 * <li>    scale	is the global scale of the pattern around the origin
	 * <li>    e2x		is the x-coordinate of the second basis vector
	 * <li>    e2y		is the y-coordinate of the second basis vector
	 * </ul>
	 * <p>    
	 * Here the first basis vector is always the x-basis vector (1,0).
	 * The resulting change of basis transformation is given by R(S(K))) where
	 * K is the skew transformation fixing (1,0) and taking (0,1)->(e2x,e2y);
	 * S is the global scale by <i>scale</i>, and R is the global rotation by <i>angle</i>.
	 * @return
	 */public double[] getChangeOfBasisParameters()	{
		double[] ret = new double[4];
		System.arraycopy(changeOfBasisParameters, 0, ret, 0, 4);
		return ret;
	}
	
	 public static void scaleBy(DiscreteGroupElement[] gens, double d)	{
		 for (DiscreteGroupElement g : gens)	{
			 g.setArray(Rn.times(null, d, g.getArray()));
		 }
	 }
}

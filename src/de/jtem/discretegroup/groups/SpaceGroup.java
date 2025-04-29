/*
 * Created on 20 Feb 2025
 *
 */
package de.jtem.discretegroup.groups;

import charlesgunn.math.p5.PlueckerLineGeometry;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jtem.discretegroup.core.DiscreteGroupColorPicker;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.discretegroup.core.DiscreteGroupUtility;

public class SpaceGroup extends EuclideanGroup  {

	public static String[] basenames = {
			"8o","4-","4o","4+","2-","2o","2+","1o",
			"8o2","4-2","4o2","4+2","2-2","2o2","2+2","1o2"};
	private static final int of = 8;
	
	public static final int _8o = 0,
			_4m = 1,
			_4o = 2,
			_4p = 3,
			_2m = 4,
			_2o = 5,
			_2p = 6,
			_1o = 7,
			_8o2 = 0+of,
			_4m2 = 1+of,
			_4o2 = 2+of,
			_4p2 = 3+of,
			_2m2 = 4+of,
			_2o2 = 5+of,
			_2p2 = 6+of,
			_1o2 = 7+of;
	
//	public static SpaceGroup instanceOfGroup(String name) {			
//		SpaceGroup theGroup = instanceOfGroup(name);
//		return convert2DTo3D(theGroup);
//	}
	
	public static SpaceGroup instanceOfGroup(int num)	{
		SpaceGroup sg = new SpaceGroup();
		sg.setMetric(Pn.EUCLIDEAN);	// only indirectly used, when creating various sorts of geometry associated to the group
		sg.setDimension(3);			// ditto
		sg.setFinite(false);
		DiscreteGroupColorPicker colorPicker = null;
		DiscreteGroupElement[] generators = null;
		double[][] reflPlanes = {{0,1,0,0},{0,1,-1,0},{-1,0,1,0},{-1,0,0,1}};
		double[][] points = {{0,0,0,1},{1,0,0,1},{1,0,1,1},{1,1,1,1}};
 		switch (num)	{
		case _8o2:
			generators = new DiscreteGroupElement[1+reflPlanes.length];
			for (int i = 0; i<reflPlanes.length; ++i)	{
				generators[i] = new DiscreteGroupElement();
				MatrixBuilder.euclidean().reflect(reflPlanes[i]).assignTo(generators[i].getArray());
				generators[i].setWord(DiscreteGroupUtility.genNames[i]);
			}
			generators[4] = new DiscreteGroupElement();
			double[] m1 = Rn.times(null, .5, Rn.add(null, points[0], points[3])),
					m2 = Rn.times(null, .5, Rn.add(null, points[1], points[2]));
			double[] m = P3.makeRotationMatrix(null, m1, m2, Math.PI, Pn.EUCLIDEAN);
			generators[4].setArray(m);
			generators[4].setWord(DiscreteGroupUtility.genNames[4]);
			colorPicker = new DiscreteGroupColorPicker.ReflectionColorPicker();				
			sg.setColorPicker(colorPicker);
//			System.err.println("m.pt = \n"+Rn.toString(Rn.matrixTimesVector((double [][])null, m, points)));
			double[] cp = {.375, .125, .25, 1};
			sg.setCenterPoint(cp);
			break;
		case _4m2:
			generators = new DiscreteGroupElement[reflPlanes.length];
			for (int i = 0; i<reflPlanes.length; ++i)	{
				generators[i] = new DiscreteGroupElement();
				MatrixBuilder.euclidean().reflect(reflPlanes[i]).assignTo(generators[i].getArray());
				generators[i].setWord(DiscreteGroupUtility.genNames[i]);
			}
			colorPicker = new DiscreteGroupColorPicker.ReflectionColorPicker();				
			sg.setColorPicker(colorPicker);
			double[] cp2 = {.75, .25, .5, 1};
			sg.setCenterPoint(cp2);
			break;
		case _2m2:
		{
			double[][] rPlanes = {{0,1,1,0},{0,1,-1,0},{-1,0,1,0},{-1,0,0,1}};
			double[][] pts = {{0,0,0,1},{1,-1,1,1},{1,0,0,1},{1,1,1,1}};

			generators = new DiscreteGroupElement[reflPlanes.length];
			for (int i = 0; i<reflPlanes.length; ++i)	{
				generators[i] = new DiscreteGroupElement();
				MatrixBuilder.euclidean().reflect(rPlanes[i]).assignTo(generators[i].getArray());
				generators[i].setWord(DiscreteGroupUtility.genNames[i]);
			}
			colorPicker = new DiscreteGroupColorPicker.ReflectionColorPicker();				
			sg.setColorPicker(colorPicker);
			cp = new double[]{.75, 0, .5, 1.0};
			sg.setCenterPoint(cp);
		}
			break;
		case _1o2:
		case _2o2:
		case _2p2:
		case _4o2:
		case _4p2:
		default:
		{
			double[][] rPlanes1 = {{0,1,1,0},{0,1,-1,0},{-1,0,1,0},{1,0,1,-2}};
			double[][] pts1 = {{0,0,0,1},{1,-1,1,1},{2,0,0,1},{1,1,1,1}};
			
			int numgens = reflPlanes.length; // + (num == _2o2 ? 1 : 0);
			generators = new DiscreteGroupElement[numgens];
			for (int i = 0; i<reflPlanes.length; ++i)	{
				generators[i] = new DiscreteGroupElement();
				MatrixBuilder.euclidean().reflect(rPlanes1[i]).assignTo(generators[i].getArray());
				generators[i].setWord(DiscreteGroupUtility.genNames[i]);
			}
			if (num == _2o2 || num == _2p2 || num == _4o2 || num == _4p2) {
				int i = 0;
				//generators[i]= new DiscreteGroupElement();
				if (num == _4o2) {
					double[] p1 = {1,0,0,1}, p2 = {1,0,1,1};
					double[] pl = {0,0,1,-.5};
					double [] rot = P3.makeRotationMatrix(null, p1, p2, Math.PI/2, Pn.EUCLIDEAN);
					double [] ref = P3.makeReflectionMatrix(null, pl, Pn.EUCLIDEAN);
					m = Rn.times(null, ref, rot);
					generators[i].setArray(m);
					double[] rot2 = Rn.times(null, m, m);
					generators[3].setArray(rot2);
					cp = new double[] {.75, .25, .5, 1};
				} else {
					i = 3;
					int i1,i2,j1,j2;
					if (num == _2o2) {
						i1 = 0; i2 = 2; j1 = 1; j2 = 3;
					} else { // 2p2 or 4p2
						i1 = 0; i2 = 3; j1 = 1; j2 = 2;
					}
					m1 = Rn.times(null, .5, Rn.add(null, pts1[i1], pts1[i2]));
					m2 = Rn.times(null, .5, Rn.add(null, pts1[j1], pts1[j2]));
					m = P3.makeRotationMatrix(null, m1, m2, Math.PI, Pn.EUCLIDEAN);					
					generators[i].setArray(m);
					cp = new double[] {.9, 0,.25, 1.0};
					if (num == _4p2) {
						i1 = 0; i2 = 1; j1 = 2; j2 = 3;
						m1 = Rn.times(null, .5, Rn.add(null, pts1[i1], pts1[i2]));
						m2 = Rn.times(null, .5, Rn.add(null, pts1[j1], pts1[j2]));
						double[] m3 = P3.makeRotationMatrix(null, m1, m2, Math.PI, Pn.EUCLIDEAN);					
						generators[0].setArray(m3);
						cp = new double[] {1,.25,.5, 1};
					}
				}
//				MatrixBuilder.euclidean().rotateX(Math.PI).assignTo(generators[i].getArray());
				System.err.println("m.pt = \n"+Rn.toString(Rn.matrixTimesVector((double [][])null, m, pts1)));
				System.err.println("m = \n"+Rn.matrixToString(m));
				System.err.println("m.ct = \n"+Rn.toString(Rn.matrixTimesVector((double [])null, m, cp)));
			} else {
				colorPicker = new DiscreteGroupColorPicker.ReflectionColorPicker();				
				sg.setColorPicker(colorPicker);
				cp = new double[]{1,0,.5, 1.0};
		}
			sg.setCenterPoint(cp);
		}
			break;
	}
		sg.setGenerators(generators);
		System.err.println("# Gens = "+generators.length);
		sg.setName(basenames[num]);
		sg.update();
		return sg;
	}

	public static void main(String[] args) {
		SpaceGroup sg = SpaceGroup.instanceOfGroup(_8o);
		DiscreteGroupSimpleConstraint dgconst = new DiscreteGroupSimpleConstraint(1, -1, 48);
		dgconst.setManhattan(true);
		sg.setConstraint(dgconst);
		sg.update();
		System.err.println("Sg elements # "+sg.getElementList().length);
		
	}
}

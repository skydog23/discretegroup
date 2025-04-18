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

	public static String[] names = {"8o","4-","4o","4+","2-","2o","2","1o"};
	public static final int _8o = 0,
			_4m = 1,
			_4o = 2,
			_4p = 3,
			_2m = 4,
			_2o = 5,
			_2p = 6,
			_1o = 7;
	
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
		double[][] reflPlanes = {{-1,0,0,0},{0,1,0,-1},{-1,0,1,0},{0,-1,1,0}};
		double[][] points = {{1,1,1,1},{0,0,0,1},{0,1,1,1},{0,1,0,1}};
 		switch (num)	{
		case _8o:
			generators = new DiscreteGroupElement[1+reflPlanes.length];
			for (int i = 0; i<reflPlanes.length; ++i)	{
				generators[i] = new DiscreteGroupElement();
				MatrixBuilder.euclidean().reflect(reflPlanes[i]).assignTo(generators[i].getArray());
				generators[i].setWord(DiscreteGroupUtility.genNames[i]);
			}
			generators[4] = new DiscreteGroupElement();
			double[] axis = PlueckerLineGeometry.lineFromPoints(null, 
					Rn.add(null, points[0], points[1]),
					Rn.add(null, points[2], points[3]));
			double[] m=P3.makeRotationMatrix(null, Rn.add(null, points[0], points[1]),
					Rn.add(null, points[2], points[3]), Math.PI, Pn.EUCLIDEAN);
			generators[4].setArray(m);
			generators[4].setWord(DiscreteGroupUtility.genNames[4]);
			colorPicker = new DiscreteGroupColorPicker.ReflectionColorPicker();				
			sg.setColorPicker(colorPicker);
			System.err.println("m.pt"+Rn.toString(Rn.matrixTimesVector((double [][])null, m, points)));
			double[] cp = {.25, .75, .5, 1.5};
			sg.setCenterPoint(cp);
			break;
	case _4o:
	default:
		generators = new DiscreteGroupElement[reflPlanes.length];
		for (int i = 0; i<reflPlanes.length; ++i)	{
			generators[i] = new DiscreteGroupElement();
			MatrixBuilder.euclidean().reflect(reflPlanes[i]).assignTo(generators[i].getArray());
			generators[i].setWord(DiscreteGroupUtility.genNames[i]);
		}
		colorPicker = new DiscreteGroupColorPicker.ReflectionColorPicker();				
		sg.setColorPicker(colorPicker);
		cp = new double[]{.25, .75, .5, 1.0};
		sg.setCenterPoint(cp);
		break;
	}
		sg.setGenerators(generators);
		sg.setName(names[num]);
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

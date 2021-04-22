package de.jtem.discretegroup.groups;

import java.util.Hashtable;

import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupElement;

public class FriezeGroup extends DiscreteGroup {

	// these are the names of the 7 frieze groups.   
	static Character infinity = '\u221e';
	final public static String[] friezeNames = {infinity+""+infinity,"*"+infinity+""+infinity,"22"+infinity, 
		infinity+"X",infinity+"*","2*"+infinity,"*22"+infinity};
	public static final int _88 = 0;
	public static final int _S88= 1;
	public static final int _228 = 2;	
	public static final int _8X = 3;
	public static final int _8S = 4;
	public static final int _2S8 = 5;
	public static final int _S228 = 6;

	public static Hashtable<String,Integer> nameTable = new Hashtable<String,Integer>();
	static {
		for (int i =0; i<friezeNames.length; ++i)	{
			nameTable.put(friezeNames[i], new Integer(i));
			}
	}

	double unitWidth = .5;
	boolean horizontalMirror = false;
	double[] changeOfBasisParameters  = {0,1,0,1};
	
	public FriezeGroup() {			
		super();
		setDimension(2);
	}

	public static FriezeGroup instanceOfGroup(String name) {	
		return instanceOfGroup(name, .3, Pn.EUCLIDEAN);
	}
	
	public static FriezeGroup instanceOfGroup(String name, double d, int metric) {			
		Integer lookup = (Integer) nameTable.get(name);
		if (lookup == null) return null;
		return instanceOfGroup(lookup.intValue(), d, metric);
	}
	
	public static FriezeGroup instanceOfGroup(int num, double d, int metric)	{
		FriezeGroup theGroup = new FriezeGroup();
		theGroup.setMetric(metric); 
		theGroup.setUnitWidth(d);
		theGroup.init(num);
		return theGroup;
	}
	
	private void init(int num)	{
		DiscreteGroupElement[] generators = null;
//		SceneGraphComponent[] generatorRepresentations = null;
//		DiscreteGroupColorPicker colorPicker = null;
//		int allowedChangeOfBasis = 0;
		double[] vec = {unitWidth,0,0,1};
		vec = Pn.dragTowards(null, P3.originP3, vec, unitWidth, metric);
		double d = Pn.distanceBetween(P3.originP3, vec, metric);
		double[] twice = Pn.dragTowards(null, P3.originP3, vec, 2*unitWidth, metric);
		double[] vecUp = vec.clone();
		vecUp[2] = 1.0;
		double[] xmirror = {vec[3], 0, 0, 0};
		double[] mirror = {vec[3], 0, 0, -vec[0]};
		double[] mirror2 = {twice[3], 0, 0, -twice[0]};
		double[] horizMirror = {0,1,0,0};
		double[] originUp = {0,0,1,1};
		switch(num)	{
			case 0:
				generators = new DiscreteGroupElement[2];
				generators[0] = new DiscreteGroupElement();
				MatrixBuilder.init(null, metric).translate(vec).assignTo(generators[0].getMatrix());
				break;
			case 1:
				generators = new DiscreteGroupElement[4];
				for (int i = 0; i<2; ++i)	generators[i] = new DiscreteGroupElement();
				MatrixBuilder.init(null, metric).reflect(xmirror).assignTo(generators[0].getMatrix());
				MatrixBuilder.init(null, metric).reflect(mirror).assignTo(generators[1].getMatrix());
				break;
			case 2:
				generators = new DiscreteGroupElement[4];
				for (int i = 0; i<2; ++i)	generators[i] = new DiscreteGroupElement();
				MatrixBuilder.init(null, metric).rotate(P3.originP3, originUp, Math.PI).assignTo(generators[0].getMatrix());
				MatrixBuilder.init(null, metric).rotate(vec, vecUp, Math.PI).assignTo(generators[1].getMatrix());
				break;
			case 3:
				generators = new DiscreteGroupElement[2];
				generators[0] = new DiscreteGroupElement();
				MatrixBuilder.init(null, metric).translate(vec).reflect(horizMirror).assignTo(generators[0].getMatrix());
//				horizontalMirror = true;
				break;
			case 4:
				generators = new DiscreteGroupElement[4];
				for (int i = 0; i<2; ++i)	generators[i] = new DiscreteGroupElement();
				MatrixBuilder.init(null, metric).translate(vec).assignTo(generators[0].getMatrix());
				MatrixBuilder.init(null, metric).reflect(horizMirror).assignTo(generators[1].getMatrix());
				horizontalMirror = true;
				break;
			case 5:
				generators = new DiscreteGroupElement[4];
				for (int i = 0; i<2; ++i)	generators[i] = new DiscreteGroupElement();
				MatrixBuilder.init(null, metric).rotate(vec, vecUp, Math.PI).assignTo(generators[0].getMatrix());
				MatrixBuilder.init(null, metric).reflect(mirror2).assignTo(generators[1].getMatrix());
				break;
			case 6:
				generators = new DiscreteGroupElement[6];
				for (int i = 0; i<3; ++i)	generators[i] = new DiscreteGroupElement();
				MatrixBuilder.init(null, metric).reflect(xmirror).assignTo(generators[0].getMatrix());
				MatrixBuilder.init(null, metric).reflect(mirror).assignTo(generators[1].getMatrix());
				MatrixBuilder.init(null, metric).reflect(horizMirror).assignTo(generators[2].getMatrix());
				horizontalMirror = true;
				break;
		}
		int n = generators.length/2;
		for (int i = 0; i<n; ++i)	
			generators[i+n] = generators[i].getInverse();
		setGenerators(generators);
		setName(friezeNames[num]);
		update();
		
	}


	public double getUnitWidth() {
		return unitWidth;
	}

	public void setUnitWidth(double unitWidth) {
		this.unitWidth = unitWidth;
	}

	public boolean isHorizontalMirror() {
		return horizontalMirror;
	}

	public void setHorizontalMirror(boolean horizontalMirror) {
		this.horizontalMirror = horizontalMirror;
	}
}
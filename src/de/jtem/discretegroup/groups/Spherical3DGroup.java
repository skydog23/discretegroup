package de.jtem.discretegroup.groups;

import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupUtility;

public class Spherical3DGroup {

	String[] names = {"333","334","343","433","335","533"};		// 24-, 120-, and 600-cell
	
	public static DiscreteGroup instanceOf(String name)	{
		if (name.equals("335")) return the600Cell();
		else throw new IllegalArgumentException("Not yet implemented: "+name);
	}
	
	public static DiscreteGroup the600Cell() {
		double[][] cc = { {1, -1, 1, 4/.944272},
				{-1, 1, 1, 4/.944272},
				{1, 1, -1, 4/.944272},
				{-1, -1, -1, 4/.944272}};
		DiscreteGroupElement[] gens = new DiscreteGroupElement[14];
		int count = 0;
		for (int i = 0; i<4; ++i)	{
			for (int j = i; j<4; ++j)	{
				if (i == j ) continue;
				double[] screw = P3.makeScrewMotionMatrix(null, cc[i], cc[j], Math.PI/5, Pn.ELLIPTIC);
				gens[count] = new DiscreteGroupElement(Pn.ELLIPTIC, screw);
				gens[count].setWord(DiscreteGroupUtility.genNames[count]);
				gens[count+6] = gens[count].getInverse();
				count++;
			}
		}
		double[] screw = P3.makeRotationMatrix(null, cc[0], cc[1], 2*Math.PI/5, Pn.ELLIPTIC);
		gens[12] = new DiscreteGroupElement(Pn.ELLIPTIC, screw);
		gens[12].setWord(DiscreteGroupUtility.genNames[count]);
		gens[13] = gens[12].getInverse();
		DiscreteGroup dg = new DiscreteGroup();
		dg.setDimension(3);
		dg.setMetric(Pn.ELLIPTIC);
		dg.setGenerators(gens);
		dg.setFinite(true);
		
		dg.update();
		System.err.println("# elements = "+dg.getElementList().length);
		return dg;
		
	}

	public static DiscreteGroup towerOfTetrahedra()	{
		DiscreteGroup dg = new DiscreteGroup();
		dg.setDimension(3);
		dg.setMetric(Pn.ELLIPTIC);
		dg.setFinite(true);
		dg.setName("tetrahedraTower");
		DiscreteGroupElement[] ogens = the600Cell().getGenerators(),
				gens = {ogens[3]};
		dg.setGenerators(gens);
		dg.update();
		DiscreteGroupElement[] justTen = dg.getElementList(),
				allThirty = new DiscreteGroupElement[30];
		double[][] order5 = {Rn.identityMatrix(4), ogens[12].getArray(), Rn.times(null, ogens[12].getArray(), ogens[12].getArray())};
		for (int i = 0; i< justTen.length; ++i) {
			for (int j = 0; j< 3; ++j) {
				double[] m = Rn.times(null, justTen[i].getArray(), order5[j]);
				allThirty[i+(j)*10] = new DiscreteGroupElement(Pn.ELLIPTIC, m);
				allThirty[i+(j)*10].setColorIndex(j);
				allThirty[i+(j)*10].setWord(i+":"+j);
			}
		}
		dg.setElementList(allThirty);
		dg.setGenerators(null);
		return dg;
	}

}

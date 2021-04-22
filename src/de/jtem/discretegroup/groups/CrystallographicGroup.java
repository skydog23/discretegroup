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

import java.util.Hashtable;

import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.FiniteStateAutomaton;


/**
 * @author gunn
 *
  */
public class CrystallographicGroup extends EuclideanGroup {

	public CrystallographicGroup() {
		super();
		dimension = 3;
	}
	public static String[] oldNames = {"P","P2","P3","P4","P6","PG","PM","PMM","PMG","PGG","CM","CMM","P31M","P3M1","P4G","P4M","P6M"};
	public static String[] names = {"O","2222","333","244","236","XX","**","*2222","22*","22X","*X","2*22","3*3","*333","4*2","*244","*236"};
	public static final int P = 0;
	public static final int P2 = 1;
	public static final int P3 = 2;
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
	static Hashtable<String,Integer> nameTable = new Hashtable<String,Integer>();
	static {
		for (int i =0; i<17; ++i)	{
			nameTable.put(names[i], new Integer(i));
			}
	}

		
	public static CrystallographicGroup instanceOfGroup(String name) {			
		WallpaperGroup theGroup = WallpaperGroup.instanceOfGroup(name);
		return convert2DTo3D(theGroup);
	}
	
	public static CrystallographicGroup instanceOfGroup(int num)	{
		WallpaperGroup theGroup = WallpaperGroup.instanceOfGroup(num);
		if (theGroup == null) return null;
		theGroup.calculateGenerators();
		// now we 3-dimensionalize the 2D group by appending a translation in z
		CrystallographicGroup cg = convert2DTo3D(theGroup);
		return cg;
	}
	
	public static CrystallographicGroup convert2DTo3D(WallpaperGroup wg)	{
		CrystallographicGroup cg = new CrystallographicGroup();
		cg.setName("3D"+wg.getName());
		cg.setMetric(Pn.EUCLIDEAN);
		cg.setDimension(3);
		
		DiscreteGroupElement[] dgel = wg.getGenerators();
		DiscreteGroupElement[] ngen = new DiscreteGroupElement[dgel.length + 2];
		int i;
		for (i=0; i<dgel.length; ++i)	{
				ngen[i] = dgel[i];
		}
		ngen[dgel.length] = new DiscreteGroupElement();
		ngen[dgel.length].setWord("z");
		MatrixBuilder.euclidean().reflect(new double[]{0,0,1,.5}).assignTo(ngen[dgel.length].getArray());
		ngen[dgel.length+1] = new DiscreteGroupElement();
		ngen[dgel.length+1].setWord("t");
		MatrixBuilder.euclidean().translate(0,0,1).assignTo(ngen[dgel.length+1].getArray());
//		ngen[i].setTranslation(0d, 0d, 1d);
//		ngen[dgel.length+1] = (DiscreteGroupElement) ngen[i].getInverse();
		cg.setGenerators(ngen);
		if (wg.getName().equals("O")) {
			FiniteStateAutomaton fsa = new FiniteStateAutomaton("3DP.wa");
			cg.setFsa(fsa);
		}
		//cg.setColorPicker(wg.getColorPicker());		
		return cg;
	}

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
		cob[0] =c;
		cob[4] = s;
		cob[1] = ynewx * c  - ynewy * s;
		cob[5] = ynewx *s  + ynewy * c;
		setChangeOfBasis(cob);
	}
	
}

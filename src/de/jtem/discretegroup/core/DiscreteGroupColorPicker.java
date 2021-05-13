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

import java.awt.Color;

import de.jreality.math.FactoredMatrix;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.shader.CommonAttributes;

/**
 * @author gunn
 *
 */
public abstract class DiscreteGroupColorPicker {

	private static double[] refl = P3.makeScaleMatrix(null, -1, -1, -1);
	public static Color[] colors = { java.awt.Color.GREEN, java.awt.Color.BLUE,java.awt.Color.RED, 
		java.awt.Color.MAGENTA, java.awt.Color.CYAN, java.awt.Color.YELLOW}; /*,
		java.awt.Color.PINK, java.awt.Color.BLACK, java.awt.Color.GRAY,
		new Color(.4f, .4f, .4f, 1f), java.awt.Color.GREEN, java.awt.Color.RED};
		
	static {
		colors[7] = new Color(.4f, .4f, .4f);
		colors[8] = new Color(.7f, .3f, .3f);
		colors[9] = new Color(.1f, .4f, 0f);
		//colors[10] = new Color(.3f, .3f, .8f, 1.0f);
		for (int i = 0; i<colors.length; ++i)	{
			System.err.println(colors[i].toString());
		}
	}
*/
	public static Appearance[] appearanceList;
	static {
		appearanceList = new Appearance[colors.length];
		for (int i = 0; i<colors.length; ++i)	{
			appearanceList[i] = new Appearance();
			appearanceList[i].setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, colors[i]);
		}
	}
	/**
	 * 
	 */
	public DiscreteGroupColorPicker() {
		super();
	}
	

	public abstract int calculateColorIndexForElement(DiscreteGroupElement dge);
	
	public  void assignColorIndices(DiscreteGroupElement[] elist)	{
		int n = elist.length;
		for (int i = 0; i<n; ++i)	{
			DiscreteGroupElement dge = elist[i];
			dge.colorIndex = calculateColorIndexForElement(dge);
			System.err.println("assigning "+dge.colorIndex);
		}
	}

	public static final class ReflectionColorPicker extends DiscreteGroupColorPicker {
		public int calculateColorIndexForElement(DiscreteGroupElement dge)	{
			double d = Rn.determinant(dge.getArray());
			if (d > 0)	return 0;
			return 1;
			}
	}
	public static final class RotationReflectionColorPicker extends RotationColorPicker {
		public RotationReflectionColorPicker() {
			this(2);
		}
		public RotationReflectionColorPicker(int c) {
			super(c);
		}

		public int calculateColorIndexForElement(DiscreteGroupElement dge)	{
			DiscreteGroupElement noref = new DiscreteGroupElement(dge);
			noref.setArray(Rn.times(null, dge.getArray(), refl));
			int n = super.calculateColorIndexForElement(noref);
			double d = Rn.determinant(dge.getArray());
			return ( d > 0 ? 0 : cycleSize)+n;
			}
	}

	public static  class RotationColorPicker extends DiscreteGroupColorPicker {
		int cycleSize;
		public RotationColorPicker()	{
			this(2);
		}
		public RotationColorPicker( int c) {
			cycleSize = c;
		}
		public int calculateColorIndexForElement(DiscreteGroupElement dge) {
			double[] m = dge.getArray();
			double det = Rn.determinant(dge.getArray());
			if (det < 0) {
				double[] refl = P3.makeReflectionMatrix(null, new double[]{0,1,0,0}, Pn.EUCLIDEAN);
				m = Rn.times(null, m, refl);
			}
			FactoredMatrix fm = new FactoredMatrix(m);
			double angle = fm.getRotationAngle()* cycleSize/(2 * Math.PI);
			if (angle < 0) angle += 2 * Math.PI;
			int ian = (int) Math.floor(angle+.01);
			return ian;
		}
	}
	public static final class LinearFunctionColorPicker extends DiscreteGroupColorPicker {
		DiscreteGroupElement[] gens;
		String genNames;
		int[] coefficients;
		public LinearFunctionColorPicker(DiscreteGroupElement[] g, int[] c) {
			super();
			gens = g;
			coefficients = c;
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i<gens.length; ++i)	{
				sb.append(gens[i].getWord().charAt(0));
			}
			genNames = sb.toString();
		}
		public int calculateColorIndexForElement(DiscreteGroupElement dge) {
			String word = dge.getWord();
			int count = 0;
			for (int i = 0; i<word.length(); ++i)	{
				for (int j = 0; j<genNames.length(); ++j)	{
					//if (j >= coefficients.length) continue;
					if (word.charAt(i) == genNames.charAt(j))	count+=coefficients[(j%coefficients.length)];
				}
			}
			return count;
		}

	}

}


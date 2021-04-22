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

import java.util.logging.Level;

import de.jreality.math.Matrix;
import de.jreality.math.Pn;
import de.jreality.math.Rn;

/**
 * @author gunn
 *
 */
public class DiscreteGroupElement  {
//	double[] array;
	Matrix matrix;
	String word;
	int colorIndex;
	boolean visible,
		mapsToNegativeW;
	int metric = Pn.EUCLIDEAN;
	final public static DiscreteGroupElement dgeID = new DiscreteGroupElement();
	
	public DiscreteGroupElement(int s, double[] m) {
		this(s, m, "?");
	}

	public DiscreteGroupElement() {
		this(Pn.EUCLIDEAN, null);
	}

	public DiscreteGroupElement(DiscreteGroupElement dge)	{
//		super( (Transformation) dge);
//		array =dge.array.clone();
		matrix = new Matrix(dge.getMatrix());
		colorIndex = dge.getColorIndex();
		visible = dge.isVisible();
		metric = dge.getMetric();
		word = dge.getWord();
	}

	public DiscreteGroupElement(int s, double[] m, String string) {
//		super(m);
		matrix = m == null ? new Matrix() : new Matrix(m);
//		array = m == null ? Rn.identityMatrix(4) : m;
		metric = s;
		word=string;
		visible = true;
	}

	public DiscreteGroupElement getInverse()	{
		DiscreteGroupElement inv = new DiscreteGroupElement(metric, matrix.getInverse().getArray());
		String invWord = invertWord(word);
		inv.setWord(invWord);
		inv.setColorIndex(colorIndex);
		DiscreteGroupUtility.logger.log(Level.FINE,inv.getWord());
		return inv;
	}

	private static String invertWord(String word) {
		char[] thisWord = word.toCharArray();
		int n = thisWord.length;
		char[] invWord = new char[n];
		for (int i = 0; i<n; ++i)	{
			char tc = thisWord[i];
			if (Character.isLowerCase(tc))	invWord[n-i-1] = Character.toUpperCase(tc);
			else							invWord[n-i-1] = Character.toLowerCase(tc);
		}
		return new String(invWord);
	}
	public double[] getArray()	{
		return matrix.getArray();
	}
	
	public void setArray(double[] m)	{
		System.arraycopy(m, 0, matrix.getArray(), 0, 16);
	}
	
	public Matrix getMatrix()	{
		return matrix;
	}
	
	public void setMatrix(Matrix m)	{
//		matrix = new Matrix(m);
		matrix = m;
	}
	
	public String getWord() {
		return word;
	}

	public void setWord(String string) {
		word = string;
	}

	public int getColorIndex() {
		return colorIndex;
	}

	public void setColorIndex(int c) {
		this.colorIndex = c;
	}

	public void setVisible(boolean b)	{
		visible = b;
	}
	public boolean isVisible() {
		return visible;
	}
	
  public int getMetric()	{
       return metric;
 	}
	
	/**
	 * Sets the metric metric of this transform. See {@link Pn}.
	 * @param aSig
	 */
  public void setMetric( int aSig)	{
  		metric = aSig;
//  		fireTransformationChanged();
	}

	public boolean isMapsToNegativeW() {
		return matrix.getArray()[15] < 0;
	}

	public void setMapsToNegativeW(boolean mapsToNegativeW) {
		this.mapsToNegativeW = mapsToNegativeW;
	}
	
	public void multiplyOnRight(DiscreteGroupElement el)	{
		matrix.multiplyOnRight(el.getArray());
		setWord(getWord()+el.getWord());
	}
	
	public void multiplyOnLeft(DiscreteGroupElement el)	{
		matrix.multiplyOnLeft(el.getArray());
		setWord(el.getWord()+getWord());
	}
	
	public static DiscreteGroupElement[] cartesianProduct(DiscreteGroupElement[] pro, DiscreteGroupElement[] u, DiscreteGroupElement[] v)	{
		if (u== null || v == null) return null;
		DiscreteGroupElement dge;
		DiscreteGroupElement[] product;
		if (pro != null && pro.length == u.length * v.length) product = pro;		
		else product = new DiscreteGroupElement[u.length * v.length];
		for (int i = 0; i<u.length; ++i)	{
			for (int j = 0; j<v.length; ++j)	{
				dge = product[i*v.length + j] = new DiscreteGroupElement();
				dge.setArray( Rn.times(dge.getArray(), u[i].getArray(), v[j].getArray()));
				dge.setWord(u[i].getWord()+v[j].getWord());
			}
		}
		return product;
	}


}

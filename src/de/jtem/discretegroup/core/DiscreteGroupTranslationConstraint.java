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


public class DiscreteGroupTranslationConstraint implements
		DiscreteGroupConstraint {

	int[] dims;
	String gens, inv;
	int dim = 3;
	int max = DiscreteGroupSimpleConstraint.globalMaxNumberElements;
	
	public DiscreteGroupTranslationConstraint(int a, int b, int c)	{
		this(a,b,c,"abc");
	}
	
	public DiscreteGroupTranslationConstraint(int a, int b, int c, String gens)	{
		super();
		dims = new int[]{a,b,c};
		dim = 3;
		this.gens = gens;
		inv = this.gens.toUpperCase();
	}
	public boolean acceptElement(DiscreteGroupElement dge) {
		int[] counts = countGens(dge.getWord());
		if (counts[0] < 0) return false;
		for (int i = 0; i<3; ++i)	
			if (counts[i] >= dims[i]) return false;
		return true;
	}

	public int getMaxNumberElements() {
		return max;
	}

	public void setMaxNumberElements(int i)	{
		max = i;
	}
	private int[] countGens(String word)	{
		int[] cnt = new int[3];
		boolean valid = true;
		for (int i = 0; i<word.length(); ++i)	{
			for (int j = 0; j<3; ++j)	{
				if (word.charAt(i) == gens.charAt(j)) cnt[j]++;
				else if (word.charAt(i) == inv.charAt(j))  {valid = false; break;}
			}
			if (!valid) break;
		}
		if (!valid) cnt[0] = cnt[1] = cnt[2] = -1;
		return cnt;
	}

	public void update() {
		// TODO Auto-generated method stub
		
	}
}

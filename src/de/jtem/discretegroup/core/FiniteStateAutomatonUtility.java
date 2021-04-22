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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class FiniteStateAutomatonUtility {

//	private static int debug = 0;

    static String __GENERATORS__ = "__GENERATORS__";
    static String __INVERSES__ = "__INVERSES__";
    static String __RELATIONS__ = "__RELATIONS__";
    
	static String fsaTemplate = "_RWS := rec( \n"+
			  "isRWS := true,\n"+
			  "ordering := \"shortlex\",\n"+
			  "generatorOrder := __GENERATORS__,\n"+
			  "inverses := __INVERSES__,\n"+
			  "equations := [__RELATIONS__]);";
	// TODO Fix problem with order-2 generators: must be consistent
	public static FiniteStateAutomaton generateFiniteStateAutomatonForGroup(DiscreteGroup dg)	{
		FiniteStateAutomaton fsa = new FiniteStateAutomaton();
		String transformedFSA = null;
		String genString = FiniteStateAutomatonUtility.generatorsAsString(dg.getGenerators());
		String invString = FiniteStateAutomatonUtility.invertString(genString);
		// have to check for generators that are their own inverses
		for (int i = 0; i<invString.length(); ++i)	{
			String x = invString.substring(i, i+1);
			if (x.charAt(0) == '[' || x.charAt(0) == ']' || x.charAt(0) == ',') continue;
			if ( genString.indexOf(x) == -1) {
				invString = invString.replace(x.charAt(0), FiniteStateAutomatonUtility.invertString(x).charAt(0));
			}
		}
		String tname = dg.getName();
		if (dg.getName().length() > 0) tname = dg.getName().replace(' ', '_');
		String outname = "/tmp/"+((tname.length() > 0) ? tname: "fsa"+((int)(Math.random()*100)));
		String kbexecutable = System.getenv("HOME")+"/Software/kbmag/bin/autgroup";
		String kbhome = System.getProperty("kbhome");
		if (kbhome != null) kbexecutable = kbhome;
		FileWriter fw = null;
		ArrayList<String> dupList = new ArrayList<String>();
		for (int i = 2; i<6; ++i)	{
			String[] newDups = DiscreteGroupUtility.getDuplicates(dg, new DiscreteGroupSimpleConstraint(-1.0, i));
			while (newDups.length > 0) {
				//for (int j = 0; j<newDups.length; ++j) dupList.add(newDups[j]);
				dupList.add(newDups[0]);
				System.err.println("Level "+i+" Added 1 duplicates "); //+newDups.length);
				transformedFSA = fsaTemplate.replaceFirst(__GENERATORS__, genString); //"[a,A,b,B,c,C,d,D,e,E,f,F]");
				transformedFSA = transformedFSA.replaceFirst(__INVERSES__, invString ); // "[A,a,B,b,C,c,D,d,E,e,F,f]");
				transformedFSA = transformedFSA.replaceFirst(__RELATIONS__, FiniteStateAutomatonUtility.relationsAsString(dupList)); //"[[a,A*A],[b,B*B],[c,C*C],[d,D*D],[e,E*E],[f,F*F], [a*d,c*a],[b*d,c*b],[c*f,e*c],[d*f,e*d],[f*b,a*f],[e*b,a*e]]"); //relationsAsString(dupList));
				System.err.println("fsa is "+transformedFSA);
				try {
					fw =  new FileWriter(outname);
					fw.write(transformedFSA);
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				String command = kbexecutable+" "+outname;
				System.err.println("Executing "+command);
				try {
					Process process = Runtime.getRuntime().exec(command);
					process.waitFor();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				BufferedReader fin = null;
				try {
					fin = new BufferedReader(new FileReader(outname+".wa"));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				fsa.initializeFrom(fin);
				try {
					fin.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				dg.setFsa(fsa);
				newDups = DiscreteGroupUtility.getDuplicates(dg, new DiscreteGroupSimpleConstraint(-1.0, i));				
			}
		}
		dg.setFsa(null);
		return fsa;
	}

	static  int intForChar(char c)	{
		if (Character.isLowerCase(c)) return (c - 'a');
		return 26 + (c - 'A');
	}

	static String relationsAsString(List<String> relationStrings)	{
			StringBuffer sb = new StringBuffer();
			boolean first = true;
			for (String reln : relationStrings) {
				sb.append(first ? "[" : ",[");
				first = false;
				System.err.println("relation is "+reln);
				sb.append(FiniteStateAutomatonUtility.insertStars(reln));
				sb.append(",IdWord]");
			}
	//		sb.append(']');
			return sb.toString();
		}

	static String insertStars(String word)	{
		int n = word.length();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i<n; ++i)	{
			sb.append(word.charAt(i));
			if (i != n-1) sb.append('*');
		}
//		System.err.println("returning "+sb.toString());
		return sb.toString();
	}

	public static String invertString(String gens)	{
		char[] gensc = gens.toCharArray();
		int n = gensc.length;
		char[] invWord = new char[n];
		for (int i = 0; i<n; ++i)	{
			char tc = gensc[i];
			if (tc == ',') invWord[i] = tc;
			if (Character.isLowerCase(tc))	invWord[i] = Character.toUpperCase(tc);
			else							invWord[i] = Character.toLowerCase(tc);
		}
		return new String(invWord);
	}

	static String generatorsAsString(DiscreteGroupElement[] gens)	{
		StringBuffer sb = new StringBuffer();
		HashSet<String> v = new HashSet<String>();
		int n = gens.length;
		sb.append('[');
		for (int i =0; i<n; ++i)	{
			v.add(gens[i].getWord());
			//v.add(PSL2C.invertWord(gens[i].getWord()));
		}
		boolean first = true;
		for (String foo : v)	{
			if (!first) { sb.append(','); }
			sb.append(foo);
			first = false; 
		}
		sb.append(']');
		return sb.toString();
	}

}

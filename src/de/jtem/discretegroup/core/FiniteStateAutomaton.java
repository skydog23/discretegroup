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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.logging.Level;

import de.jtem.discretegroup.ResourceClass;

/**
 * @author Charles Gunn
 *
 */
public class FiniteStateAutomaton {
	
	int[][] transitions;
	int[] lookupTable;	// maps alphabet (52 possibilities) to the right column of transition table
	private int numStates;
	int numLetters;
	private static int debug  = 0;
	Hashtable<String, Integer> nameTable;

	FiniteStateAutomaton()	{
		super();
	}
	
	public FiniteStateAutomaton(String genNames, int[][] t) {
		super();
		//TODO check comopatibility of arguments
		transitions = t;
		setupNameTable(genNames);
	}

	/**
	 *  @deprecated Use {@link #fsaForName(String, Class)}.
	 * @param waFileName
	 */public FiniteStateAutomaton(String waFileName) {
		this(waFileName, ResourceClass.class);
	}
	
	/**
	 *  @deprecated Use {@link #fsaForName(String, Class)}.
	 * @param waFileName
	 * @param cc
	 */
	public FiniteStateAutomaton(String waFileName, Class<?> cc) {
		super();
		fsaForName(this, waFileName, cc);
	}
	
	/**
	 * @deprecated Use {@link #fsaForName(String, Class)}.
	 * @param fin
	 */public FiniteStateAutomaton(BufferedReader fin) {
		super();
		initializeFrom(fin);
	}

	public static FiniteStateAutomaton fsaForName( String waFileName, Class<?> cc)	{
		return fsaForName(null, waFileName, cc);
	}

	public static FiniteStateAutomaton fsaForName(FiniteStateAutomaton fsa, String waFileName, Class<?> cc)	{
		try {
			String realName = null;
			File file = null;
			BufferedReader fin = null;
			DiscreteGroupUtility.logger.log(Level.FINE,"Attempting to open "+realName);
			if (waFileName.charAt(0) == '/')	{
				//realName = DiscreteGroup.resourceDir+"wa/"+waFileName;
				file = new File(waFileName);
				if (!file.exists()) throw new RuntimeException("file not found! ["+file.getCanonicalPath()+"]");
				fin = new BufferedReader(new FileReader(file));
			}
			else {
				realName = "resources/wa/"+waFileName;
				InputStream is = cc.getResourceAsStream(realName);
				if (is == null) return null;
				fin = new BufferedReader(new InputStreamReader(is));
			}	
			if (fsa == null) fsa = new FiniteStateAutomaton();
			fsa.initializeFrom(fin);
			if (debug>0)	fsa.debugPrint();
			return fsa;
		}
		catch (java.io.IOException ev)	{
			ev.printStackTrace();
		}
		return null;
	}
	
	public void debugPrint()	{
		for (int i = 0; i<getNumStates(); ++i)	{
			System.err.print("state "+i+": ");
			for (int j = 0; j < numLetters; ++j)	{
//				DiscreteGroupUtility.logger.log(Level.FINE,transitions[i][j]+"  ");
				System.err.print(transitions[i][j]+"  ");
			}
			System.err.println("");
		}
	}
	private void setupNameTable(String genNames)	{
		nameTable = new Hashtable<String, Integer>();
		lookupTable  = new int[52];
		for (int i = 0; i<genNames.length(); ++i)	{
			nameTable.put(genNames.substring(i,i+1), new Integer(i));	
			lookupTable[FiniteStateAutomatonUtility.intForChar(genNames.charAt(i))] = i;
		}
	}
	
	public void initializeFrom(BufferedReader fin) {
		String ss;
		boolean inAlphabet = false,
				inStates = false,
				inTransitions = false;
		int row = 0, column = 0;
		try {
			while ( (ss = fin.readLine()) != null)	{
				//System.out.println("Reading line "+ss);
				StringTokenizer st = new java.util.StringTokenizer(ss);
				while (st.hasMoreTokens())	{
					String token = st.nextToken();
					if (inTransitions)	{
						StringTokenizer st2 = new java.util.StringTokenizer(token.replaceAll("[^0-9]"," "));
						while (st2.hasMoreTokens())	{
							int foo = Integer.parseInt(st2.nextToken());
							transitions[row][column] = foo;
							column++;
							if (column == numLetters)	{
								column = 0;
								row++;
								if (row == getNumStates())	inTransitions = false;
							}
						}
					}
					else if (token.matches("alphabet")) {
						if (debug > 0) DiscreteGroupUtility.logger.log(Level.FINE,"Found alphabet");
						inAlphabet = true;
					} 
					else if (token.matches("states")) {
						if (debug > 0) DiscreteGroupUtility.logger.log(Level.FINE,"Found states");
						inAlphabet = false;
						inStates = true;
					} 
					else if (token.matches("transitions")) {
						if (debug > 0) DiscreteGroupUtility.logger.log(Level.FINE,"Found transitions");
						inStates = false;
						inTransitions = true;
						st.nextToken();		// ":="
						transitions = new int[getNumStates()][numLetters];
					} 
					else if (token.matches("size")) {
						if (debug > 0) DiscreteGroupUtility.logger.log(Level.FINE,"Found size");
						st.nextToken();	// ":="
						int num = Integer.parseInt(st.nextToken().replaceAll(",",""));
						if (inAlphabet)	{
							numLetters = num;
						} else if (inStates)	{
							setNumStates(num);
						}
					} 
					else if (token.matches("names")) {
						if (inAlphabet)	{
							st.nextToken();		// ":="
							// get rid of asterisks
							String names = st.nextToken().replaceAll("[[^a-z]&&[^A-Z]]","");
							//System.out.println("Names: "+names);
							setupNameTable(names);
						} 
					}
					if (debug > 0) DiscreteGroupUtility.logger.log(Level.FINE,token);
				}
			}
			fin.close();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static int STATE_FAIL	= 0;
	private static int STATE_START	= 1;
	public boolean accepts(String word)	{
		int state = STATE_START;
		for (int i = 0; i<word.length(); ++i)	{
//			String cc = word.substring(i,i+1);
//			int gen = ((Integer) nameTable.get(cc)).intValue();
//			if (debug > 0) DiscreteGroup.logger.log(Level.FINE,state+"-"+gen+" ");
			int gen = lookupTable[FiniteStateAutomatonUtility.intForChar(word.charAt(i))];
			state = transitions[state-1][gen];
			//if (debug > 0) System.err.print(state+"-"+gen+" ");
			if (state == STATE_FAIL) {
				//System.err.println("Rejecting "+word);
				return false;
			}
		}
		//if (debug > 0) System.err.println("Accepting "+word);
		return true;
	}

	public int[][] getTransitions() {
		return transitions;
	}

	public void setTransitions(int[][] is) {
		transitions = is;
	}

	public int getNumStates() {
		return numStates;
	}

	public void setNumStates(int numStates) {
		this.numStates = numStates;
	}
}

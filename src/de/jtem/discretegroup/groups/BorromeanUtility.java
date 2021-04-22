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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;

import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.util.Secure;
import de.jtem.discretegroup.ResourceClass;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupUtility;
import de.jtem.discretegroup.core.FiniteStateAutomaton;
import de.jtem.discretegroup.util.WingedEdge;
import de.jtem.discretegroup.util.WingedEdge.Edge;
import de.jtem.discretegroup.util.WingedEdge.Face;
public class BorromeanUtility {

    static String __POWERS__ = "__POWERS__";
    
	static String fsaTemplate = "_RWS := rec( \n"+
			  "isRWS := true,\n"+
			  "ordering := \"shortlex\",\n"+
			  "generatorOrder := [a,A,b,B,c,C,d,D,e,E,f,F],\n"+
			  "inverses := [A,a,B,b,C,c,D,d,E,e,F,f],\n"+
			  "equations := [__POWERS__ [c*a*d*A,IdWord],[d*b*c*B,IdWord],[e*c*f*C,IdWord],[e*B*E*A,IdWord],[f*d*e*D,IdWord],[f*A*F*B,IdWord]]);";
	private static char[] gens = {'a','b','c','d','e','f'};
	private static String powerRelnsForOrder(int n )	{
		if (n == -1) return "";
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i<6; ++i)	{
			sb.append('[');
			char g = gens[i];
			for (int j=0;j<n;++j) {
				sb.append(g);
				if (j!= (n-1)) sb.append('*');
			}
			sb.append(",IdWord]");
			sb.append(',');
		}
		System.err.println("power words: "+sb.toString());
		return sb.toString();
	}
	public static FiniteStateAutomaton fsaForBorromeanOrder(int n)	{
		String transformedFSA = fsaTemplate.replaceFirst(__POWERS__, powerRelnsForOrder(n)); //"[[a,A*A],[b,B*B],[c,C*C],[d,D*D],[e,E*E],[f,F*F], [a*d,c*a],[b*d,c*b],[c*f,e*c],[d*f,e*d],[f*b,a*f],[e*b,a*e]]"); //relationsAsString(dupList));

		BufferedReader br = new BufferedReader(new StringReader(transformedFSA));
		String outname = "/tmp/borrom"+n;
		FileWriter fw = null;
		FiniteStateAutomaton fsa = null;
		try {
			fw =  new FileWriter(outname);
			fw.write(transformedFSA);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String command = "/homes/geometer/gunn/Software/kbmag/bin/autgroup "+outname;
		String override = Secure.getProperty("discreteGroup.autgroupCommand");
		if (override != null) command = override + " "+outname;
		try {
			Process process = Runtime.getRuntime().exec(command);
			process.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		BufferedReader fin = null;
		try {
			fin = new BufferedReader(new FileReader(outname+".wa"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fsa = new FiniteStateAutomaton(fin);	
		try {
			//TODO figure out why this command doesn't work
			// leave the original descriptor file (no extension in name)
			String rmCmd = "/bin/rm -f "+outname+".*";
			System.err.println("Remove command is "+rmCmd);
			Process cleanup = Runtime.getRuntime().exec(rmCmd);
			cleanup.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return fsa;
	}
	
	public static DiscreteGroupElement[] borromeanLimitGenerators()	{
		double[] para = {-1, -2, 0, 2, 2, 1, 0, -2,0,0,1,0, -2, -2, 0, 3};
		return BorromeanUtility.generateGeneratorsFromSingleMotion(para, Pn.HYPERBOLIC);
	}

	public static DiscreteGroup borromeanGroupOfOrder(int n)	{
		DiscreteGroup tg = new DiscreteGroup();
		tg.setName("borromean-"+n);
		tg.setGenerators(borromeanGenerators(n));
		FiniteStateAutomaton fsa = FiniteStateAutomaton.fsaForName("borrom"+n+".wa", ResourceClass.class);
		if (fsa != null && fsa.getNumStates() > 0) {
			tg.setFsa(fsa);
			System.err.println("Got wa file");
		}
		else 
			tg.setFsa(fsaForBorromeanOrder(n));				
		tg.setMetric(n == 2 ? Pn.EUCLIDEAN : Pn.HYPERBOLIC);
		tg.setDimension(3);
		tg.setFinite(false);
		return tg;
	}
	
	public static double[][] borromColors = {{.3f, .5f, 1f},{.3f, 1f, .5f},{1f, .2f,.2f},{.7f, .6f, .6f}};
	public static void colorEdges(WingedEdge we)	{
		int ne = we.getNumEdges();
		double[][] ecolors = new double[ne][3];
		int i = 0;
		for (Edge edge : we.getEdgeList()) 	{
			Face f1 = edge.fL;
			Face f2 = edge.fR;
			DiscreteGroupElement dge1 = (DiscreteGroupElement) f1.source,
				dge2 = (DiscreteGroupElement) f2.source;
			double[] product = Rn.times(null, dge1.getArray(), dge2.getArray());
			int index = 3;
			if (Rn.isIdentityMatrix(product, 10E-8)) {
				if (dge1.getWord().equals("a") || dge1.getWord().equals("A")|| dge1.getWord().equals( "b")|| dge1.getWord().equals( "B")) index = 0;
				else if (dge1.getWord().equals("c")|| dge1.getWord().equals("C")|| dge1.getWord().equals( "d")|| dge1.getWord().equals( "D")) index = 1;
				else if (dge1.getWord().equals("e")|| dge1.getWord().equals("E")|| dge1.getWord().equals( "f")|| dge1.getWord().equals( "F")) index = 2;	
				else throw new IllegalStateException("Invalid word "+dge1.getWord());
			}
			ecolors[i] = borromColors[index];
			System.err.println("words = "+dge1.getWord()+" "+dge2.getWord());
			i++;
		}
		we.setEdgeAttributes(Attribute.COLORS, StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(ecolors));
		//we.update();
	}

	public static void colorFaces(WingedEdge we) {
		int ne = we.getNumFaces();
		double[][] ecolors = new double[ne][3];
		int i = 0;
		for (Face f1 : we.getFaceList()) 	{
			DiscreteGroupElement dge1 = (DiscreteGroupElement) f1.source;
			int index = 0;
			if (dge1.getWord().equals("a") || dge1.getWord().equals("A")|| dge1.getWord().equals( "b")|| dge1.getWord().equals( "B")) index = 0;
			else if (dge1.getWord().equals("c")|| dge1.getWord().equals("C")|| dge1.getWord().equals( "d")|| dge1.getWord().equals( "D")) index = 1;
			else if (dge1.getWord().equals("e")|| dge1.getWord().equals("E")|| dge1.getWord().equals( "f")|| dge1.getWord().equals( "F")) index = 2;	
			//else throw new IllegalStateException("Invalid word "+dge1.getWord());
			ecolors[i] = borromColors[index];
			System.err.println("words = "+dge1.getWord());
			i++;
		}
		we.setFaceAttributes(Attribute.COLORS, StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(ecolors));
		//we.update();
	}

	private static double[] prettier = P3.makeRotationMatrixX(null, Math.PI/2);
	public static DiscreteGroupElement[] borromeanGenerators(int order)	{
		DiscreteGroupElement[] gens;
		if (order == -1) gens =  borromeanLimitGenerators();
		else if (order == 2)	{
			// handle as special case, euclidean
			double[] p0 = {1,-1,0,1};
			double[] p1 = {1,1,0,1};
			double[] rot = P3.makeRotationMatrix(null,p1, p0, Math.PI, Pn.EUCLIDEAN);
			gens = generateGeneratorsFromSingleMotion(rot, Pn.EUCLIDEAN);
		}  else {
			double a,b,c,d;
			double angle = 2*Math.PI/order;
			double ca = Math.cos(angle);
			d = (ca - 3)/(ca - 1);
			b = (d - Math.sqrt(d*d - 4))/2;
			a = Math.sqrt(1-b);
			c = a/(2-b);
			
			System.err.println("a ab c: "+a+":"+a*b+":"+c);
			double[] p0 = {a, a*b, 0, 1};
			double[] p1 = {a, -a*b, 0, 1};
			double[] rot = P3.makeRotationMatrix(null,p1, p0, angle, Pn.HYPERBOLIC);
			gens = BorromeanUtility.generateGeneratorsFromSingleMotion(rot, Pn.HYPERBOLIC);
		}
		return gens;
	}

	static DiscreteGroupElement[] generateGeneratorsFromSingleMotion(double[] rot, int sig) {
		DiscreteGroupElement[] gens = new DiscreteGroupElement[sig == Pn.EUCLIDEAN ? 6 : 12];
		double[][] syms = getS3Group();
		int count = 0;
		for (int i = 0; i<6; ++i)	{
				double[] rotc = Rn.conjugateByMatrix(null, rot, syms[i]);
				gens[count] = new DiscreteGroupElement(sig, rotc);
				gens[count].setWord(  DiscreteGroupUtility.genNames[i]);
				gens[count].setColorIndex(i);
				if (sig != Pn.EUCLIDEAN)	{
					gens[count+6] = (DiscreteGroupElement) gens[count].getInverse();
					gens[count+6].setWord(  DiscreteGroupUtility.genInvNames[i]);					
				}
				count++;
		}
		for (int i = 0; i<gens.length; ++i)	{
			gens[i].setArray(Rn.conjugateByMatrix(null, gens[i].getArray(), prettier));
		}
		return gens;
	}
	public static double[][] getS3Group() {
		double[][][] syms = new double[2][3][];
		double[][] rets = new double[6][];
		syms[0][0] = syms[1][0] = Rn.identityMatrix(4);
		double[] p0 = {0,-1,0,1};
		double[] p1 = {0,1,0,1};
		syms[0][1] = P3.makeRotationMatrix(null,p0, p1, Math.PI, Pn.EUCLIDEAN);//diagonalMatrix(null, new double[]{-1,-1,-1,1});
		syms[1][2] = P3.makeRotationMatrix(null, new double[]{1,1,1}, 2*Math.PI/3);
		syms[1][1] = Rn.times(null, syms[1][2], syms[1][2]);
		for (int i = 0; i<3; ++i)	{
			for (int j=0;j<2; ++j)	{
				rets[2*i+j] = Rn.times(null, syms[1][i], syms[0][j]);
			}
		}
		return rets;
	}

	public static double[][] getS3Group2() {
		double[][][] syms = new double[2][3][];
		double[][] rets = new double[12][];
		syms[0][0] = syms[1][0] = Rn.identityMatrix(4);			
		double[] p0 = {1,-1,0,1};
		double[] p1 = {1,1,0,1};
		syms[0][1] = P3.makeRotationMatrix(null,p0, p1, Math.PI, Pn.EUCLIDEAN);
		syms[1][2] = P3.makeRotationMatrix(null, new double[]{1,1,1}, 2*Math.PI/3);
		syms[1][1] = Rn.times(null, syms[1][2], syms[1][2]);
		double[] p00 = {1,0,1,1};
		double[] p10 = {1,1,1,1};
		double[] dd =	Rn.diagonalMatrix(null, new double[]{-1,-1,-1,1});
		double[] rot = P3.makeRotationMatrix(null,p00, p10, Math.PI, Pn.EUCLIDEAN);
		double[] rot2 = new double[16];
		MatrixBuilder.euclidean().translate(-2,-2,-2).reflect(new double[]{0,0,1,-1}).assignTo(rot2);
		System.err.println("Matrix is "+Rn.matrixToString(rot));			
		for (int i = 0; i<3; ++i)	{
			for (int j=0;j<2; ++j)	{
				rets[2*i+j] = Rn.times(null, syms[1][i],  syms[0][j]);
				rets[2*i+j+6] = Rn.times(null, syms[1][i], Rn.times(null, rot2, syms[0][j]));
//				System.err.println("Matrix is "+Rn.matrixToString(rets[2*i+j]));			
			}
		}
//		for (int k=0;k<6; ++k)	{
//			rets[k+6] = Rn.times(null, dd, Rn.times(null, rets[k], rot));
////			rets[k+6] = Rn.times(null, dd, rets[k]);
////			rets[k+6] = Rn.times(null, rets[k], rot2);
//		}
		return rets;
	}

}

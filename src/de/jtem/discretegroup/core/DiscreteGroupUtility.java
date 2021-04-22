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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.jogl.JOGLConfiguration;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P2;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.LoggingSystem;
import de.jreality.util.SceneGraphUtility;
import de.jreality.util.Secure;
import de.jtem.discretegroup.groups.WallpaperGroup;
import de.jtem.discretegroup.util.WingedEdge;
import de.jtem.discretegroup.util.WingedEdgeUtility;

/**
 * 
 * @author Charles Gunn
 *
 */
public class DiscreteGroupUtility {

	public static String[] genNames = {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p"};
	public static String[] genInvNames = {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P"};

	private static double sz = 5.0;
	protected static double[][] bigSquare = {{sz,sz,1},{-sz,sz,1},{-sz,-sz,1},{sz,-sz,1}};
	static boolean debug = true;
	
	public static String resourceDir = "./resources/"; //"/net/MathVis/Projects/discreteGroup/resources/";
	public static String triangleGroupFiles = "./";
	public static Logger logger = LoggingSystem.getLogger(DiscreteGroup.class); //
	static {
		try {		
			logger.setLevel(Level.FINE);
			System.err.println("Setting logging level to fine");
			System.err.println("Log level is "+logger.getLevel().toString());
			//logger = Logger.getLogger("discreteGroup"); //LoggingSystem.getLogger();		
			//logger.setLevel(Level.INFO);
			String foo = Secure.getProperty("discreteGroup.resourceDir");
			if (foo != null) resourceDir = foo;
			foo = Secure.getProperty("discreteGroup.maxNumElements");
			if (foo != null) DiscreteGroupSimpleConstraint.globalMaxNumberElements = Integer.parseInt(foo);
			foo = Secure.getProperty("triangleGroupFiles");
			if (foo != null) triangleGroupFiles = foo;
		} catch(SecurityException se)	{
			JOGLConfiguration.theLog.log(Level.WARNING,"Security exception in setting configuration options",se);
		}
		
	}
	
	private static void printMatrixProducts(WingedEdge dd)	{
//		for (Face f1 : dd.getFaceList())	{
//			DiscreteGroupElement dge1 = (DiscreteGroupElement) f1.source;
//			for (Face f2 : dd.getFaceList())	{
//				DiscreteGroupElement dge2 = (DiscreteGroupElement) f2.source;
//				double[] prod = Rn.times(null, dge1.getArray(), dge2.getArray());
//				if (Rn.isIdentityMatrix(prod, 10E-6))
//					System.err.println("Matching elements "+dge1.getWord()+" "+dge2.getWord());
//			}
//		}
	}
	
	/**
	 * Calculate the point within the dirichlet domain represented by the WingedEdge instance <i>dd</i>
	 * which is equivalent under a group element to the point <i>point</i>.
	 * @return
	 */
	public static double[] getCanonicalRepresentative(WingedEdge dd, double[] point, double tol)	{
		return getCanonicalRepresentative(dd, null, point, tol);
	}
	
	public static double[] getCanonicalRepresentative(WingedEdge dd, DiscreteGroupElement dge, double[] point, double tol)	{
		double[] tpoint = point.clone();
		List<WingedEdge.Face> out = WingedEdgeUtility.pointLiesOutsideFace(dd, point, tol, 1);
		while (out.size() > 0 && out.get(0) instanceof WingedEdge.Face)		{
			WingedEdge.Face face = (WingedEdge.Face) out.get(0);
			DiscreteGroupElement t = (DiscreteGroupElement) face.source;
			if (dge != null) dge.multiplyOnRight(t);
			tpoint = Rn.matrixTimesVector(null, Rn.inverse(null, t.getArray()), tpoint);
			System.err.println("tpoint = "+Rn.toString(tpoint));
			out = WingedEdgeUtility.pointLiesOutsideFace(dd, tpoint, tol, 1);			
		}
		if (dge != null) {
			double[] tpoint2 = Rn.matrixTimesVector(null, Rn.inverse(null, dge.getArray()), point);
			System.err.println("tpoint diff ="+Rn.toString(Rn.subtract(null, tpoint, tpoint2)));
		}
		return tpoint;
	}

	// a slightly different version written two years later after the previous (which was then forgotten)
	public static DiscreteGroupElement getCanonicalRepresentative(double[] canpt, double[] pt, WingedEdge dirdom, DiscreteGroup dg)	{
		DiscreteGroupElement dge = new DiscreteGroupElement();
		List<WingedEdge.Face> out = WingedEdgeUtility.pointLiesOutsideFace(dirdom, pt, .001, 1);
		double[] tmp = pt.clone();
		int count = 0;
		while (out.size() > 0)	{
			WingedEdge.Face face = (WingedEdge.Face) out.get(0);
			DiscreteGroupElement t = (DiscreteGroupElement) face.source;
			DiscreteGroupElement inverse = t.getInverse();
			dge.multiplyOnLeft(inverse);
//			System.err.println("dge = "+inverse.getWord());
			Rn.matrixTimesVector(tmp, dge.getArray(), pt);
//			System.err.println("Point = "+Rn.toString(tmp));
			out = WingedEdgeUtility.pointLiesOutsideFace(dirdom, tmp, .001, 1);
			count++;
		}
		// overwrite the contents of pt with the transformed "canonical" point
		Rn.matrixTimesVector(canpt, dge.getArray(), pt);
		System.err.println("final pt = "+Rn.toString(canpt)+" count = "+count);
		return dge;
	}
	public static int badcount = 0;
	// a slightly different version written two years later after the previous (which was then forgotten)
	public static double[] getCanonicalRepresentative2(double[] canpt, double[] pt, DiscreteGroupElement dge,
			IndexedFaceSet fd, WallpaperGroup dg)	{
		if (dge == null) dge = new DiscreteGroupElement();
		else {
			dge.setWord("");
			dge.setArray(Rn.identityMatrix(4));
		}
		double[][] polygon = fd.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		double[][] edgeIds = fd.getVertexAttributes(Attribute.attributeForName("edgeIds")).toDoubleArrayArray(null);
		String[] edgeIdsNames = fd.getVertexAttributes(Attribute.attributeForName("edgeIdsWords")).toStringArray(null);
		boolean[] open = (boolean[])  fd.getGeometryAttributes("open");
		
		int edge = -1;
		if (pt.length == 3) pt = Pn.homogenize(null, pt);
		double[] tmp4 = pt.clone(), tmp = {tmp4[0], tmp4[1], tmp4[3]};
		int count = 0;
		while ((edge = P2.getFirstOutsideEdge(polygon, null, tmp)) != -1)	{
			double[] mat = edgeIds[edge];
			DiscreteGroupElement t = new DiscreteGroupElement(Pn.EUCLIDEAN, mat, edgeIdsNames[edge]);
			DiscreteGroupElement inverse = t.getInverse();
			dge.multiplyOnLeft(inverse);
//			System.err.println("dge = "+dge.getWord());
			Rn.matrixTimesVector(tmp4, dge.getArray(), pt);
			tmp = new double[]{tmp4[0], tmp4[1], tmp4[3]};
			count++;				
			if ( (count % 50) == 0) {
				System.err.println("Point = "+Rn.toString(pt));
				System.err.println("TPoint = "+Rn.toString(tmp4));
				System.err.println("Matrix = "+Rn.matrixToString(mat));
				badcount++;
				break;
			}
			if (Rn.equals(tmp4, pt, 10E-12)) {
				System.err.println("fixed point: "+Rn.toString(pt));break;
			}
		}
//		if ( count != 0) {
//			System.err.println("Point = "+Rn.toString(pt));
//			System.err.println("TPoint = "+Rn.toString(tmp4));
////			System.err.println("Matrix = "+Rn.matrixToString(mat));
//		}

		// overwrite the contents of pt with the transformed "canonical" point
		canpt = Rn.matrixTimesVector(canpt, dge.getArray(), pt);
//		System.err.println("final pt = "+Rn.toString(canpt)+" count = "+count);
		return canpt;
	}


	/**
	 * Create a new instance of {@link IndexedFaceSet} which represents the orbit of
	 * the given IndexedFaceSet under the action of the group <i>dg</i>.
	 * @param dg
	 * @param ifs
	 * @return
	 */public static IndexedFaceSet actOnIndexedFaceSet(DiscreteGroup dg, IndexedFaceSet ifs)	{
		IndexedFaceSet newifs;
		DiscreteGroupElement[] elementList = dg.getElementList();
		int numElements = elementList.length;
		int numVerts = ifs.getNumPoints();
		int vsize = GeometryUtility.getVectorLength(ifs);
		double[][] oldVerts = ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		double[][] newVerts = new double[numElements*numVerts][vsize];
		int[][] oldIndices = ifs.getFaceAttributes(Attribute.INDICES).toIntArrayArray(null);
		int numFaces = oldIndices.length;
		int[][] newIndices = new int[numElements*numFaces][];
		double[][] nfc = null;
		DataList fcolors = ifs.getFaceAttributes(Attribute.COLORS);
		double[][] ofc = fcolors.toDoubleArrayArray(null);
		int ll = ofc[0].length;
		if (fcolors!= null)	nfc = new double[numElements*numFaces][ll];
		for (int i = 0; i<numElements; ++i)	{
			for (int j = 0; j<numVerts; ++j)	{
				Rn.matrixTimesVector(newVerts[i*numVerts+j], elementList[i].getArray(), oldVerts[j]);
			}
			for (int j = 0; j<numFaces; ++j)	{
				newIndices[i*numFaces + j] = oldIndices[j].clone();
				for (int k = 0; k<oldIndices[j].length; ++k)	{
					newIndices[i*numFaces+j][k] += i*numVerts;
				}
				if (nfc != null)	System.arraycopy(ofc[j],0,nfc[i*numFaces+j],0,ll);
			}
		}
		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		ifsf.setVertexCount(newVerts.length);
		ifsf.setFaceCount(newIndices.length);
		ifsf.setVertexCoordinates(newVerts);
		ifsf.setFaceIndices(newIndices);
		ifsf.setGenerateEdgesFromFaces(true);
		ifsf.setGenerateFaceNormals(true);
		if (nfc != null) ifsf.setFaceColors(nfc);
		ifsf.update();
		newifs = ifsf.getIndexedFaceSet(); //IndexedFaceSetUtility.createIndexedFaceSetFrom(newIndices, newVerts,null,null, null, nfc);
		return newifs;
	}

	/**
	 * Filter out and return as an array, those elements of the input list <i>list</i> which
	 * satisfy the constraint <i>dgc</i>.
	 * @param dgc
	 * @param list
	 * @return
	 */
	 public static DiscreteGroupElement[] applyConstraint(DiscreteGroupConstraint dgc, DiscreteGroupElement[] list)	{
		if (dgc == null) return list;
		int n = list.length;
		ArrayList<DiscreteGroupElement> al = new ArrayList<DiscreteGroupElement>();
		int count = 0;
		int max = dgc.getMaxNumberElements();
		for (int i = 0; i<n; ++i)	{
			if (dgc.acceptElement(list[i])) {count++; al.add(list[i]); }
			if (count > max) break;
		}
		System.err.println("`dge accepted "+count+" elements");
		return al.toArray(new DiscreteGroupElement[al.size()]);
	}

	/**
	 * Bit of a hack: process the scene graph component's children; first create a {@link DiscreteGroupElement}
	 * which contains the transformation of the child, and set its <i>word</i> to the name 
	 * of the transformation. Then set the component's visibility based on whether the
	 * constraint accepts this dge.
	 * @param dgc
	 * @param sgc
	 * @param metric
	 */
	 public static void applyConstraint(DiscreteGroupConstraint dgc, SceneGraphComponent sgc, int metric)	{
		int n = sgc.getChildComponentCount();
		int valid = 0;
		int max = dgc.getMaxNumberElements();
		for (int i = 0; i<n; ++i)	{
			SceneGraphComponent child =  sgc.getChildComponent(i);
			Transformation tform = child.getTransformation();
			DiscreteGroupElement dge = new DiscreteGroupElement(metric, tform.getMatrix(), tform.getName());
			if (dgc.acceptElement(dge)) {valid++; child.setVisible(true);}
			else child.setVisible(false);
			if (valid > max) break;
		}
		System.err.println("SG applyConstraint: valid count: "+valid);
	}

	static double[][] el = {{0,0,0},{1,0,0},{1,.2,0},{.2,.2,0},{.2,1.618,0},{0,1.618,0}};
	public static SceneGraphComponent getElKit()	{
		SceneGraphComponent elkit = SceneGraphUtility.createFullSceneGraphComponent("El Kit");
		MatrixBuilder.euclidean().translate(.15,.15,.01).scale(.15).assignTo(elkit);
//		elkit.getTransformation().setStretch(.15);
//		elkit.getTransformation().setTranslation(.15,.15,.01);
		Appearance ap = elkit.getAppearance();
		ap.setAttribute(CommonAttributes.FACE_DRAW, true);
		ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		ap.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, new Color(30, 100, 30));
		elkit.setGeometry(IndexedFaceSetUtility.constructPolygon(el));
		return elkit;
	}
	
	/**
	 * @deprecated
	 * @param geometry
	 * @param sgc
	 * @return
	 */
	public static SceneGraphComponent collectGeometry(List<Geometry> geometry, SceneGraphComponent sgc) {
		if (sgc ==null) sgc = new SceneGraphComponent();
		else SceneGraphUtility.removeChildren(sgc);
		for (int i = 0; i < geometry.size(); ++i)	{
			Object child = geometry.get(i);
			if (child instanceof Geometry)	{
				SceneGraphComponent gc = new SceneGraphComponent();
				gc.setGeometry(geometry.get(i));
				sgc.addChild(gc);
			} 
			else if (child instanceof SceneGraphComponent) sgc.addChild((SceneGraphComponent) child);
		}
		return sgc;
	}

	/**
	 * A convenience method which sorts an array of DiscreteGroupElement's based on the
	 * distance which it moves the origin.
	 * @param list
	 * @param sig
	 */
	public static void sort(DiscreteGroupElement[] list, final int sig)	{
		sort(list, P3.originP3, sig);
	}
	
	public static void sort(DiscreteGroupElement[] list, double[] center, final int sig) {
		Comparator<DiscreteGroupElement> comp = new Comparator<DiscreteGroupElement>() {

			public int compare(DiscreteGroupElement o1, DiscreteGroupElement o2) {
				double d1 = distanceOriginMoved(sig, o1);
				double d2 = distanceOriginMoved(sig, o2);
				if (d1 < d2) return -1;
				if (d1 == d2) return 0;
				return 1;
			}

		};
		Arrays.sort(list, comp);
		for (int i = 0; i<list.length; ++i)	{
			if (i > 200) break;
			double d = distanceOriginMoved(sig, list[i]);
//			if (d == 0) System.err.println("d = 0 "+Rn.matrixToString(list[i].getMatrix()));
		}
	}
	
	static protected double distanceOriginMoved(final int sig, DiscreteGroupElement o1) {
		double[] m = o1.getArray();
		double[] v1 = {m[3],m[7],m[11],m[15]};
		double d1 = Pn.distanceBetween(P3.originP3, v1, sig);
		return d1;
	}
	static protected  double distancePointMoved(final int sig, double[] point,
			DiscreteGroupElement o1) {
		double[] v1 = Rn.times(null, o1.getArray(), point);
		double d1 = Pn.distanceBetween(point, v1, sig);
		return d1;
	}
	
	/**
	 * Creates the 4x4 matrix associated to the given word in the generators.
	 * @param gens
	 * @param word
	 * @return
	 */
	public static DiscreteGroupElement getElementForWord(DiscreteGroupElement[] gens, String word)	{
		
		HashMap<String, DiscreteGroupElement> nameTable = new HashMap<String, DiscreteGroupElement>();
		for (DiscreteGroupElement dge : gens)	{
			nameTable.put(dge.getWord(), dge);
		}
		DiscreteGroupElement ret = new DiscreteGroupElement();
		int n = word.length();
		Matrix m = new Matrix();
		for (int i = 0; i<n; ++i)	{
			String onechar = word.substring(i, i+1);
			DiscreteGroupElement gen = nameTable.get(onechar);
			m.multiplyOnRight(gen.getArray());
		}
//		System.err.println("word = "+ret.getWord());
		ret.setArray(m.getArray());
		ret.setWord(word);
		return ret;
	}

	public static DiscreteGroupElement[] generateElements(DiscreteGroup dg, DiscreteGroupConstraint constraint, List<String> dupList)	{
		logger.log(Level.FINE,"generate elements");                                                                                     
		DiscreteGroupElement[] generators = dg.getGenerators();
		if (generators == null) {
			DiscreteGroupUtility.logger.log(Level.WARNING,"No generators");
			return dg.elementList;
		}
		if (constraint == null) {
			DiscreteGroupUtility.logger.log(Level.FINE,"Using global default constraint");
			constraint = DiscreteGroupSimpleConstraint.defaultConstraint;
		}  else 
			constraint.update();
		DiscreteGroupElement[] elementList;
		int metric = dg.getMetric();
		boolean isProjective = dg.isProjective();
		FiniteStateAutomaton fsa = dg.getFsa();
		Vector<DiscreteGroupElement> biglist = new Vector<DiscreteGroupElement>();
		DiscreteGroupElement dge = new DiscreteGroupElement();
		dge.setWord("");
		biglist.add(dge);
		int length = 1,
			oldLength = 0,
			old2Length = 0;
		double[] mat = new double[16];
		DiscreteGroupElement dgen = new DiscreteGroupElement(), dgen2 = null;
		dgen.setMetric(metric);
		int maxNumberElements = constraint.getMaxNumberElements();
		while(length > oldLength && length < maxNumberElements)	{
			LoggingSystem.getLogger(DiscreteGroup.class).finer("element list length is "+length);
			oldLength = length;
			for (int i = old2Length; i<oldLength; ++i)	{		// multiply by generators
				dge = biglist.get(i);
				for (int j = 0; j<generators.length; ++j)	{
					if (trivialDup(dge.getWord(), generators[j].getWord())) {
						continue;
					}
					dgen.setWord(dge.getWord()+generators[j].getWord());
					dgen.setColorIndex(dge.getColorIndex()+generators[j].getColorIndex());
					if (fsa != null && !fsa.accepts(dgen.getWord())) continue;
					Rn.times(mat, dge.getArray(), generators[j].getArray());
					dgen.setArray(mat);
					if (dg.isProjective() && mat[15] < 0) Rn.times(mat, -1, mat);
//					if (!dg.isFinite() && constraint != null && !constraint.acceptElement(dgen)) continue;
					if (constraint != null && !constraint.acceptElement(dgen)) continue;
					if (fsa == null || dupList != null) {
						boolean dup = isDuplicate(dgen,biglist, dg.isProjective(), false);
						if (dup)	{
							if (dupList != null)	{
								dupList.add(dgen.getWord()+invertWord(dg, latestDup.getWord()));							
							}
							continue;		
						}
					}
					dgen2 = new DiscreteGroupElement(dgen);
//					DefaultMatrixSupport.getSharedInstance().storeAsDefault(dgen2);
					biglist.add(dgen2);
					length = biglist.size();
					if (length >= maxNumberElements) break;
				}
				if (length >= maxNumberElements) break;
			}
			if (length >= maxNumberElements) break;
			length = biglist.size();
			old2Length = oldLength;
		}
		elementList = biglist.toArray(new DiscreteGroupElement[biglist.size()]);
		logger.log(Level.FINE,"Created group with "+length+" elements");                                                                                     
		dg.setHasChanged(false);
		return elementList;
	}

	public static String invertWord(DiscreteGroup dg, String word) {
		char[] thisWord = word.toCharArray();
		int n = thisWord.length;
		char[] invWord = new char[n];
		for (int i = 0; i<n; ++i)	{
//			char tc = thisWord[i];
			String oneLetter = word.substring(i, i+1);
			String inverseLetter = dg.getGeneratorInverseWord(oneLetter);
			invWord[n-i-1] = inverseLetter.charAt(0);
//			if (Character.isLowerCase(tc))	invWord[n-i-1] = Character.toUpperCase(tc);
//			else							invWord[n-i-1] = Character.toLowerCase(tc);
		}
		return new String(invWord);
	}
	
	public static String[] getDuplicates(DiscreteGroup dg, DiscreteGroupConstraint dgc)	 {
		ArrayList<String> dupList = new ArrayList<String>();
		generateElements(dg, dgc, dupList);
		String[] dupWords = new String[dupList.size()];
		return dupList.toArray(dupWords);
	}

	public static DiscreteGroupElement[] generateElements(DiscreteGroup dg, DiscreteGroupConstraint constraint)	{
		return generateElements(dg, constraint, null);
	}
	
	static boolean trivialDup(String word, String word2) {
		int n = word.length();
		if (n==0) return false;
		if (!word.endsWith(word2) )	{
			if (word.endsWith(word2.toLowerCase()) ||
					word.endsWith(word2.toUpperCase() )) {
				//System.err.println("Trivial dup "+word+" "+word2);
				return true;
			}
		}
		return false;
	}
    static DiscreteGroupElement latestDup = null;
	static boolean isDuplicate(DiscreteGroupElement dgex, Vector<DiscreteGroupElement> list, boolean isProjective, boolean printDiff)	{
		double[] inv = new double[16];
		double[] tmp = new double[16];
		double[] mat = dgex.getArray();
		Rn.inverse(inv, mat);
		int length = list.size();
		//System.err.println("In isDuplicate()");
		for (int i = 0; i< length; ++i)	{
			DiscreteGroupElement dge = list.get(i);
			Rn.times(tmp, inv, dge.getArray());
			if (Rn.isIdentityMatrix(tmp, 10E-4)
					|| (isProjective && Rn.isIdentityMatrix(
							Rn.times(tmp, -1, tmp), 10E-4))
					)  { 
				if (printDiff ) 
					DiscreteGroupUtility.logger.log(Level.INFO,dge.getWord()+" "+dgex.getWord());
				latestDup = dge;
					 //System.err.println(dge.getWord()+" "+dgex.getWord());
				return true;
			}
			if (isProjective) {
				
			}
		}
		return false;
	}

	public static DiscreteGroupElement elementFromWord(DiscreteGroup dg, String word)	{
		if (dg.genTable == null) dg.buildGeneratorHashTable();
		DiscreteGroupElement dge = new DiscreteGroupElement();
		dge.setWord(word);
		Matrix m = new Matrix();
		for (int i = 0; i< word.length(); ++i)	{
			String g = word.substring(i,i+1);
			//System.err.println("this letter:"+g);
			DiscreteGroupElement gen =dg.genTable.get(g);
			if (gen != null)	m.multiplyOnLeft(gen.getArray());
		}
		dge.setArray(m.getArray());
		return dge;
	}

	public static void addWordLabels(WingedEdge dirDom) {
		int n = dirDom.getNumFaces();
		String[] labels = new String[n];
		for (int i = 0; i<n;++i)	{
			labels[i] = ((DiscreteGroupElement) dirDom.getFaceList().get(i).source).getWord();
		}
		dirDom.setFaceAttributes(Attribute.LABELS, StorageModel.STRING_ARRAY.createReadOnly(labels));
	}

	/**
	 * A simple example group
	 * @return
	 */
	public static DiscreteGroup Star222()	{
		DiscreteGroup dg = new DiscreteGroup();
		dg.setMetric(Pn.EUCLIDEAN);	// only indirectly used, when creating various sorts of geometry associated to the group
		dg.setDimension(3);			// ditto
		dg.setFinite(true);			// this is a 'hint' that can help optimize the group element generation
		// create the generators: in this case reflections in the three coordinate axes.
		DiscreteGroupElement[] gens = new DiscreteGroupElement[3];
		double[] xplane = {1,0,0,0},
			yplane = {0,1,0,0},
			zplane = {0,0,1,0};
		gens[0] = new DiscreteGroupElement( Pn.EUCLIDEAN, MatrixBuilder.euclidean().reflect(xplane).getArray(), "x");
		gens[1] = new DiscreteGroupElement( Pn.EUCLIDEAN, MatrixBuilder.euclidean().reflect(yplane).getArray(), "y");
		gens[2] = new DiscreteGroupElement( Pn.EUCLIDEAN, MatrixBuilder.euclidean().reflect(zplane).getArray(), "z");
		dg.setGenerators(gens);
		// now generate the group elements, in this simple case we get them all without having to specify anything
		dg.update();
		return dg;
	}
	
	/**
	 * Returns the trivial group consisting of the identity element
	 * @return
	 */
	public static DiscreteGroup trivialGroup()	{
		DiscreteGroup dg = new DiscreteGroup();
		dg.setMetric(Pn.EUCLIDEAN);	// only indirectly used, when creating various sorts of geometry associated to the group
		dg.setDimension(3);			// ditto
		dg.setFinite(true);			// this is a 'hint' that can help optimize the group element generation
		dg.setName("trivial group");
		DiscreteGroupElement id = new DiscreteGroupElement();
		id.setWord("");
		id.setArray(Rn.identityMatrix(4));
		dg.setElementList(new DiscreteGroupElement[]{id});
		return dg;
	}
	
	public static void conjugate(DiscreteGroup dg, Matrix conj)	{
		DiscreteGroupElement[] gens = dg.getGenerators();
		if (gens != null)	{
			for (int i = 0; i<gens.length; ++i)	{
				double[] tmp = Rn.conjugateByMatrix(null, gens[i].getArray(), conj.getArray());
				gens[i].setArray(tmp);
			}
			dg.setGenerators(gens);
			dg.update();
		}
		else {
			DiscreteGroupElement[] ellist = dg.getElementList();
			for (int i = 0; i<ellist.length; ++i)	{
				double[] tmp = Rn.conjugateByMatrix(null, ellist[i].getArray(), conj.getArray());
				ellist[i].setArray(tmp);
			}
			dg.setElementList(ellist);
		}
	}
}

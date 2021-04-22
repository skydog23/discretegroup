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
import java.util.logging.Level;

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.discretegroup.core.DiscreteGroupUtility;
import de.jtem.discretegroup.core.FiniteStateAutomaton;
import de.jtem.discretegroup.util.WingedEdge;


/**
 * @author gunn
 *
 */
public class TriangleGroup extends DiscreteGroup {
	
	protected boolean mirrorGroup = false;
	protected int p, q, r;
	protected double[][] vertices;
	protected double[][] reflectionPlanes;
	protected double[] weights;
	protected boolean constrained = false;
	
	static String[] names = {"233","*233","234","*234","3*2","235","*235","236","*236","244","*244","237", "*237","245","*245","224","*224","*225","225","*226","226","*228","228"};
	public static String[] trinames = {"*233","*234","*235","233","234","235"};
	public static double[][] faceColors;
	
	static {
		faceColors = new double[3][];
		faceColors[1] = new double[]{1,.3,.3};
		faceColors[0] = new double[]{1,.9,0};
		faceColors[2] = new double[]{.5,.6,1};
	}
	public static String[] getNames()	{
		return names;
	}
		/**
	 * Aarg! getting this thing initialized properly before the call to 
	 * calculateGenerators() is not easy!
	 */
	public static TriangleGroup instanceOf(int ip, int iq, int ir, boolean b, String name) {
		TriangleGroup tg = new TriangleGroup();
		tg.p = ip;
		tg.q = iq;
		tg.r = ir;
		tg.mirrorGroup = b;
		tg.vertices = new double[3][4];
		for (int i =0;i<3;++i) tg.vertices[i][3] = 1.0;
		tg.reflectionPlanes = new double[3][];
		tg.weights = new double[3];
		tg.dimension = 2;
		tg.setName(name);
		tg.update();
		return tg;
	}
	
	static Hashtable<String,Integer> nameTable = new Hashtable<String,Integer>();
	static {
		for (int i =0; i<names.length; ++i)	{
			nameTable.put(names[i], new Integer(i));
			}
	}

		
	public static TriangleGroup instanceOfGroup(String name) {	
		String[] atoms = null;
		if (name == "3*2") return new PointGroup3S2();
		if (name.indexOf(" ") != -1)	{
			//Pattern p = new Pattern();
			atoms = name.split(" ");
			if (debug) for (int i = 0; i<atoms.length; ++i)	{
				DiscreteGroupUtility.logger.log(Level.FINE,atoms[i]);
			}
		} else {
			atoms = new String[name.length()];
			for (int i =0; i<name.length(); ++i) {
				atoms[i] = name.substring(i,i+1);
			}
		}
		if (atoms.length < 3 || atoms.length > 4)	{
			throw new IllegalArgumentException("Invalid name for triangle group");
		}
		int ip,iq,ir;
		boolean m = false;
		int index = 0;
		if (atoms[0].charAt(0) == '*')	{m = true; index++; }
		ip = Integer.parseInt(atoms[index]);
		iq = Integer.parseInt(atoms[index+1]);
		ir = Integer.parseInt(atoms[index+2]);
		TriangleGroup tg = instanceOf(ip,iq,ir,m, name);
		tg.setName(name);
		return tg;
	}
	
	protected static double[] swapZW = {1,0,0,0,   0,1,0,0,   0,0,0,1, 0,0,1,0};
	public TriangleGroup convertToProjective()	{
		if ( !(metric == Pn.EUCLIDEAN && dimension == 3)) return this;
		TriangleGroup tg = instanceOf(p, q, r, mirrorGroup, name);
		return _convertToProjective(tg);
	}
	protected TriangleGroup _convertToProjective(TriangleGroup tg) {
		int n = tg.generators.length;
		for (int i = 0; i<n; ++i)	{
			tg.generators[i].setArray(Rn.conjugateByMatrix(null, tg.generators[i].getArray(), swapZW));
		}
		
		for (int i = 0; i<3; ++i)	{
			tg.vertices[i][3] = 0.0;
			Rn.matrixTimesVector(tg.vertices[i], swapZW, tg.vertices[i]);
			Pn.dehomogenize(tg.vertices[i], tg.vertices[i]);
			if ( !(tg instanceof PointGroup3S2)) Rn.matrixTimesVector(tg.reflectionPlanes[i], swapZW, tg.reflectionPlanes[i]);
		}
		System.err.println("transformed points = "+Rn.toString(tg.vertices));
		tg.metric = Pn.ELLIPTIC;
		tg.dimension = 2;
		tg.setChanged(true);
		tg.update();
		return tg;
	}

	/* (non-Javadoc)
	 * @see charlesgunn.gv2.DiscreteGroup#calculateGenerators()
	 */
	public void calculateGenerators() {
		double a = Math.PI / p;
		double b = Math.PI / q;
		double c = Math.PI / r;
		if (p!=2 ) { //|| a+b+c <= Math.PI) {
			DiscreteGroupUtility.logger.log(Level.WARNING,"Not a right-angled triangle, not implemented");
			return;
		}
		
		double[][] reflections = new double[3][];
		if (a+b+c > Math.PI)	{
			// solve the spherical triangle
			metric = Pn.EUCLIDEAN;		// TODO distinguish this from "real" euclidean. it's 2D elliptic really
			dimension = 3;			// consider this as a point group in E3.
			isFinite = true;
			double A = Math.acos((Math.cos(a)+Math.cos(b)*Math.cos(c))/(Math.sin(b)*Math.sin(c)));
			double B = Math.acos((Math.cos(b)+Math.cos(a)*Math.cos(c))/(Math.sin(a)*Math.sin(c)));
			double C = Math.acos((Math.cos(c)+Math.cos(b)*Math.cos(a))/(Math.sin(b)*Math.sin(a)));
			double f = 180.0/Math.PI;
			System.err.println("Angles are "+f*A+" "+f*B+" "+f*C);
			vertices[0][0] = vertices[0][1] = 0.0; vertices[0][2] = 1.0;
			
			vertices[1][0] = 0.0;
			vertices[1][1] = Math.sin(C);
			vertices[1][2] = Math.cos(C);
			
			vertices[2][0] = Math.sin(B);
			vertices[2][1] = 0.0;
			vertices[2][2] = Math.cos(B);
			DiscreteGroupUtility.logger.log(Level.FINE,"Angles:   "+A+"  "+B+"  "+C);
			for (int i = 0; i<3; ++i)	{
				// could also use Pn.planeFromPoints(...) here
				//Rn.crossProduct(normals[i], vertices[(i+1)%3],vertices[(i+2)%3]);
				//double[] hnormal = Pn.homogenize(null, normals[i]);
				//hnormal[3] = 0.0;
				reflectionPlanes[i] = P3.planeFromPoints(null, P3.originP3, vertices[(i+1)%3], vertices[(i+2)%3]);
//				System.err.println("Reflection plane is "+Rn.toString(reflectionPlanes[i]));
				reflections[i] =P3.makeReflectionMatrix(null, reflectionPlanes[i], Pn.EUCLIDEAN);
			}
		} else if (Math.abs(a+b+c - Math.PI) < .00001)	{
			metric = Pn.EUCLIDEAN;		// TODO distinguish this from "real" euclidean. it's 2D elliptic really
			dimension = 2;			// consider this as a point group in E3.
			double B = Math.cos(b);
			double C = Math.cos(c);
			vertices[0][0] = C;
			vertices[0][1] = 0;
			vertices[0][2] = 0.0;

			vertices[1][0] = C;
			vertices[1][1] = B; 
			vertices[1][2] = 0.0;

			vertices[2][0] = 0;
			vertices[2][1] = 0.0;
			vertices[2][2] = 0;

			double[] plane = new double[4];
			// Order of reflection is chosen to allow us to use existing FSA P6M.wa, P4M.wa
			// reflect in hypotenuse
			plane[0] = B; plane[1] = -C; plane[2] = 0d; plane[3] = 0;
			reflections[0] = P3.makeReflectionMatrix(null, plane, Pn.EUCLIDEAN);
			// reflect in y = 0
			plane[0] = 0; plane[1] = 1; plane[2] = 0d; plane[3] = 0d;
			reflections[1] = P3.makeReflectionMatrix(null, plane, Pn.EUCLIDEAN);
			// reflect in x==0
			plane[0] = 1d; plane[1] = 0d; plane[2] = 0d; plane[3] = -C;
			reflections[2] = P3.makeReflectionMatrix(null, plane, Pn.EUCLIDEAN);
			setConstraint(new DiscreteGroupSimpleConstraint(12.0,12));
			if (name.equals("*236"))	{
				FiniteStateAutomaton fsa = new FiniteStateAutomaton("m236.wa");
				setFsa(fsa);
			}
			if (name.equals("*244"))	{
				FiniteStateAutomaton fsa = new FiniteStateAutomaton("m244.wa");
				setFsa(fsa);
			}
			
		} 	 else {		// hyperbolic
			// this triangle is not positioned in the same way as the elliptic or euclidean one (alas)
			if (name.equals("*237"))	{
				FiniteStateAutomaton fsa = new FiniteStateAutomaton("m237.wa");
				setFsa(fsa);
				System.err.println("Got fsa for *237"+(getFsa() != null));
			}
			metric = Pn.HYPERBOLIC;
			dimension = 2;
			isFinite = false;
			double A = Pn.acosh((Math.cos(a)+Math.cos(b)*Math.cos(c))/(Math.sin(b)*Math.sin(c)));
			double B = Pn.acosh((Math.cos(b)+Math.cos(a)*Math.cos(c))/(Math.sin(a)*Math.sin(c)));
			double C = Pn.acosh((Math.cos(c)+Math.cos(b)*Math.cos(a))/(Math.sin(b)*Math.sin(a)));
			double d = Pn.tanh(B);
			DiscreteGroupUtility.logger.log(Level.FINE,""+d);
			vertices[0][0] = d;
			vertices[0][1] = 0.0;
			vertices[0][2] = 0;
			vertices[1][0] = d;
			vertices[1][1] = d*Math.tan(c);
			vertices[1][2] = 0.0;
			vertices[2][0] = vertices[2][1] = 0.0; vertices[2][2] = 0.0;
			DiscreteGroupUtility.logger.log(Level.FINE,"Angles:   "+A+"  "+B+"  "+C);
			double[] plane = new double[4];
			plane[0] = vertices[1][1]; plane[1] = -vertices[1][0]; plane[2] = 0d; plane[3] = 0d;
			reflections[0] = P3.makeReflectionMatrix(null, plane, Pn.HYPERBOLIC);
			// reflect in y==0
			plane[0] = 0d; plane[1] = 1d; plane[2] = 0d; plane[3] = 0d;
			reflections[1] = P3.makeReflectionMatrix(null, plane, Pn.HYPERBOLIC);
			// reflect in x==d
			plane[0] = 1d; plane[1] = 0d; plane[2] = 0d; plane[3] = -vertices[0][0];
			reflections[2] = P3.makeReflectionMatrix(null, plane, Pn.HYPERBOLIC);
			setConstraint(new DiscreteGroupSimpleConstraint(12.0,12));
			if (name.equals("*237"))	{
				FiniteStateAutomaton fsa = new FiniteStateAutomaton("m237.wa");
				setFsa(fsa);
			}
		}
		// settle the  business  of the generators
		generators = new DiscreteGroupElement[3];
		for (int i = 0; i<3; ++i)		{
			generators[i] = new DiscreteGroupElement();
			generators[i].setWord(DiscreteGroupUtility.genNames[i]);
			if (mirrorGroup)	generators[i].setArray(reflections[i]);
			else	 generators[i].setArray(Rn.times(null, reflections[(i+1)%3], reflections[(i+2)%3]));
			//System.out.println("Generator "+generators[i].getName()+"is "+Rn.matrixToString(generators[i].getMatrix()));
		}
	}
	
	public void update()	{
		// solve the spherical triangle
		if (generators == null) calculateGenerators();
		super.update();
	}
	
	public double[][] getTriangle()	{
		return vertices;
	}
	

	/**
	 * @return
	 */
	public boolean isMirrorGroup() {
		return mirrorGroup;
	}

	/**
	 * @param b
	 */
	public void setMirrorGroup(boolean b) {
		mirrorGroup = b;
	}

	/**
	 * @return
	 */
	public double[][] getReflectionPlanes() {
		return reflectionPlanes;
	}
	public void setConstrained(boolean b) {
		constrained = b;
	}
	
	public static WingedEdge getTenPlaneRegion(TriangleGroup tg,  double[] d)	{
//		if (domain == null) 
		WingedEdge domain = new WingedEdge();
//		else domain.reset();
		double[][] vertices = tg.getTriangle();
		double[] centerPoint = tg.getCenterPoint();
		double[][] planes = new double[10][4],
			coordinates = new double[4][4];
		
		Pn.setToLength(centerPoint, centerPoint, 1.0, tg.getMetric());
		// assume they are normalized to length 1
		for (int i = 0; i<3; ++i)	{
			planes[i] = P3.planeFromPoints(planes[i], P3.originP3, vertices[(2+i)%3], vertices[(i+1)%3]);
		}
		for (int i = 0; i<3; ++i)	{
			System.arraycopy(vertices[i], 0, planes[i+3], 0, 3);
			planes[i+3][3] = -Rn.innerProduct(vertices[i], centerPoint, 3);			
		}
		System.arraycopy(centerPoint, 0, planes[6], 0, 3);
		planes[6][3] = -d[3];
		for (int i = 0; i<7; ++i)	{
			domain.cutWithPlane(planes[i], i);
		}
		System.err.println("verts = "+Rn.toString(
				domain.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null)));
		return domain;
	}
	public static  Geometry getSplitFundamentalRegion(TriangleGroup tg, IndexedFaceSet foo)	{
		
			//if (!mirrorGroup) return getDefaultFundamentalRegion();
			if (tg instanceof PointGroup3S2) return ((PointGroup3S2) tg).getSplitFundamentalRegion(foo);
			//	 reflect center in three sides and find midpoint of line joining image to center point
			double[] centerPoint = tg.getCenterPoint();
//			System.err.println("Center point is "+Rn.toString(centerPoint));
			boolean mirrorGroup = tg.isMirrorGroup();
			boolean threeInOne = true;
			double[][] vertices = tg.getTriangle();
			int metric = tg.getMetric();
			int dimension = tg.getDimension();
			DiscreteGroupElement[] generators = tg.getGenerators();
			if (metric == Pn.EUCLIDEAN && dimension == 3)	{
				Pn.setToLength(centerPoint, centerPoint, 1.0, Pn.EUCLIDEAN);
			}
			tg.setCenterPoint(centerPoint);
			
			double[][] images = new double[3][4];
			double[][] quads = new double[12][4];
			int[][] indices;
			System.arraycopy(centerPoint,0, quads[0], 0, 4);
			double factor = (mirrorGroup) ? .5 : 1.0;
			for (int i =0; i<3; ++i)	{	
				double[] temp = new double[4];
				System.arraycopy(vertices[i], 0, quads[i+1], 0, 4);	
//				System.err.println(i+" length: "+Pn.norm(quads[i+1],metric));
				//quads[i+1][3] = 1.0;
				Rn.matrixTimesVector(images[i], generators[i].getArray(), centerPoint);
				Pn.dehomogenize(images[i], images[i]);
				Pn.linearInterpolation(temp, centerPoint, images[i],factor, metric);
				Pn.dehomogenize(quads[i+4], temp);
//				System.err.println(i+" length: "+Pn.norm(quads[i+4],metric));
			}
			quads[7] = quads[0];
			quads[8] = quads[6];
			quads[9] = quads[0];
			quads[10] = quads[5];
			quads[11] = quads[4];
			double[] tll = {0,0}, tlr = {1,0}, tur = {1,1}, tul = {0,1};
			double[][] textures = {
					tll.clone(),
					tur.clone(),
					tur.clone(),
					tur.clone(),
					tlr.clone(),
					tul.clone(),
					tlr.clone(),
					tll.clone(),
					tul.clone(),
					tll.clone(),
					tlr.clone(),
					tul.clone()
			};
			if (!mirrorGroup)	{
				if ( metric == Pn.EUCLIDEAN && dimension == 3)	{
						//for (int i = 0; i<7; ++i)	System.err.println(Rn.toString(quads[i]));
						double[] plane = new double[4];
						Rn.planeParallelToPassingThrough(plane, quads[2], quads[0]);
						P3.lineIntersectPlane(quads[2], P3.originP3, quads[2], plane );
						//Pn.planeFromPoints(plane, quads[4], quads[0], quads[5]);
						Rn.planeParallelToPassingThrough(plane, quads[3], quads[0]);
						P3.lineIntersectPlane(quads[3], P3.originP3, quads[3], plane );
					}  
					indices = new int[3][3];
					indices[0][0] = 0;
					indices[0][1] = 4;
					indices[0][2] = 5;
					
					indices[1][0] = 7;
					indices[1][1] = 10;
					indices[1][2] = 2;
					
					//System.err.println(Rn.toString(plane));
					indices[2][0] = 9;
					indices[2][1] = 6;
					indices[2][2] = 3;
			} else {
				indices = new int[3][4];
				if (metric == Pn.EUCLIDEAN && dimension == 3)	{
					//for (int i = 0; i<7; ++i)	System.err.println(Rn.toString(quads[i]));
					double[] plane = new double[4];
					Rn.planeParallelToPassingThrough(plane, quads[1], quads[0]);
					P3.lineIntersectPlane(quads[1], P3.originP3, quads[1], plane );
					Rn.planeParallelToPassingThrough(plane, quads[2], quads[0]);
					P3.lineIntersectPlane(quads[2], P3.originP3, quads[2], plane );
					Rn.planeParallelToPassingThrough(plane, quads[3], quads[0]);
					P3.lineIntersectPlane(quads[3], P3.originP3, quads[3], plane );
				}
				
				indices[0][0] = 0;
				indices[0][1] = 6;
				indices[0][2] = 1;
				indices[0][3] = 5;
				
				indices[1][0] = 7;
				indices[1][1] = 4;
				indices[1][2] = 2;
				indices[1][3] = 8;
				
				//System.err.println(Rn.toString(plane));
				indices[2][0] = 9;
				indices[2][1] = 10;
				indices[2][2] = 3;
				indices[2][3] = 11;
			}	
			IndexedFaceSetFactory ifsf = null;
//			if (metric == Pn.HYPERBOLIC)	{		// test out conformal model
//				Pn.normalize(quads, quads, Pn.HYPERBOLIC);
//				for (int i = 0; i<quads.length; ++i)	{
//					quads[i][3] += 1.0;
//				}
//			}
			if (foo == null)	{
				ifsf = new IndexedFaceSetFactory();
				ifsf.setMetric(metric);
				ifsf.setVertexCount(quads.length);
				ifsf.setFaceCount(indices.length);
				ifsf.setVertexCoordinates(quads);
				if (threeInOne) {
					double offset = 0.0, third = 1.0/3.0;
					for (int[] face : indices) {
						for (int j : face)	{
							textures[j][0] = offset + third*textures[j][0];
							textures[j][1] = textures[j][1];
//							System.err.println("Texture = "+Rn.toString(textures[j]));
						}
						offset += third;
					}
				}
				ifsf.setVertexTextureCoordinates(textures);
				ifsf.setFaceIndices(indices);
				ifsf.setGenerateEdgesFromFaces(true);
				ifsf.setGenerateFaceNormals(true);
				if (faceColors!= null) ifsf.setFaceColors(faceColors);
				ifsf.update();
				foo = ifsf.getIndexedFaceSet(); 			
				if (!threeInOne)	{
					int[] texunits = {0,1,2};
					foo.setFaceAttributes(Attribute.attributeForName("textureUnit"),StorageModel.INT_ARRAY.createReadOnly(texunits));					
				}
			} else {
				ifsf = (IndexedFaceSetFactory) foo.getGeometryAttributes(GeometryUtility.FACTORY);
				if (ifsf != null)	{
					ifsf.setVertexCoordinates(quads);
					ifsf.update();
				} else {
					foo.setVertexAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(4).createReadOnly(quads));
					IndexedFaceSetUtility.calculateAndSetEdgesFromFaces(foo);
					IndexedFaceSetUtility.calculateAndSetFaceNormals(foo);					
				}
			}
			return foo;
			
		}
	public static  Geometry getSplitFundamentalRegion(TriangleGroup tg)	{
		return TriangleGroup.getSplitFundamentalRegion(tg, null);
	}
	public static Geometry getDefaultFundamentalRegion(TriangleGroup tg)	{
		double[][] vertices = tg.getTriangle();
		if (vertices == null)	return null;
		// TODO figure out why I'm doing this here!
		double[] cp = Rn.average(null, vertices);
		//cp[3]=1.0;
		tg.setCenterPoint(cp);
		IndexedFaceSetFactory ifsf = IndexedFaceSetUtility.constructPolygonFactory(null, vertices, Pn.EUCLIDEAN);
		ifsf.setEdgeCount(3);
		ifsf.setEdgeIndices(new int[][]{{0,1},{1,2},{2,0}});
//		ifsf.setEdgeIndices((int[][]) null);
//		ifsf.setGenerateEdgesFromFaces(true);
		ifsf.update();
		return ifsf.getGeometry();
	}
	@Override
	public Geometry getDefaultFundamentalRegion() {
		return getDefaultFundamentalRegion(this);
	}

}

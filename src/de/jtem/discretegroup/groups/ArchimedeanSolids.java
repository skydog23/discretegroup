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

import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedFaceSet;
import de.jtem.discretegroup.core.DiscreteGroupUtility;
import de.jtem.discretegroup.util.WingedEdge;
import de.jtem.discretegroup.util.WingedEdgeUtility;

public class ArchimedeanSolids {

	public static String[] archnames = {"100", "010","001",  "101", "110", "011","111","snub"}; //,"110"};
	public static String[] archimedeanNames = {"3.3.3","3.3.3.3","3.3.3.3.3","4.4.4","5.5.5","3.4.3.4","3.4.4.4","3.4.5.4",
//	public static String[] archimedeanNames = {"5.5.5","3.3.3","3.3.3.3","3.3.3.3.3","4.4.4","3.4.3.4","3.4.4.4","3.4.5.4",
	"3.5.3.5","3.6.6","3.8.8","3.10.10","4.6.6","4.6.8","4.6.10","5.6.6","3.3.3.3.4","3.3.3.3.5"};
	static Integer[][] lookupFromNames = {{0,1},{1,1},{2,1},{1,2},{2,2},{1,0},{1,5},{2,5},{2,0},
		{0,4},{1,4},{2,4},{0,6},{1,6},{2,6},{2,3},{4,7},{5,7}};
	static Hashtable<String,Integer[]> archTable = new Hashtable<String,Integer[]>();
	static boolean debug = false;
	
	static {
		for (int i = 0; i<lookupFromNames.length; ++i)	{
			archTable.put(ArchimedeanSolids.archimedeanNames[i], lookupFromNames[i]);
		}
	}
	
	public static WingedEdge archimedeanSolid(String archname)	{	
		Integer[] which = ArchimedeanSolids.lookupArchimedeanSolid(archname);
		if (which == null)	{
			throw new IllegalArgumentException("Invalid name for Archimedean solid: "+archname);
		}
		return ArchimedeanSolids.archimedeanSolid(TriangleGroup.trinames[which[0]], ArchimedeanSolids.archnames[which[1]]);
	}

	public static WingedEdge archimedeanSolid(String grpname, String subtype)	{
		TriangleGroup tg = TriangleGroup.instanceOfGroup(grpname);
		ArchimedeanSolids.prepareArchimedeanSolid(tg, subtype);
		IndexedFaceSet foo = (IndexedFaceSet) TriangleGroup.getSplitFundamentalRegion(tg);
		IndexedFaceSet fooall = DiscreteGroupUtility.actOnIndexedFaceSet(tg, foo);
		WingedEdge we = WingedEdgeUtility.convertConvexPolyhedronToWingedEdge(fooall);
		return we;
		//return fooall;
	}

	public static String[] getArchimedeanNames()	{
		return archimedeanNames;
	}

	public static Integer[]  lookupArchimedeanSolid(String archname)	{	
		Integer[] which = archTable.get(archname);
		return which;
	}

	/**
	 * 
	 * @param tg 	The triangle group at work (*233, *234, *235, *236, etc)
	 * @param subtype   Where to put the vertex in the fundamental triangle 
	 * 				Essentially this gives the barycentric coordinates of the vertex
	 * 				7 main possibilities: on vertices, in middle of sides, or in middle
	 * @return
	 */
	public static WingedEdge prepareArchimedeanSolid(TriangleGroup tg, String subtype)	{
		tg.update();
		int metric = tg.getMetric();
		if (subtype.equals("100"))	{
			tg.setCenterPoint( tg.getTriangle()[0]);
		} else if (subtype.equals("001"))	{
			tg.setCenterPoint( tg.getTriangle()[1]);
		}	else if (subtype.equals("010"))	{
			tg.setCenterPoint( tg.getTriangle()[2]);
		} else if (subtype.equals("110"))	{
			double[] mm = Rn.times(null, -1.0, tg.getReflectionPlanes()[0]);
			double[] midplane = Pn.midPlane(null, mm, tg.getReflectionPlanes()[1], metric);
			double[] cp = P3.lineIntersectPlane(null, tg.getTriangle()[0], tg.getTriangle()[1], midplane);
			tg.setCenterPoint(cp);
		} else if (subtype.equals("101"))	{
			double[] mm = Rn.times(null, -1.0, tg.getReflectionPlanes()[0]);
			double[] midplane = Pn.midPlane(null, mm, tg.getReflectionPlanes()[2], metric);
			double[] cp = P3.lineIntersectPlane(null, tg.getTriangle()[0], tg.getTriangle()[2], midplane);
			tg.setCenterPoint(cp);
		} else if (subtype.equals("011"))	{
			double[] mm = Rn.times(null, -1.0, tg.getReflectionPlanes()[1]);
			double[] midplane = Pn.midPlane(null, mm, tg.getReflectionPlanes()[2], metric);
			double[] cp = P3.lineIntersectPlane(null, tg.getTriangle()[1], tg.getTriangle()[2], midplane);
			tg.setCenterPoint(cp);
		} else if (subtype.equals("111"))	{
			double[] mm = Rn.times(null, -1.0, tg.getReflectionPlanes()[1]);
			double[] midplane1 = Pn.midPlane(null,mm, tg.getReflectionPlanes()[2], metric);
			 mm = Rn.times(null, -1.0, tg.getReflectionPlanes()[0]);
			double[] midplane2 = Pn.midPlane(null,mm, tg.getReflectionPlanes()[2], metric);
			double[] plane3 = {0.0,0.0, 1.0, -1.0};
			double[] cp = P3.pointFromPlanes(null, midplane1, midplane2, plane3);
			Pn.dehomogenize(cp,cp);
			Pn.setToLength(cp, cp, 1.0, Pn.EUCLIDEAN);
			tg.setCenterPoint(cp);
		} else if  (subtype.equals("snub"))	{
			//double[] mm = Rn.times(null, -1.0, tg.getReflectionPlanes()[1]);
			// get an initial point
			//double[] midplane = Pn.midPlane(null, mm, tg.getReflectionPlanes()[2], metric);
			//double[] cp = Pn.lineIntersectPlane(null, tg.getTriangle()[1], tg.getTriangle()[2], midplane);
			double[] mm = Rn.times(null, -1.0, tg.getReflectionPlanes()[1]);
			double[] midplane1 = Pn.midPlane(null,mm, tg.getReflectionPlanes()[2], metric);
			 mm = Rn.times(null, -1.0, tg.getReflectionPlanes()[0]);
			double[] midplane2 = Pn.midPlane(null,mm, tg.getReflectionPlanes()[2], metric);
			double[] plane3 = {0.0,0.0, 1.0, -1.0};
			double[] cp = P3.pointFromPlanes(null, midplane1, midplane2, plane3);
			Pn.dehomogenize(cp,cp);
			Pn.setToLength(cp, cp, 1.0, Pn.EUCLIDEAN);
			double[] cp3 = new double[3];
			System.arraycopy(cp, 0, cp3, 0, 3);
			Rn.normalize(cp3,cp3);
			double error = 1.0;
			double[] delta = new double[3];
			double[] e = new double[3];
			double[] d = new double[3];
			double[][] diffs = new double[3][3];
			double[][] images = new double[3][3];
			double fudge = .2;
			int count = 0;
			int[] order = new int[3];
			do
			{
				for (int i = 0; i<3; ++i)	{
					Rn.matrixTimesVector(images[i], tg.getGenerators()[i].getArray(), cp3);
					d[i] = Math.acos(Rn.innerProduct(images[i], cp3));
					Rn.subtract(diffs[i], tg.getTriangle()[i], cp3);
				}
				error = 0.0;
				// Find which distance is the longest
				ArchimedeanSolids.mysort(d, order);
				for (int i = 0; i<3; ++i)	{
					e[i] = d[(i+2)%3] - d[(i+1)%3];
					error += Math.abs(e[i]);
				}
				// move in the direction to equalize the lengths by shortening the longest one
				int biggest = order[2];
				int smallest = order[0];
				double size = 2 * d[biggest] - d[(biggest+1)%3] - d[(biggest+2)%3];
				Rn.times(delta, fudge * size, diffs[biggest]);	
				Rn.add(cp3, delta, cp3);
				size = 2 * d[smallest] - d[(smallest+1)%3] - d[(smallest+2)%3];
				Rn.times(delta, fudge * size, diffs[smallest]);	
				Rn.add(cp3, delta, cp3);
				//Pn.normalize(cp3,cp3,Pn.EUCLIDEAN);
				Rn.normalize(cp3, cp3);
				//System.out.println("Distances: "+Rn.toString(d));
				//System.out.println("Point is: "+Rn.toString(cp3));
				//System.out.println("Error is: "+error);
				count++;
			} while (error > .01 && count < 500);
			System.arraycopy(cp3, 0, cp, 0, 3);
			cp[3] = 1.0;
			tg.setCenterPoint(cp);
			if (debug)	{
				DiscreteGroupUtility.logger.log(Level.INFO,"Point is: "+Rn.toString(cp3));
				DiscreteGroupUtility.logger.log(Level.INFO,"Distances: "+Rn.toString(d));
				DiscreteGroupUtility.logger.log(Level.INFO,"Error is: "+error+" "+count);				
			}
		}
	
		
		IndexedFaceSet foo = (IndexedFaceSet) TriangleGroup.getSplitFundamentalRegion(tg);
		IndexedFaceSet fooall = DiscreteGroupUtility.actOnIndexedFaceSet(tg, foo);
		WingedEdge we = WingedEdgeUtility.convertConvexPolyhedronToWingedEdge(fooall);
		return we;
		//return fooall;
	}

	static void mysort(double[] d, int[] o)	{
		if (d[0] >= d[1] && d[1] >= d[2])	{o[0] = 2; o[1] = 1; o[2] = 0; return;}
		if (d[0] >= d[2] && d[1] >= d[0])	{o[0] = 2; o[1] = 0; o[2] = 1; return;}
		if (d[0] >= d[2] && d[2] >= d[1])	{o[0] = 1; o[1] = 2; o[2] = 0; return;}
		if (d[0] >= d[1] && d[2] >= d[0])	{o[0] = 1; o[1] = 0; o[2] = 2; return;}
		if (d[1] >= d[2] && d[2] >= d[0])	{o[0] = 0; o[1] = 2; o[2] = 1; return;}
		if (d[1] >= d[0] && d[2] >= d[1])	{o[0] = 0; o[1] = 1; o[2] = 2; return;}
	}

}

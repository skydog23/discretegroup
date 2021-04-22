/*
 * Created on Nov 17, 2009
 *
 */
package de.jtem.discretegroup.core;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.math.Matrix;
import de.jreality.math.P2;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedFaceSet;
import de.jtem.discretegroup.groups.WallpaperGroup;
import de.jtem.discretegroup.util.WingedEdge;
import de.jtem.discretegroup.util.WingedEdge.Face;
import de.jtem.discretegroup.util.WingedEdgeUtility;

public class DirichletDomain {

	DiscreteGroup group;
	int orbitSize = 75;
	IndexedFaceSet dirdomIFS;
	private DiscreteGroupElement[] elementList;
	IndexedFaceSetFactory ifsf;
	
	public DirichletDomain(DiscreteGroup dg) {
		group = dg;
		if (dg.getDimension() == 2) {
			 ifsf = new IndexedFaceSetFactory();
			 ifsf.setVertexCount(4);
			 ifsf.setVertexCoordinates(DiscreteGroupUtility.bigSquare);
			 ifsf.setFaceCount(1);
			 ifsf.setFaceIndices(new int[][]{{0,1,2,3}});
			 ifsf.update();
			 
			 dirdomIFS = ifsf.getIndexedFaceSet();
			 dirdomIFS = new IndexedFaceSet();
		}
		else if (dg.getDimension() == 3) dirdomIFS = new WingedEdge(20.0);
		orbitSize = dg.getMaxDirDomOrbitSize();
	}

	public IndexedFaceSet getDirichletDomain()	{
		return dirdomIFS;
	}
	
	boolean doAll3D = true;
	private static double[] zplane1 = {0,0,1,0}, zplane2 = {0,0,1,-.01};
	public void update()	{
		WingedEdge tmpWE = null;
		if (elementList == null) setDirichletDomainOrbit(75);
		int dimension = group.getDimension();
		double[] centerPoint = group.getCenterPoint();
		int metric = group.getMetric();
		int n = elementList.length;
		if (n == 1) return;
		Matrix changeOfBasis = group.getChangeOfBasis();
		double[] cobm = changeOfBasis.getArray();
		double[] cobim = Rn.inverse(null, cobm); //changeOfBasis.getInverse().getMatrix();
		double[] cobCenter = Rn.matrixTimesVector(null, cobm, centerPoint);
		tmpWE = null;
		if (dimension == 2)	{
			if (!doAll3D) {
				double[] orbit = new double[4];
				double[] orbit3 = new double[3];
				double[] centerPoint3 = new double[3];
				double[] pb = new double[3];
				double[][] polygon = new double[4][3];
				for (int i = 0; i<4; ++i)	System.arraycopy(DiscreteGroupUtility.bigSquare[i],0,polygon[i],0,3);
				P2.projectP3ToP2(centerPoint3, cobCenter);
				if (n == 1) return;
				for (int i =1; i<n; ++i)	{
					Rn.matrixTimesVector(orbit, elementList[i].getArray(), centerPoint);
					Rn.matrixTimesVector(orbit, cobm, orbit);
					P2.projectP3ToP2(orbit3,  orbit);
					P2.perpendicularBisector(pb,centerPoint3, orbit3, metric);
					double[][] polygon2 = P2.chopConvexPolygonWithLine(polygon, pb);

					if (polygon2 == null)	{
						DiscreteGroupUtility.logger.log(Level.FINE,"calculateDirichletDomain: Null polygon");
						 break; 
					} 
					polygon = polygon2;
					} 
				if (polygon == null) return;
				// set z-coordinate to 0
				for (int i =0; i<polygon.length; ++i)	polygon[i][2] = 0.0;
				Rn.matrixTimesVector(polygon, cobim, polygon);
				dirdomIFS = IndexedFaceSetUtility.constructPolygon(polygon); //(dirdomIFS,polygon);
//					return dirdomIFS;
			} else {
				// cook up a 3D version of the group from which it's easy to reconstruct
				// the 2D  polygon.
				tmpWE = new WingedEdge(20.0);
				// the face with tag -1 will be the one we extract when we return to 2D
				tmpWE.cutWithPlane(zplane1, -1, null);
				tmpWE.cutWithPlane(zplane2, -2,null);
			}
		}  else if (dimension == 3)	{
			tmpWE = (WingedEdge) dirdomIFS;
			tmpWE.init();
		}
		
		if (tmpWE != null)	{
			double[] orbit = new double[4];
			double[] pb = new double[4];
			tmpWE.setMetric(metric);
//			double[] cobCenter = centerPoint; //Rn.matrixTimesVector(null, changeOfBasis.getMatrix(), centerPoint);
			if (n == 1) return;
			for (int i =1; i<n; ++i)	{
				Rn.matrixTimesVector(orbit, elementList[i].getArray(), centerPoint);
//					Rn.matrixTimesVector(orbit,cobm, orbit);
				if (Pn.distanceBetween(orbit, centerPoint, metric) < 10E-8) {
					System.err.println("calcDD: fixed point!!");
					continue;
//						System.err.println("matrix = "+Rn.matrixToString(elementList[i].getMatrix()));
//						double[] cp2 = new double[4];
//						for (int j=0;j<4;++j)	cp2[j] = centerPoint[j]+.0001*Math.random();
//						Rn.matrixTimesVector(orbit, elementList[i].getMatrix(), cp2);
//						Rn.matrixTimesVector(orbit,cobm, orbit);
				}
				P3.perpendicularBisector(pb,cobCenter, orbit, metric);
				Pn.normalizePlane(pb, pb, metric);
//					System.err.println("perp bis = "+Rn.toString(pb));
				if (!tmpWE.cutWithPlane(pb, i, elementList[i])) continue; //elementList[i].getColorIndex(), elementList[i]);
//					System.err.println("dd: cut with word "+elementList[i].getWord());
				
				if (tmpWE.getNumFaces() == 0) return;
//					log.info("Orbit point"+Rn.toString(orbit));
//					log.info("Perpendicular bisector"+Rn.toString(pb));
//					log.info("i = "+i);
//					log.info("matrix = "+Rn.matrixToString(elementList[i].getMatrix()));
				if (i > group.getGenerators().length) {
					tmpWE.update();
					if (allFacesMatched(tmpWE)) {
						System.err.println("dirdom succeeded after "+i+" iterations.");
						break;
					}
				} 
			}
				tmpWE.update();
//				for (Face f: dd.getFaceList())	{
//					System.err.println("word = "+((DiscreteGroupElement) f.source).getWord());
//				}
//				return dd;
			}
//			return null;
//		}
		if (dimension == 2 && doAll3D)	{
			// find the face with tag -1
			int foo = tmpWE.getFirstFaceWithTag(-1);
			double[][] verts = tmpWE.getFaceWithIndex(foo);
			if (verts == null)	{
				throw new IllegalStateException("No face found with tag -1");
			}
			dirdomIFS = IndexedFaceSetUtility.constructPolygon(verts);
			WallpaperGroup.storeEdgeIds(group, dirdomIFS);
		}
	}
	
	private boolean allFacesMatched(WingedEdge we) {
		List<Face> unmatched = new Vector<Face>();
		unmatched.addAll(we.getFaceList());
		DiscreteGroupElement dge, dge2;
		do {
			Face foo = unmatched.get(0);
			dge = (DiscreteGroupElement) foo.source;
			if (dge == null) return false;
			// check if this element is order 2; then the face is matched with itself
			double[] m = Rn.times(null, dge.getArray(), dge.getArray());
			if (Rn.isIdentityMatrix(m, 10E-8)) {
				unmatched.remove(foo);
			}
			// check all products with other unmatched faces for an inverse
			else for (Face f: we.getFaceList())	 {
				if (!unmatched.contains(f)) continue;
				dge2 = (DiscreteGroupElement) f.source;
				m = Rn.times(null, dge.getArray(), dge2.getArray());
				if (Rn.isIdentityMatrix(m, 10E-8) && isSameFace(we, foo, f)) {
					unmatched.remove(foo);
					unmatched.remove(f);
					break;
				}
			}
			// keep going if we matched this face
			if (!unmatched.contains(foo)) continue;
			// otherwise give up, since this face has no match
//			System.err.println("unmatched face with word "+dge.getWord());
			return false;
		} while(!unmatched.isEmpty());
		return true;
	}

	private boolean isSameFace(WingedEdge we, Face f1, Face f2) {
		WingedEdgeUtility.removeDuplicateVertices(we);
		if (f1.order != f2.order) return false;
		double[][] v0 = IndexedFaceSetUtility.extractVerticesForFace(we, f1.index),
				v1 = IndexedFaceSetUtility.extractVerticesForFace(we, f2.index);
		v1 = Rn.matrixTimesVector(null, ((DiscreteGroupElement)f1.source).getArray(), v1);
		for (int i = 0; i< v0.length; ++i)	{
			boolean matched = false;
			for (int j = 0; j<v1.length; ++j)	{
				double d = Pn.distanceBetween(v0[i], v1[j], group.getMetric());
				if (Double.isNaN(d))	
					throw new IllegalArgumentException("nan");
//				System.err.println(i+" "+j+" "+d);
				if (d < 10E-4) {matched = true; break;}
			}
			if (!matched) return false;
		}
		return true;
	}

	public int getDirichletDomainOrbitSize() {
		return orbitSize;
	}

	public void setDirichletDomainOrbit(int i) {
		orbitSize = i;
		DiscreteGroupSimpleConstraint triv = new DiscreteGroupSimpleConstraint(orbitSize);
		boolean proj= group.isProjective();
		group.setProjective(false);
		elementList = DiscreteGroupUtility.generateElements(group, triv);
		group.setProjective(proj);
	}
	
	public static HashMap< Integer, Integer> getPairedFaces(WingedEdge we)	{
		HashMap map = new HashMap<Integer, Integer>();
		HashMap map2 = new HashMap<String, DiscreteGroupElement>();
		int i = 0;
		for (Face face : we.faceList) {
			DiscreteGroupElement dge = (DiscreteGroupElement) face.source;
			int j = 0;
			for (Face face2 : we.faceList) {
				DiscreteGroupElement dge2 = (DiscreteGroupElement) face2.source;
				if (Rn.isIdentityMatrix(Rn.times(null, dge.getArray(), dge2.getArray()), 10E-8)) {
					map.put(i, j);
//					if (i != j) map.put(j,i);
				}
				j++;
			}
			i++;
		}
		System.err.println("Found "+map.size()+" pairs");
		return map;
	}

}

/*
 * Created on 11 Apr 2025
 *
 */
package de.jtem.discretegroup.spacegroups;

import java.awt.Color;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.scene.IndexedFaceSet;
import de.jtem.discretegroup.core.DiscreteGroup;

public class CubicTetrahedra {

	static double[][] vv = {
			{0,0,0,1},
			{1,0,0,1},
			{1,1,0,1},
			{0,0,1,1},
			{1,-1,0,1},
			{0,0,-1,1}
	};
	
	static int[][] pi = {
			{0,1,2,3},
			{0,4,2,3},
			{5,4,2,3}
	};
	public static Color[] faceColors = {Color.red, Color.blue, Color.green, new Color(255,0,255)};

	static double[][][] mirrors = new double[3][][];
	static final int[][] tris = {{0,1,2},{0,2,3},{0,3,1},{1,3,2}};
	static DiscreteGroup dgRefGrp[] = new DiscreteGroup[3];
	static IndexedFaceSet tetra[] = new IndexedFaceSet[3];
	static {
		for (int i = 0; i< 3; ++i) {
			IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
			ifsf.setVertexCount(4);
			ifsf.setFaceCount(4);
			ifsf.setFaceIndices(tris);
			ifsf.setFaceColors(faceColors);
			ifsf.setGenerateEdgesFromFaces(true);
			ifsf.setGenerateFaceNormals(true);
			double tv[][] = new double[4][];
			for (int j = 0; j<4; ++j)	{
//				int k = pi[i][tris[j][0]],
//					m = pi[i][tris[j][1]],
//					n = pi[i][tris[j][2]];
				tv[j] = vv[pi[i][j]];
//				mirrors[i][j] = P3.planeFromPoints(null, vv[k], vv[m], vv[n]); 
			}
			ifsf.setVertexCoordinates(tv);
			ifsf.update();
			tetra[i] = ifsf.getIndexedFaceSet();
		}
	}
	
	static IndexedFaceSet getTetra(int level) {
		if (level <0) level = 0;
		if (level > 2) level = 2;
		return tetra[level];
	}
	public static void main(String[] args) {
		
	}

}

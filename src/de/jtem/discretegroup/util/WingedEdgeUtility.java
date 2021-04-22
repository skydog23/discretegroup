/*
 * Created on Jan 16, 2012
 *
 */
package de.jtem.discretegroup.util;

import java.util.List;
import java.util.Vector;
import java.util.logging.Level;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.QuadMeshFactory;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.IntArrayArray;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.LoggingSystem;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.util.WingedEdge.Edge;
import de.jtem.discretegroup.util.WingedEdge.Face;
import de.jtem.projgeom.PlueckerLineGeometry;

public class WingedEdgeUtility {

	//	static Color[] notKnotColors ={
	//			new Color(.3f, .5f, 1f),
	//			new Color(.3f, 1f, .5f),
	//			new Color(1f, .2f,.2f),
	//			new Color(.7f, .6f, .6f)
	//	};
		static double[][] borromColors = {{.3f, .5f, 1f},{.3f, 1f, .5f},{1f, .2f,.2f},{.9f, .8f, .8f}};

		static int[][]	edata = {
		  {0, 4,  8,  4,  9,  6, 2, 4},
		  {2, 6,  4, 10,  6, 11, 4, 3},
		  {1, 5,  5,  8,  7,  9, 5, 2},
		  {3, 7, 10,  5, 11,  7, 3, 5},
		  {0, 2,  0,  8,  1, 10, 4, 0},
		  {1, 3,  8,  2, 10,  3, 0, 5},
		  {4, 6,  9,  0, 11,  1, 1, 4},
		  {5, 7,  2,  9,  3, 11, 5, 1},
		  {0, 1,  4,  0,  5,  2, 0, 2},
		  {4, 5,  0,  6,  2,  7, 2, 1},
		  {2, 3,  1,  4,  3,  5, 3, 0},
		  {6, 7,  6,  1,  7,  3, 1, 3}};
		static int[]	fdata = {4, 6, 0, 1, 0, 2};
		static double[][] builtincmap = 	
		{{0.8, 0.1, 0.1, 1.0}, 
		{0.1, 0.65, 0.4, 1.0}, 
		{0.1, 0.1, 0.8, 1.0}, 
		{0.9, 0.6, 0, 1.0}, 
		{0, 0.6, 0.8, 1.0}, 
		{0.5, 0, 0.9, 1.0}, 
		{.7, .15, .1, 1.0},
		{.2, .2, .8, 1.0},
		{.9, .6, .02, 1.0},
		{.1, .3, .8, 1.0},
		{.1, .7, .2, 1.0},
		{.8, .8, .4, 1.0},
		{.7, .7, 0, 1.0},
		{.7, 0, .7, 1.0},
		{0, .7, .7, 1.0},
		{.9, 0, .2, 1.0},
		{.2, .6, 0, 1.0},
		{0, .2, .9, 1.0},
		{.75, .75, .75, 1.0},
		{.8, .4, 0, 1.0},
		{.8, .4, 0, 1.0},
		{0, .4, .8, 1.0},
		{0, .4, .8, 1.0},
		{0, .8, .4, 1.0},
		{0, .8, .4, 1.0},
		{.4, 0, .8, 1.0}};
		static double[] xaxis = {1,0,0};

	//static double[][] profile = {{0,0,0},{1,0,0}};
	public static SceneGraphComponent createTubesOnEdges(WingedEdge we, double radius, int segments, int slices)	{
		int numTubes = we.getNumEdges();
		SceneGraphComponent all = SceneGraphUtility.createFullSceneGraphComponent("all");
		all.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, true);
		all.getAppearance().setAttribute(CommonAttributes.FACE_DRAW, true);
		double[][] profile = new double[segments+1][];
		for (int i = 0; i<numTubes; ++i)	{
			Edge edge = (Edge) we.edgeList.get(i);
			double[] v0 = edge.v0.point;
			double[] v1 = edge.v1.point;
			double distance = Pn.distanceBetween(v0, v1, we.metric);
			double[] trans = P3.makeTranslationMatrix(null, v0, P3.originP3, we.metric);
			double[] v1p = Rn.matrixTimesVector(null, trans, v1);
			Pn.dehomogenize(v1p, v1p);
			double[] rot = P3.makeRotationMatrix(null, v1p, WingedEdgeUtility.xaxis);
			double[] isometry1 = Rn.times(null, rot, trans);
			double[] isometry = Rn.inverse(null, isometry1);
			// prepare profile curve (equidistant curve) above x-axis
			double[] vv0 = {0,0,0,1};
			double[] vv2 = {.99,0,0,1};
			double[] up = {0,.5,0,0};
			double[] up1 = new double[4];
			double[] vv1 = Pn.dragTowards(null, vv0, vv2, distance, we.metric);
			double d2 = Pn.distanceBetween(vv0, vv1, we.metric);
			vv1 = Rn.matrixTimesVector(null, isometry1, v1);
			double[] v = null;
			for (int j = 0; j<=segments; ++j)	{
				double t = ((double) j)/(segments);
				v = Pn.linearInterpolation(null, vv0, vv1, t, we.metric );
				Pn.normalize(v, up1, v, up, we.metric);
				profile[j] = Pn.dragTangentVector(null,null, v, up1, radius, we.metric);
				d2 = Pn.distanceBetween(v, profile[j], we.metric);
			}
			double[][] tubev= surfaceOfRevolution(profile, slices, Math.PI * 2);
			Rn.matrixTimesVector(tubev, isometry, tubev);
			Pn.dehomogenize(profile, profile);
			QuadMeshFactory qmf = new QuadMeshFactory();
			qmf.setClosedInUDirection(false);
			qmf.setClosedInVDirection(false);
			qmf.setULineCount(profile.length);
			qmf.setVLineCount(slices);
			qmf.setVertexCoordinates(tubev);
			qmf.setGenerateEdgesFromFaces(true);
			qmf.setGenerateVertexNormals(true);
			qmf.setGenerateFaceNormals(true);
			qmf.update();
			SceneGraphComponent sgc = new SceneGraphComponent();//SceneGraphUtilities.createFullSceneGraphComponent("tube"+i);
			sgc.setGeometry(qmf.getIndexedFaceSet());
			all.addChild(sgc);
		}
		
		return all;
	}
	
	  /**
		 * Create a surface of revolution surface by rotating the profile curve around the X-axis.
		 * The resulting array with have the original curve twice, once at the beginning and also
		 * at the end.  
		 * @param profile	a 3- or 4-d array of points (generally of form (x,y,0) or (x,y,0,1))
		 * @param num		number of copies of the curve to make
		 * @return
		 * @see 
		 */
		public static double[][] surfaceOfRevolution(double[][] profile, int num, double angle) {
			if (num <= 1 || profile[0].length < 3) {
				throw new IllegalArgumentException("Bad parameters");
			}
			double[][] vals = new double[num * profile.length][profile[0].length];
			for (int i = 0 ; i < num; ++i)	{
				double a = i * angle/(num-1);
				double[] rot = P3.makeRotationMatrixX(null, a);
				for (int j = 0; j<profile.length; ++j)
					Rn.matrixTimesVector(vals[i*profile.length+j], rot, profile[j]);
			}
			return vals;
		}


	public static SceneGraphComponent createBeamsOnEdges(WingedEdge we, IndexedFaceSet beams, double radius, int segments, int slices)	{
		return createBeamsOnEdges(null, we, beams, radius, segments, slices);
	}

	public static SceneGraphComponent createBeamsOnEdges(SceneGraphComponent all, 
				WingedEdge we, 
				IndexedFaceSet beams, 
				double radius, 
				int segments, 
				int slices)	{
			int numTubes = we.getNumEdges();
			double[][] vertices = new double[(slices > 1 ? 6: 4)*numTubes][4];
			double[][] vNormals = new double[vertices.length][4];
			int[][] indices = new int[(slices > 1 ? 2 : 1)*numTubes][4];
			int vertexCount = 0, faceCount = 0;
			double[][] standardTex = {{0,0},{1,0},{0,1},{1,1}, {0,.5},{1,.5}};
			double[][] fullTex = new double[(slices > 1 ? 6: 4)*numTubes][];
			double[][] colors = new double[(slices > 1 ? 2 : 1)*numTubes][];
			boolean doNotKnot = (we.getGeometryAttributes("doNotKnot") != null);
			double[][] ecolors = null;
			if (we.getEdgeAttributes(Attribute.COLORS) != null)
				ecolors = we.getEdgeAttributes(Attribute.COLORS).toDoubleArrayArray(null);
			boolean doFaceColors = doNotKnot || ecolors != null;
			for (int i = 0; i<numTubes; ++i)	{
				Edge edge = (Edge) we.edgeList.get(i);
				double[] v0 = edge.v0.point;
				double[] v1 = edge.v1.point;
				double[] diff = Rn.subtract(null, v0, v1);
				//System.err.println("Edge vector is "+Rn.toString(diff));
				if (doNotKnot)	{
					int index = 0;
					if ( Math.abs(diff[0]) + Math.abs(diff[1]) < 10E-8) index = 0;
					else if ( Math.abs(diff[1]) + Math.abs(diff[2]) < 10E-8) index = 1;
					else if ( Math.abs(diff[2]) + Math.abs(diff[0]) < 10E-8) index = 2;
					else index = 3;
					if (slices > 1) colors[2*i] = colors[2*i+1]=borromColors[index];
					else colors[i] = borromColors[index];	
				} else if (ecolors != null)	{
					if (slices > 1) colors[2*i] = colors[2*i+1]=ecolors[i];
					else colors[i] = ecolors[i];
				}
				else colors[i] = new double[]{1,1,1};				
				double[] v0L, v0R, v1L, v1R;
				if (edge.e0L.v0 == edge.v0)	v0L = edge.e0L.v1.point;
				else 						v0L = edge.e0L.v0.point;
				if (edge.e0R.v0 == edge.v0)	v0R = edge.e0R.v1.point;
				else 						v0R = edge.e0R.v0.point;
				if (edge.e1L.v0 == edge.v1)	v1L = edge.e1L.v1.point;
				else 						v1L = edge.e1L.v0.point;
				if (edge.e1R.v0 == edge.v1)	v1R = edge.e1R.v1.point;
				else 						v1R = edge.e1R.v0.point;
				// TODO adjust the length dragged based on the angle between this edge and adjacent edge
				// using the inverse sine rule.
				double[] c0L = Pn.dragTowards(null,  v0, v0L, radius, we.metric);
				double[] c0R = Pn.dragTowards(null,  v0, v0R, radius, we.metric);
				double[] c1L = Pn.dragTowards(null,  v1, v1L, radius, we.metric);
				double[] c1R = Pn.dragTowards(null,  v1, v1R, radius, we.metric);
				double[] c0M = Pn.linearInterpolation(null, c0L, c0R, .5, we.metric);
				double[] c1M = Pn.linearInterpolation(null, c1L, c1R, .5, we.metric);
				double[] p0L = PlueckerLineGeometry.projectPointOntoLine(null, c0L, v0, v1, we.metric);
				double[] p0R = PlueckerLineGeometry.projectPointOntoLine(null, c0R, v0, v1, we.metric);
				double[] p0 = Pn.linearInterpolation(null, p0L, p0R, .5, we.metric);
				double[] p1L = PlueckerLineGeometry.projectPointOntoLine(null, c1L, v1, v0, we.metric);
				double[] p1R = PlueckerLineGeometry.projectPointOntoLine(null, c1R, v1, v0, we.metric);
				double[] p1 = Pn.linearInterpolation(null, p1L, p1R, .5, we.metric);
	//			System.err.println("v0 = "+Rn.toString(v0));
	//			System.err.println("p0 = "+Rn.toString(p0));
	//			System.err.println("v1 = "+Rn.toString(v1));
	//			System.err.println("p1 = "+Rn.toString(p1));
				c0M = Pn.dragTowards(null,  p0, c0M, radius, we.metric);
				c1M = Pn.dragTowards(null,  p1, c1M, radius, we.metric);
				int count = 0;
				Rn.subtract(vNormals[vertexCount+count], c0L, v0);
				vertices[vertexCount+count++] = c0L;
				Rn.subtract(vNormals[vertexCount+count], c1L, v1);
				vertices[vertexCount+count++] = c1L;
				if (slices > 1)	{
					Rn.subtract(vNormals[vertexCount+count], c0M, v0);
					vertices[vertexCount+count++] = c0M;
					Rn.subtract(vNormals[vertexCount+count], c1M, v1);
					vertices[vertexCount+count++] = c1M;				
				}
				Rn.subtract(vNormals[vertexCount+count], c0R, v0);
				vertices[vertexCount+count++] = c0R;
				Rn.subtract(vNormals[vertexCount+count], c1R, v1);
				vertices[vertexCount+count++] = c1R;
				count = 0;
				fullTex[vertexCount+count++] = standardTex[0];
				fullTex[vertexCount+count++] = standardTex[1];
				if (slices > 1) {
					fullTex[vertexCount+count++] = standardTex[4];
					fullTex[vertexCount+count++] = standardTex[5];				
				}
				fullTex[vertexCount+count++] = standardTex[2];
				fullTex[vertexCount+count++] = standardTex[3];
				indices[faceCount][0] = vertexCount;
				indices[faceCount][1] = vertexCount+1;
				indices[faceCount][2] = vertexCount+3;
				indices[faceCount][3] = vertexCount+2;
				faceCount++;
				if (slices > 1) {
					indices[faceCount][0] = vertexCount+2;
					indices[faceCount][1] = vertexCount+3;
					indices[faceCount][2] = vertexCount+5;
					indices[faceCount][3] = vertexCount+4;
					faceCount++;				
				}
				vertexCount += (slices > 1)? 6:4;
			}
			if (beams != null  && beams.getGeometryAttributes("factory") != null)	{
				IndexedFaceSetFactory ifsf = (IndexedFaceSetFactory) beams.getGeometryAttributes("factory");
				ifsf.setVertexCoordinates(vertices);
				ifsf.setGenerateFaceNormals(true);
				ifsf.update();
				//beams.setVertexAttributes(Attribute.NORMALS, StorageModel.DOUBLE_ARRAY.array(4).createReadOnly(vNormals));
			} else {
				IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
				ifsf.setMetric(we.metric); // Pn.EUCLIDEAN); //
				System.err.println("winged edge metric = "+we.metric);
				ifsf.setGenerateEdgesFromFaces(true);
				ifsf.setGenerateVertexNormals(false);
				ifsf.setGenerateFaceNormals(true);
				ifsf.setVertexCount(vertices.length);
				ifsf.setFaceCount(indices.length);
				ifsf.setVertexCoordinates(vertices);
				//ifsf.setVertexNormals(vNormals);
				ifsf.setVertexTextureCoordinates(fullTex);
				ifsf.setFaceIndices(indices);
				if (doFaceColors) ifsf.setFaceColors(colors);
				ifsf.update();
				beams = ifsf.getIndexedFaceSet();
				beams.setName("Beams"+Integer.toString(we.hashCode()));
				beams.setGeometryAttributes("factory", ifsf);
			}
	//		if (beams == null) beams = IndexedFaceSetUtility.createIndexedFaceSetFrom(indices, vertices, null, null, null, null);
	//		else IndexedFaceSetUtility.setIndexedFaceSetFrom(beams, indices, vertices, null, null, null, null, null);
	//		beams.buildEdgesFromFaces();
	//		GeometryUtility.calculateAndSetFaceNormals(beams);
			if (all == null) {
				all = SceneGraphUtility.createFullSceneGraphComponent("beamsOnEdges");
			}
			if (all.getGeometry() != beams) all.setGeometry(beams);
			all.getAppearance().setAttribute(CommonAttributes.METRIC, we.metric);
			return all;
		}

	/**
	 * 
	 * @param ifs
	 * @return
	 */
	public static WingedEdge convertConvexPolyhedronToWingedEdge(IndexedFaceSet ifs)	{
		WingedEdge we = new WingedEdge(200.0);
		
		if (ifs.getFaceAttributes(Attribute.COLORS) != null)
				we.setColormap(ifs.getFaceAttributes(Attribute.COLORS).toDoubleArrayArray(null));
		
		DataList fn = ifs.getFaceAttributes(Attribute.NORMALS);
		if (fn != null) we.setFaceCountAndAttributes(Attribute.NORMALS, fn);
		DataList vc = ifs.getVertexAttributes(Attribute.COORDINATES);
		if (vc != null) we.setVertexCountAndAttributes(Attribute.COORDINATES, vc);
		DataList vn = ifs.getVertexAttributes(Attribute.NORMALS);
		if (vn != null) we.setVertexAttributes(Attribute.NORMALS, vn);
		
		WingedEdgeUtility.removeDuplicateVertices(ifs);
		double[][] vv = ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
	
		int[][] indices = ifs.getFaceAttributes(Attribute.INDICES).toIntArrayArray(null);
		for (int i = 0; i<indices.length; ++i)	{
			double[][] points = new double[3][];
			double[] plane = new double[4];
			if (indices[i].length < 3) continue;
			for (int j = 0; j<3; ++j)	points[j] = vv[indices[i][j]];
			P3.planeFromPoints(plane, points[0], points[1], points[2]);
			if (plane[3] > 0) Rn.times(plane,-1.0,plane);
			we.cutWithPlane(plane, i);
		}
		we.update();
		LoggingSystem.getLogger(WingedEdge.class).log(Level.FINER,"Created winged edge with "+we.numFaces+" facces and"+we.numVertices+" vertices.");
		return we;
	}

	public static double[] centerPoint(WingedEdge we)	{
		double[][] pts = we.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		return Pn.centroid(null, pts, we.metric);
	}

	public static List<Face> pointLiesOutsideFace(WingedEdge we, double[] p, double tolerance)		{
		return pointLiesOutsideFace(we, p, tolerance, -1);
	}
	
	/**
	 * Generate a list of the first <i> count </i> faces WRT which the point <i>p</i> lies outside. 
	 */
	public static List<Face> pointLiesOutsideFace(WingedEdge we, double[] p, double tolerance, int count)		{
		Vector<Face> v  = new Vector<Face>();
		double[] p4 = new double[4];
		Rn.copy(p4, P3.originP3);
		Rn.copy(p4, p);
		List<Face> faces = we.getFaceList();
		int n = faces.size();
		for (int i = 0; i<n; ++i)	{
			WingedEdge.Face f = ( faces.get(i));
			double d = Rn.innerProduct(p4, f.plane);
			if (d > tolerance)	{
				v.add(f);
				if (count >= 0 && v.size() >= count) break;
			}
		}
		return v;
	}

	public static void removeDuplicateVertices(IndexedFaceSet ifs)	{
		double tolerance = 10E-8;
		double[][] vdata = ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		int[][] indices = ifs.getFaceAttributes(Attribute.INDICES).toIntArrayArray().toIntArrayArray(null);
		for (int i = 0; i<indices.length; ++i)	{
			int[] newIndices = new int[indices[i].length];
			int count = 0;
			for (int j = 0; j<indices[i].length; ++j)	{
				if ( Rn.equals(vdata[indices[i][j]], vdata[indices[i][(j+1)%indices[i].length]], tolerance)) continue;
				newIndices[count++] = indices[i][j];
			}
			indices[i] = new int[count];
			System.arraycopy(newIndices,0,indices[i], 0,count);
		}
		ifs.setFaceAttributes(Attribute.INDICES, 	new IntArrayArray.Array(indices));
	}

	public static SceneGraphComponent unfoldXY(WingedEdge we)	{
		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		List<Face> remaining = new Vector<Face>();
		List<Face> done = new Vector<Face>();
		Face face0 = we.getFaceList().get(0);
		remaining.add(face0);
		// first rotate the plane of this anchor face to the xy plane
		// all other faces will be rotated then to lie in this plane
		
		while(!remaining.isEmpty())  {
			Face f = remaining.get(0);
			Edge firstEdge = f.someEdge;
			Edge thisEdge = firstEdge;
			Edge nextEdge = thisEdge.fL == f ? thisEdge.e1L : thisEdge.e1R;
			System.err.println("Face "+f.tag);
			int count = 0;
			do 	{
				Face otherface = thisEdge.fL == f ? thisEdge.fR : thisEdge.fL;
				if ( !done.contains(otherface) && !remaining.contains(otherface)) remaining.add(otherface);
				System.err.println("    edge "+thisEdge.tag);
				Edge tmp = nextEdge;
				//thisEdge = nextEdge;
				if (nextEdge.fL != f && nextEdge.fR != f)
					throw new IllegalStateException("Lost the face");
				nextEdge = nextEdge.fL == f ? 
						(thisEdge == nextEdge.e0L ? nextEdge.e1L : nextEdge.e0L) : 
						(thisEdge == nextEdge.e0R ? nextEdge.e1R : nextEdge.e0R) ;
				thisEdge = tmp;
				count++;
				if (count > 10) break;
			}
			while ((thisEdge != firstEdge ));
			remaining.remove(f);
			done.add(f);
		}
		SceneGraphComponent sgc = null;
		return sgc;
	}
	
}

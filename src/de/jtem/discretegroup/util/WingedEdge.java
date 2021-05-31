package de.jtem.discretegroup.util;


import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;

import charlesgunn.math.p5.PlueckerLineGeometry;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.Scene;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.StorageModel;
import de.jreality.util.LoggingSystem;

/*
 * Created on Mar 16, 2004
 *
  */
/**
 * A class for representing closed polyhedral models which stores connectivity information via the edge.
 * That is, every edge knows its two vertices, its two faces, and the four edges adjacent to it having in
 * common with it one of these vertices and one of these faces.
 * It's currently [29.07.05] sadly in need of a factory method to keep its under;lying IndexedFaceSet up-to-date with
 * itself.
 * Original C code stems from Jeff Weeks.
 * @author Charles Gunn
 *
 */
public class WingedEdge extends IndexedFaceSet {
	double		bounds[][] = new double[2][3];
	int		numVertices,
			numEdges,
			numFaces;
	int metric;
	boolean	isDirty,
		coloredFaces = true;
	public Vector<Vertex> vertexList;
	public Vector<Edge> edgeList;
	public Vector<Face>	faceList;
	double[][] colormap;

	static double tolerance = 10E-8;
	
	public WingedEdge(double xs, double ys, double zs)	{
		super();
		initAsBox(-xs, xs, -ys, ys, -zs, zs);
	}
	
	public WingedEdge(double xn, double xx, double yn, double yx, double zn, double zx)	{
		super();
		initAsBox(xn, xx, yn, yx, zn, zx);
	}
	
	public WingedEdge(double d)	{
		super();
		initAsBoxOfSize(d,d,d);
	}
	
	public WingedEdge()	{
		super();
		initAsBoxOfSize(17.0, 17.0, 17.0);
	}
	
	public class Vertex	{
		public double[] point;
		public double dist;
		public int order, tag;
		public boolean isIdeal;
		Vertex()	{
			this(new double[4]);
		}
		Vertex(double[] p)	{
			super();
			point = p;
		}
	}
	
	public class Edge	{
		public Vertex v0, v1;
		public Edge	e0L, e0R, e1L, e1R;
		public Face 	fL, fR;
		public int tag = -1;
		Edge()	{
			super();
		}
	}
	
	public class Face	{
		public Face	inverse;
		public Edge someEdge;
		public int	order,
			tag,		// can be set by user
			index;		// reserved for the index of this face in the facelist
		public Object source;
		public double[] plane;
		public double area;
		Face()	{
			plane = new double[4];
		}
		
	}
	public void init()
	{
		 initAsCubeOfSize(17.0d);
	}

	public void initAsCubeOfSize(double size)	 {
		initAsBoxOfSize(size, size, size);
	}

	public void initAsBoxOfSize(double xs, double ys, double zs)	
	{
		initAsBox(-xs, xs, -ys, ys, -zs, zs);
	}

	/**
	 * @param d
	 * @param e
	 * @param f
	 * @param g
	 * @param h
	 * @param i
	 */
	private void initAsBox(double d, double e, double f, double g, double h, double i) {
		bounds[0][0] = d; 
		bounds[1][0] = e;
		bounds[0][1] = f;
		bounds[1][1] = g;
		bounds[0][2] = h;
		bounds[1][2] = i;
		reset();
	}

	public void reset()	
	{
		int i;

		numVertices		= 8;
		numEdges		= 12;
		numFaces		= 6;
		isDirty 		= true;

		vertexList	= new Vector<Vertex>(8);
		edgeList = new Vector<Edge>(12);
		faceList = new Vector<Face>(6);
		
		for (i=0; i < 8; ++i ) {
			double[] v = new double[4];
			v[0] = ((i & 4) != 0) ? bounds[1][0] : bounds[0][0];
			v[1] = ((i & 2) != 0) ? bounds[1][1] : bounds[0][1];
			v[2] = ((i & 1) != 0) ? bounds[1][2] : bounds[0][2];
			v[3] = 1.0;
			vertexList.add(new Vertex(v));
		}

		for (i=0; i < 12; ++i ) 	edgeList.add(new Edge());
		for (i=0; i < 6; ++i ) 		faceList.add(new Face());
		
		for (i=0; i < 12; ++i)		{
			Edge we = edgeList.get(i);
			we.tag = i;
			we.v0	= vertexList.get(WingedEdgeUtility.edata[i][0]);
			we.v1	= vertexList.get(WingedEdgeUtility.edata[i][1]);
			we.e0L	= ((Edge) edgeList.get(WingedEdgeUtility.edata[i][2]));
			we.e0R	= ((Edge) edgeList.get(WingedEdgeUtility.edata[i][3]));
			we.e1L	= ((Edge) edgeList.get(WingedEdgeUtility.edata[i][4]));
			we.e1R	= ((Edge) edgeList.get(WingedEdgeUtility.edata[i][5]));
			we.fL	= ((Face) faceList.get(WingedEdgeUtility.edata[i][6]));
			we.fR	= ((Face) faceList.get(WingedEdgeUtility.edata[i][7]));
		}

		for (i=0; i < 6; ++i ) {
			Face wf = (Face) faceList.get(i);
			wf.order			= 4;
			wf.index = wf.tag				= i;
			wf.someEdge		= ((Edge) edgeList.get(WingedEdgeUtility.fdata[i]));
			wf.inverse			= null;
		}
		update();

	}

	private void updateColors()	{
		if (!coloredFaces) return;
		if (colormap == null)	{
			colormap = WingedEdgeUtility.builtincmap;
		}
		double[][] cc = new double[numFaces][colormap[0].length];
		Face[] farray = (Face []) faceList.toArray(new Face[1]);
		
		for (int i = 0; i<numFaces;  ++i)	{
			//if (needsNewFaceColors)
			int foo = (farray[i].tag >= 0) ? farray[i].tag : 0;
				Rn.copy(cc[i], colormap[foo % colormap.length]);
		}
		DataList newFaceColors = StorageModel.DOUBLE_ARRAY.array(colormap[0].length).createReadOnly(cc);
		setFaceAttributes(Attribute.COLORS, newFaceColors);
	}
	
	public void update()	{
		if (!isDirty) return;
		
		numFaces = faceList.size();
		numEdges = edgeList.size();
		numVertices = vertexList.size();
		
		if (numVertices == 0 || numEdges == 0 || numFaces ==  0)	{
			throw new IllegalStateException("Degenerate geometry");
		}
		
//		Vertex[] varray = vertexList.toArray(new Vertex[1]);
//		Face[] farray = faceList.toArray(new Face[1]);
		
		final double[][] data = new double[numVertices][4];
		for (int i = 0; i < numVertices; ++i)	{
			Rn.copy(data[i], vertexList.get(i).point);
			vertexList.get(i).tag = i;
		}
	
		final int[][] indices = new int[numFaces][];
		
//		System.err.println("printing faces");
		for (int i = 0; i<numFaces;  ++i)	{
			indices[i] = new int[faceList.get(i).order];
//			System.err.println("Order "+faceList.get(i).order);
		}
		
		for (int i = 0; i<numEdges;  ++i)	{
			edgeList.get(i).tag = i;
		}
		
		for (int i = 0; i < numFaces;  ++i)	{
			Face thisf  = faceList.get(i);
			thisf.index = i;
			//System.err.print("Face "+farray[i].order);
			Edge thise = thisf.someEdge;
			int vertexIndex = 0;
				do {
					if (vertexIndex == indices[i].length)	{		// problem!
						LoggingSystem.getLogger(this).log(Level.WARNING,"Bad vertex index");
						break;
					}
					if (thise.fL ==  faceList.get(i)) {
						indices[i][vertexIndex++] = thise.v0.tag;
						thise = thise.e1L;
					} else {
						indices[i][vertexIndex++] = thise.v1.tag;
						thise = thise.e0R;
					}
					//System.err.print("\t"+indices[i][vertexIndex-1]);
				} while (thise != thisf.someEdge );
				//System.err.println("");
		}
		isDirty = false;
		final IndexedFaceSet ifs = this;
		//System.err.println("IN update: "+numEdges+":"+numFaces);
		Scene.executeWriter(ifs, new Runnable()	{

			public void run() {
				setVertexCountAndAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(4).createReadOnly(data));
				setFaceCountAndAttributes(Attribute.INDICES, StorageModel.INT_ARRAY_ARRAY.createReadOnly(indices));
				IndexedFaceSetUtility.calculateAndSetFaceNormals(ifs);
				calculateAndSetEdgesFromFaces(ifs);
				updateColors();
			}
		
		});
		
		fireGeometryChanged(null, null, null, null);
		//isBoundDirty = true;
}

	protected void calculateAndSetEdgesFromFaces(IndexedFaceSet ifs) {
		int[][] edges = new int[edgeList.size()][2];
		for (int i = 0; i<edgeList.size(); ++i)	{
			edges[i][0] = edgeList.get(i).v0.tag;
			edges[i][1] = edgeList.get(i).v1.tag;
		}
		setEdgeCountAndAttributes(Attribute.INDICES, StorageModel.INT_ARRAY_ARRAY.createReadOnly(edges));
	}

	public void removeEdge(int[] edges)	{
		ArrayList<Edge> ede = new ArrayList<Edge>();
		for (int i : edges)	{
			ede.add(edgeList.get(i));
		}
		for (Edge e: ede)
			removeEdge(e);
	}
	public void removeEdge(Edge e )	{
		if (!edgeList.contains(e)) 
			throw new IllegalStateException("No such edge");
		
		// merge fR into fL
		Edge nextEdge = e.e1R;
		if (e.fL.someEdge == e) e.fL.someEdge = nextEdge;
//		System.err.println("Beginning edge "+e.tag+"::"+
//				e.v0.tag+":"+
//				e.v1.tag);
		do {
//			System.err.println("Processing edge "+nextEdge.tag+"::"+
//					nextEdge.v0.tag+":"+
//					nextEdge.v1.tag);
			if (nextEdge.fR == e.fR) {
//				System.err.println("0");
				nextEdge.fR = e.fL;
				nextEdge = nextEdge.e1R;
			}
			else if (nextEdge.fL == e.fR) {
//				System.err.println("1");
				nextEdge.fL = e.fL;
				nextEdge = nextEdge.e0L;
			}
			else  {
				throw new IllegalStateException("Bad face connectivity");				
			}
			e.fL.order++;
		} while (nextEdge != e);
		// remove this edge from vertex 0
		if (e == e.e0L.e1L)	e.e0L.e1L = e.e0R;
		else if (e == e.e0L.e0R) e.e0L.e0R = e.e0R;
		else throw new IllegalStateException("Bad edge connections");
		
		if (e == e.e0R.e1R)	e.e0R.e1R = e.e0L;
		else if (e == e.e0R.e0L) e.e0R.e0L = e.e0L;
		else throw new IllegalStateException("Bad edge connections");
		
		// remove this edge from vertex 1
		if (e == e.e1L.e0L)	e.e1L.e0L = e.e1R;
		else if (e == e.e1L.e1R) e.e1L.e1R = e.e1R;
		else throw new IllegalStateException("Bad edge connections");
		
		if (e == e.e1R.e0R)	e.e1R.e0R = e.e1L;
		else if (e == e.e1R.e1L) e.e1R.e1L = e.e1L;
		else throw new IllegalStateException("Bad edge connections");
		
		edgeList.remove(e);
		e.fL.order--;
		faceList.remove(e.fR);
		isDirty = true;
		update();
		
	}
	public void cutWithPlane(double[] aPlane)	{
		cutWithPlane(aPlane, numFaces, null);
	}
	

	public void cutWithPlane(double[][] planes)	{
		int n = planes.length;
		for (int i = 0; i<n; ++i)	{
			cutWithPlane(planes[i], i, null);
		}
	}
	public void cutWithPlane(double[] aPlane, int aTag)	{
		cutWithPlane(aPlane,  aTag, null);	
	}

	public boolean cutWithPlane(double[] aPlane, int aTag, Object s)	{
		
		Face          newFace;
		boolean             face_is_needed = false;

		for (int i = 0; i<numVertices;  ++i)	{
			Vertex  nvertex = (Vertex) vertexList.get(i);
			nvertex.dist = Rn.innerProduct( aPlane, nvertex.point);
			// TODO work out better tolerance control
			if (nvertex.dist > .00001) face_is_needed = true;
			if (Math.abs(nvertex.dist) < tolerance) {
				nvertex.dist = 0.0;
			}
		}

		if ( !face_is_needed)  return false;
		newFace = new Face();
//		System.err.println("adding face for tag "+aTag); //Rn.toString(aPlane));
		Rn.copy(newFace.plane, aPlane);
		newFace.tag = aTag;		
		newFace.source = s;	

		/* set the new face */
		newFace.order = 0;
		cutEdges();
		cutFaces(newFace);
		removeDeadEdges();
		removeDeadVertices();

		faceList.add(newFace);
		numFaces++;

		isDirty = true;
		update();
		return true;
	}

/*
- computeAreas
{
	WingedEdgeFace		*fptr;
	WingedEdgeEdge		*eptr;
	double		*nv, *ov;
	int 		i, lim;
	float		totalarea = 0;

		for (i = 0, lim = [faceList count]; i < lim;  ++i)	{
		fptr = [faceList objectAt: i];
		eptr = fptr->some_edge;
		fptr->area = 0.0;
		do	{
		if (eptr->fL == fptr) {
			ov = (double *) &eptr->v0->point;
				nv = (double *) &eptr->v1->point;
				eptr = eptr->e1L;
			} 
		else {
			ov = (double *) &eptr->v1->point;
				nv = (double *) &eptr->v0->point;
				eptr = eptr->e0R;
			}
		fptr->area += triArea((double *) &fptr->plane, ov, nv);
		} while (eptr != fptr->some_edge);
		totalarea += fptr->area;
	}

	return self;
}
*/

	public void cutEdges()	{
		int			i, lim;

		for (i = 0, lim = numEdges;  i< lim; ++i)	{
			Edge edge = (Edge) edgeList.get(i);
			double d0 = edge.v0.dist;
			double d1 = edge.v1.dist;
			if ( d0 * d1 < 0.0) {
				Vertex new_vertex =new Vertex();
				Edge new_edge =new Edge();
				double t = -d0/(d1 - d0);
				double s = 1.0 - t;
				/*
				if (Math.abs(s) < tolerance)	{
					new_vertex = edge.v1;
					//continue;
				} else if (Math.abs(t) < tolerance)	{
					new_vertex = edge.v0;
					//continue;
				} else */
				Rn.linearCombination(new_vertex.point, s, edge.v0.point, t, edge.v1.point);
				Vertex already = vertexExists(new_vertex.point);
				if (already != null)	{
					//new_vertex = already;
					LoggingSystem.getLogger(this).log(Level.WARNING,"duplicate vertex "+already.tag);
				} //else {
					vertexList.add(new_vertex);
					numVertices++;
					new_vertex.dist = 0.0;
				//}
	
				new_edge.v1 = edge.v1;
				new_edge.v0 = new_vertex;
				edge.v1 = new_vertex;
	
				Edge nbr_edge = edge.e1L;
				new_edge.e1L = nbr_edge;
				if (nbr_edge.e0L == edge)		nbr_edge.e0L = new_edge;
				else							nbr_edge.e1R = new_edge;
	
				nbr_edge = edge.e1R;
				new_edge.e1R = nbr_edge;
				if (nbr_edge.e0R == edge)		nbr_edge.e0R = new_edge;
				else							nbr_edge.e1L = new_edge;
	
				new_edge.e0L = edge;
				new_edge.e0R = edge;
				edge.e1L = new_edge;
				edge.e1R = new_edge;
	
				new_edge.fL = edge.fL;
				new_edge.fR = edge.fR;
	
				edge.fL.order++;
				edge.fR.order++;
	
				edgeList.add(new_edge);
				numEdges++;
			}
		}
	
		// make sure each good face "sees" a good edge 
		for (i = 0; i<numEdges;  ++i)	{
			Edge edge = edgeList.get(i);
			if (edge.v0.dist < 0.0 || edge.v1.dist < 0.0) {
				edge.fL.someEdge = edge;
				edge.fR.someEdge = edge;
			}
		}
	}

	/* check each old face:									*/
	/* (1) if a face is entirely >= 0, remove it			*/
	/*	   (this can be checked immediately, because each	*/
	/*	   good face sees a good edge at this point)		*/
	/*	   (check that removing dead faces doesn't			*/
	/*	   change the group)								*/
	/* (2) if a face is entirely <=0, leave it alone		*/
	/*	   (but make sure any 0-0 edges "see" the new face	*/
	/*		and the new face sees the 0-0 edges)			*/
	/*		also set order of new face						*/
	/* (3) otherwise bisect the face with a new edge, and	*/
	/*	   make sure the new edge "sees" the new new face	*/
	/*	   and the old face "sees" a valid edge				*/
	
	public void cutFaces(Face new_face)	{

		/* we'll count the order of the new_face as we go */
		new_face.order = 0;
	
		// move from the end backwards so deletions aren't noticed
		Edge next_edge, e0 = null, e1 = null, e2 = null, e3 = null;
		for (int i = numFaces - 1;  i >= 0; --i )	{
			Face	face = (Face) faceList.get(i);
			Edge edge = face.someEdge;
	
			/* Is the face entirely >= 0 ? */
			/* Note: cutEdges() has been called,	*/
			/* so all good faces see good edges.		*/
			/* Some fL and fR pointers on 0-0 edges may	*/
			/* temporarily be left dangling when face is freed.	*/
			/* check here for bad faces */
			if (face.someEdge.v0.dist >= 0 && face.someEdge.v1.dist >= 0) {
				faceList.remove(i);
				continue;
			}
	
			/* cut the face if necessary */
	
			/* counts number of vertices at dist 0 (two consecutive */
			/* vertices at dist 0 are counted as one (or none)	*/
			/* because we don't need to cut such a face)		*/ 
			int zero_count = 0;
	
			/* We'll traverse the face counterwise to find the	*/
			/* edges going in and out of the "zero vertices" (i.e.	*/
			/* the vertices at dist 0)				*/
			/* e0 = negative to zero				*/
			/* e1 = zero to positive				*/
			/* e2 = positive to zero				*/
			/* e3 = zero to negative				*/
	
			int count = 0, count1 = 0, count3 = 0;	/* for use in finding order of new face */
			do {	
				double d0, d1;
				/* which way does the edge point? */
				/* edge points counterclockwise	*/
				if (edge.fL == face) {	
					d0 = edge.v0.dist;
					d1 = edge.v1.dist;
					next_edge = edge.e1L;
				}
				else {		/* edge points clockwise	*/
					d0 = edge.v1.dist;
					d1 = edge.v0.dist;
					next_edge = edge.e0R;
				}
				if (d0 == 0.0) {
					if (d1 == 0.0) {
						if (edge.fL == face)
							edge.fR = new_face;
						else
							edge.fL = new_face;
						new_face.someEdge = edge;
						new_face.order++;
						break;
						}
					zero_count++;
					if (d1 < 0.0) {
						e3 = edge;
						count3 = count;
					}
					else {	/* d1 > 0.0 */
						e1 = edge;
						count1 = count;
					}
				}
				else if (d1 == 0.0) {
					if (d0 < 0.0)
						e0 = edge;
					else	/* d0 > 0.0 */
						e2 = edge;
				}
	
				edge = next_edge;
				count++;
			}
			while (edge != face.someEdge);
	
			if (zero_count == 2) { /* we need to make a cut */
				Edge new_edge =new Edge();
				edgeList.add(new_edge);
				numEdges++;
	
				/* v01 = vertex between edges e0 and e1	*/
				/* v23 = vertex between edges e2 and e3	*/
				Vertex v01 = (e0.v0.dist == 0.0) ? e0.v0 : e0.v1;
				Vertex v23 = (e2.v0.dist == 0.0) ? e2.v0 : e2.v1;
	
				new_edge.v0 = v01;
				new_edge.v1 = v23;
				new_edge.e0L = e0;
				new_edge.e0R = e1;
				new_edge.e1L = e3;
				new_edge.e1R = e2;
				new_edge.fL = face;
				new_edge.fR = new_face;
	
				if (e0.v0 == v01)
					e0.e0R = new_edge;
				else
					e0.e1L = new_edge;
	
				if (e1.v0 == v01)
					e1.e0L = new_edge;
				else
					e1.e1R = new_edge;
	
				if (e2.v0 == v23)
					e2.e0R = new_edge;
				else
					e2.e1L = new_edge;
	
				if (e3.v0 == v23)
					e3.e0L = new_edge;
				else
					e3.e1R = new_edge;
	
				new_face.someEdge = new_edge;
				new_face.order++;
	
				face.order = (count1 - count3 + face.order)%face.order + 1;
	
			}
		}
		numFaces = faceList.size();
	}

	public void removeDeadEdges()	{
		// move from the end backwards so deletions aren't noticed
		for (int i = numEdges - 1;  i >= 0; --i )	{
			Edge edge = (Edge) edgeList.get(i);
	
			/* if it has one or more bad vertices, throw it away */
			if (edge.v0.dist > 0.0 || edge.v1.dist > 0.0) {
	
				/* first tidy up at vertices of dist 0.0 */
				if (edge.v0.dist == 0.0) {
					if (edge.e0L.e0R == edge)
						edge.e0L.e0R = edge.e0R;
					else
						edge.e0L.e1L = edge.e0R;
	
					if (edge.e0R.e0L == edge)
						edge.e0R.e0L = edge.e0L;
					else
						edge.e0R.e1R = edge.e0L;
				}
				if (edge.v1.dist == 0.0) {
					if (edge.e1L.e1R == edge)
						edge.e1L.e1R = edge.e1R;
					else
						edge.e1L.e0L = edge.e1R;
	
					if (edge.e1R.e1L == edge)
						edge.e1R.e1L = edge.e1L;
					else
						edge.e1R.e0R = edge.e1L;
				}
				/* throw away the edge */
				edgeList.remove(i);
			}
		}
		numEdges = edgeList.size();
	}
	
	
	public void removeDeadVertices()		{
		for (int i = numVertices - 1;  i >= 0; --i )	{
			Vertex	nvertex = (Vertex) vertexList.get(i);
			if (nvertex.dist > 0.0) vertexList.remove(i);
		}
		numVertices = vertexList.size();
	}

	public Vertex vertexExists(double[] pt)	{
		for (int i = 0; i<vertexList.size(); ++i)	{
			Vertex wev = (Vertex) vertexList.get(i);
			if (Rn.equals(wev.point, pt, tolerance)) return wev;
		}
		return null;
	}

	public WingedEdge polarize()	{
		WingedEdge P = polarize(1.0);
		// rest of the method looks for the right scaling factor so edges intersect
		double[] v0, v1, w0, w1;
		Edge wee = edgeList.get(0);
		v0 = wee.v0.point;
		v1 = wee.v1.point;
		int i;
		for ( i = 0; i<P.edgeList.size(); ++i)	{
			Edge wep = P.edgeList.get(i);
			if ((wep.fL.tag == wee.v0.tag && wep.fR.tag == wee.v1.tag) ||
				(wep.fL.tag == wee.v1.tag && wep.fR.tag == wee.v0.tag) )	break;
		}
		double scale;
		if (i < P.edgeList.size())	{
			wee = P.edgeList.get(i);
			w0 = wee.v0.point;
			w1 = wee.v1.point;
			double[] p = PlueckerLineGeometry.lineFromPoints(null, v0, v1);
			double[] q = PlueckerLineGeometry.lineFromPoints(null, w0, w1);
			scale = -(q[2]*p[3] + q[4]*p[1] + q[5]*p[0])/(q[0]*p[5] + q[1]*p[4] + q[3]*p[2]);
			LoggingSystem.getLogger(this).log(Level.FINER,"Scale: "+scale);
		} else {
			LoggingSystem.getLogger(this).log(Level.WARNING,"Can't find matching edge");
			//return null;
			scale = .5;
		}
		double[] mat = MatrixBuilder.euclidean().scale(scale).getArray();
		for ( i = 0; i<P.vertexList.size(); ++i)	{
			Vertex wv = P.vertexList.get(i);
			Rn.matrixTimesVector(wv.point, mat, wv.point);
		}
		mat = MatrixBuilder.euclidean().scale(1.0/scale).getArray();
		for ( i = 0; i<P.faceList.size(); ++i)	{
			Face wv = P.faceList.get(i);
			Rn.matrixTimesVector(wv.plane, mat, wv.plane);
		}
		P.isDirty = true;
		P.update();
//		System.err.println("polyhedron "+toString());
//		System.err.println("polarize "+P.toString());
		return P;
	}
		
	public WingedEdge polarize(double scale)	{
		WingedEdge polar = new WingedEdge(20.0);
		DataList vertices = getVertexAttributes(Attribute.COORDINATES);
		int nv = vertices.getStorageModel().getDimensions(vertices)[0];
//		LoggingSystem.getLogger(this).log(Level.INFO, "nv: "+nv);
		double[] plane = new double[4];
		for (int i = 0; i<nv; ++i)	{
			Vertex wv = ((Vertex) vertexList.get(i));
			Pn.polarize(plane, wv.point, metric);
			if (metric == Pn.EUCLIDEAN) plane[3] = -scale;
			polar.cutWithPlane(plane, wv.tag);
			LoggingSystem.getLogger(this).log(Level.FINER,"Cutting:\n"+toString());
		}
		return polar;
	}
	
	public String toString()	{
		StringBuffer sb = new StringBuffer();
		sb.append("Winged Edge");
		DataList vertices = getVertexAttributes(Attribute.COORDINATES);
		int nv = getNumPoints(); //vertices.getStorageModel().getDimensions()[0];
		for (int i = 0; i< nv; ++i)	{
			sb.append("v"+i+":"+vertices.get(i).toString()+"\n");
			sb.append("\tnorm = "+Pn.norm(vertexList.get(i).point, metric));
		}
		int[][] indices = getFaceAttributes(Attribute.INDICES).toIntArrayArray(null);
		for (int i = 0; i< indices.length; ++i)	{
			double[] plane = ((Face) faceList.get(i)).plane;
			sb.append("f"+i+": Plane equation"+Rn.toString(plane)+": ");
			for (int j = 0 ; j<indices[i].length; ++j)	sb.append(indices[i][j]+" ");
			sb.append("\n");
			double dd = Rn.innerProduct(plane, plane, 3);
			dd = Math.sqrt(dd);
			sb.append("\tdist = "+Math.abs(plane[3]/dd));
		}
		return sb.toString();
	}
	
	/**
	 * @return
	 */
	public Vector<Edge> getEdgeList() {
		return edgeList;
	}

	/**
	 * @return
	 */
	public Vector<Face> getFaceList() {
		return faceList;
	}

	/**
	 * @return
	 */
	public Vector<Vertex> getVertexList() {
		return vertexList;
	}

	/**
	 * @param vector
	 */
	public void setEdgeList(Vector vector) {
		edgeList = vector;
	}

	/**
	 * @param vector
	 */
	public void setFaceList(Vector vector) {
		faceList = vector;
	}

	/**
	 * @param vector
	 */
	public void setVertexList(Vector vector) {
		vertexList = vector;
	}

	/**
	 * @return
	 */
	public double[][] getColormap() {
		return colormap;
	}

	/**
	 * @param grid
	 */
	public void setColormap(double[][] grid) {
		colormap = grid;
		// TODO figure out how to nullify an attribute setting
		//setFaceAttributes(Attribute.COLORS, null);
		isDirty = true;
		updateColors();
		fireGeometryChanged(null, null, null, null);
	}

	public  int getFirstFaceWithTag( int t)	{
		Face face = null;
		int i;
		for (i = 0; i<numFaces; ++i)	{
			face = (Face) faceList.get(i);
			if (t == face.tag) break;
		}
		if (i == numFaces) return -1;
		return i;
	}

	public double[][] getFaceWithIndex(int i) {
		if (i >= getNumFaces()) 
			throw new IllegalStateException("No such face");
		int[] ind = getFaceAttributes(Attribute.INDICES).item(i).toIntArray(null);
		double[][] verts = new double[ind.length][];
		for (int j = 0; j<ind.length; ++j)	{
			verts[j] = getVertexAttributes(Attribute.COORDINATES).item(ind[j]).toDoubleArray(null);
		}
		return verts;
	}
	
	/**
	 * @return Returns the metric.
	 */
	public int getMetric() {
		return metric;
	}
	/**
	 * @param metric The metric to set.
	 */
	public void setMetric(int metric) {
		this.metric = metric;
		GeometryUtility.setMetric(this, metric);
	}

	public void normalize(double scale) {
		double size = 0;
		double[][] verts = getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		for (int i = 0; i<verts.length; ++i)	{
			size += Pn.norm(verts[i], Pn.EUCLIDEAN);
		}
		size /= verts.length;
		size = scale/size;
//		System.err.println("we: size = "+size);
		double[] mat = MatrixBuilder.euclidean().scale(size).getArray();
		for ( int i = 0; i<vertexList.size(); ++i)	{
			Vertex wv = vertexList.get(i);
			Rn.matrixTimesVector(wv.point, mat, wv.point);
		}
		mat = Rn.diagonalMatrix(null, new double[]{1,1,1,size});
		for ( int i = 0; i<faceList.size(); ++i)	{
			Face wv = faceList.get(i);
			Rn.matrixTimesVector(wv.plane, mat, wv.plane);
		}
		isDirty = true;
		update();
	}

	public boolean isColoredFaces() {
		return coloredFaces;
	}

	public void setColoredFaces(boolean coloredFaces) {
		this.coloredFaces = coloredFaces;
	}
}


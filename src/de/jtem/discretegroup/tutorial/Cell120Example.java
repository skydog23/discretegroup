/*
 * Created on Jun 8, 2012
 *
 */
package de.jtem.discretegroup.tutorial;

import java.util.HashMap;

import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.plugin.JRViewer;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.Color;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.core.DirichletDomain;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.discretegroup.core.DiscreteGroupUtility;
import de.jtem.discretegroup.core.ImportGroup;
import de.jtem.discretegroup.util.WingedEdge;
import de.jtem.discretegroup.util.WingedEdge.Face;

public class Cell120Example {

	DiscreteGroup my120cell;
	private DirichletDomain dirdom;
	public SceneGraphComponent get120Cell()	{
		my120cell = ImportGroup.initFromResource("resources/groups/120cell.gens", Pn.ELLIPTIC); //120cell.gens");		
		my120cell.setFinite(true);
		my120cell.setName("120 cell");
		my120cell.setDimension(3);
		my120cell.setMetric(Pn.ELLIPTIC);
		// just display the identity and the generators
		my120cell.setConstraint(new DiscreteGroupSimpleConstraint(13));
		my120cell.update();
		dirdom = new DirichletDomain(my120cell);
		dirdom.update();
		sandbox();
		DiscreteGroupUtility.addWordLabels((WingedEdge) dirdom.getDirichletDomain());
		SceneGraphComponent willie = SceneGraphUtility.createFullSceneGraphComponent();
		willie.setGeometry(dirdom.getDirichletDomain());
//		MatrixBuilder.euclidean().scale(.2).assignTo(willie);
		willie.getAppearance().setAttribute("polygonShader.diffuseShader", Color.white);
		willie.getAppearance().setAttribute(CommonAttributes.METRIC, Pn.ELLIPTIC);
		willie.getAppearance().setAttribute("useGLSL", true);
		willie.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
		willie.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
//		willie.getAppearance().setAttribute(CommonAttributes.OFFSET, new double[]{0,.3,0});
//		willie.getAppearance().setAttribute(CommonAttributes.ALIGNMENT, SwingConstants.CENTER);
		willie.getAppearance().setAttribute(CommonAttributes.TEXT_SCALE, .01);
		
		DiscreteGroupSceneGraphRepresentation dgsgr = new DiscreteGroupSceneGraphRepresentation(my120cell);
		dgsgr.setWorldNode(willie);
		dgsgr.update();
		return dgsgr.getRepresentationRoot();
	}
	
	public void sandbox()	{
		double[][] verts = dirdom.getDirichletDomain().getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		Pn.dehomogenize(verts, verts);
		dirdom.getDirichletDomain().setVertexAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array().createReadOnly(verts));
		System.err.println("Verts = "+Rn.toString(verts));
		DiscreteGroupElement[] gens = my120cell.getGenerators();
		for (int i = 0; i<12; ++i)	{
			System.err.println("Generator "+gens[i].getWord()+" = \n"+Rn.matrixToString(gens[i].getArray()));
		}
		WingedEdge we = (WingedEdge) dirdom.getDirichletDomain();
		int[][] faceIndices = we.getFaceAttributes(Attribute.INDICES).toIntArrayArray(null);
		int i = 0;
		for (Face face : we.faceList) {
			System.err.println(i+"tag = "+face.tag+" "+((DiscreteGroupElement) face.source).getWord());//+" opposite face: "+face.inverse.tag);
			i++;
		}
		HashMap<Integer, Integer> foo = DirichletDomain.getPairedFaces(we);
		for (i = 0; i<foo.size(); ++i)	{
			int inverse = foo.get(i);
			double[] face1 = getFaceCenter(i, faceIndices, verts);
			double[] face2 = getFaceCenter(inverse, faceIndices, verts);
			DiscreteGroupElement source = (DiscreteGroupElement) we.faceList.get(i).source;
			DiscreteGroupElement sourceI = (DiscreteGroupElement) we.faceList.get(inverse).source;
			double[] m = source.getArray();
			double[] face1b = Rn.matrixTimesVector(null, m, face2);
			System.err.println(i+"face center = "+Rn.toString(face1));
			System.err.println(i+"face center2 = "+Rn.toString(face1b));
		}
	}
	public static void main(String[] argv)	{
		Cell120Example harry = new Cell120Example();
		Viewer v = JRViewer.display(harry.get120Cell());
		CameraUtility.encompass(v);
	}
	
	private double[] getFaceCenter(int k, int[][] indices, double[][] verts)	{
		double[][] vs = new double[indices[k].length][];
		for (int i = 0; i<vs.length; ++i)	{
			vs[i] = verts[indices[k][i]];
		}
		return Rn.average(null, vs);
	}
}


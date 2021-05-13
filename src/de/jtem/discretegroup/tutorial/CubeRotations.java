/*
 * Created on Feb 18, 2010
 *
 */
package de.jtem.discretegroup.tutorial;

import java.awt.Color;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.math.Matrix;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.plugin.JRViewer;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.groups.TriangleGroup;

public class CubeRotations  {

	public SceneGraphComponent makeWorld() {
		SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");
		world.getAppearance().setAttribute("lineShader.diffuseColor", Color.white);
		world.getAppearance().setAttribute("pointShader.diffuseColor", Color.white);
		world.getAppearance().setAttribute("lineShader.tubeRadius", .01);
		world.getAppearance().setAttribute("pointShader.pointRadius", .015);
		world.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, true);
		TriangleGroup cubeRot = TriangleGroup.instanceOfGroup("234");
		
		DiscreteGroupSceneGraphRepresentation dgsgr = new DiscreteGroupSceneGraphRepresentation(cubeRot);
		double[][] pts = cubeRot.getTriangle();//{{0,0,1}, {1,1,1},  {1,-1,1}};
		System.err.println("triangle = "+Rn.toString(pts));
		// project the points onto the unit cube; points by default lie on unit cube
		Pn.setToLength(pts[0], pts[0], 1.414, 0);
		Pn.setToLength(pts[1], pts[1], 1.731, 0);

		// add the center of the triangle as fourth point
		double[][] pts4 = {pts[0], pts[1], pts[2], new double[4]};
		Rn.average(pts4[3], pts);
		Pn.setToLength(pts4[3], pts4[3], 1.5, Pn.EUCLIDEAN);
		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		ifsf.setVertexCount(4);
		ifsf.setVertexCoordinates(pts4);
		ifsf.setFaceCount(3);
		ifsf.setFaceIndices(new int[][]{{0,1,3},{1,2,3},{2,0,3}});
		ifsf.setGenerateEdgesFromFaces(true);
		ifsf.setGenerateFaceNormals(true);
		ifsf.update();
		IndexedFaceSet ifs = ifsf.getIndexedFaceSet();
		// put it into a scene graph
		SceneGraphComponent threeTriangles = SceneGraphUtility.createFullSceneGraphComponent("3");		
		threeTriangles.setGeometry(ifs);
		
		Matrix m = new Matrix(
				P3.makeRotationMatrix(null, pts4[0], pts4[2], Math.PI, Pn.EUCLIDEAN));
		SceneGraphComponent dom = SceneGraphUtility.createFullSceneGraphComponent("fd");
		SceneGraphComponent dom2 = SceneGraphUtility.createFullSceneGraphComponent("fd2");
		dom.addChild(dom2);
		dom2.addChild(threeTriangles);
		// color the two copies differently
		dom2.getAppearance().setAttribute("polygonShader.diffuseColor", Color.red);
		dom.getAppearance().setAttribute("polygonShader.diffuseColor", Color.blue);
		m.assignTo(dom2);
		dom.addChild(threeTriangles);
		dgsgr.setWorldNode(dom);
		dgsgr.update();
		world.addChild(dgsgr.getRepresentationRoot());
		return world;
	}

	public static double[] projectPointOntoPlane(double[] result, double[] point, double[] plane) {
		if (result == null )result = new double[4];
		double ip = -Rn.innerProduct(point, plane);
		double[] nv = plane.clone();
		nv[3] = 0.0;
		Rn.add(result, point, Rn.times(null, ip, nv));
		ip = Rn.innerProduct(result, plane);
		System.err.println("ppop: result = "+ip);
		return result;
	}

	public static void main(String[] args)	{
		CubeRotations cr = new CubeRotations();
		
		Viewer v = JRViewer.display(cr.makeWorld());
		v.getSceneRoot().getAppearance().setAttribute(CommonAttributes.BACKGROUND_COLOR, new Color(20,20,40));
		CameraUtility.encompass(v);
	
	}

}

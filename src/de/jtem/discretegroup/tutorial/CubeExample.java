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


package de.jtem.discretegroup.tutorial;

import charlesgunn.jreality.newtools.FlyTool;
import de.jreality.geometry.GeometryMergeFactory;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.basic.Scene;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;
import de.jreality.util.Secure;
import de.jreality.util.SystemProperties;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.discretegroup.core.DiscreteGroupUtility;
import de.jtem.discretegroup.groups.Platycosm;

public class CubeExample {

	public static void main(String[] args) {
		Secure.setProperty(SystemProperties.JOGL_COPY_CAT, "true");
		// construct a 3d discrete group
		DiscreteGroup dg = Platycosm.instanceOfGroup("c1");
		dg.setConstraint(new DiscreteGroupSimpleConstraint(10000));
		dg.update();
		// create a scene graph representation of the group
		DiscreteGroupSceneGraphRepresentation dgsgr = new DiscreteGroupSceneGraphRepresentation(dg, true);
		// the following sets the time in milliseconds between new display lists, default is 500.
		dgsgr.getDropBox().setDelay(1000);
		// when copycat is enabled, the transforms are handled outside the scene graph
		// the following sets a much smaller set of elements in the scene graph, needed mostly for picking.
		DiscreteGroupSimpleConstraint dgsc = new DiscreteGroupSimpleConstraint(100);
		dgsgr.setOfficialElementList(
				DiscreteGroupUtility.generateElements(dg, dgsc));
		// construct a scene graph component to represent one fundamental domain
		SceneGraphComponent fundDomSGC = SceneGraphUtility.createFullSceneGraphComponent("fundDomSGC");
		fundDomSGC.setGeometry(Primitives.box(.5,.6,.4, true));
		MatrixBuilder.euclidean().translate(.1,.2,.3).assignTo(fundDomSGC);
		// attach it to the scene graph representation
		dgsgr.setWorldNode(fundDomSGC);
		// this will generate a jReality scene graph
		dgsgr.update();
		SceneGraphComponent root = dgsgr.getRepresentationRoot();
		boolean flatten = false;
		if (flatten)	{
	        GeometryMergeFactory mergeFact= new GeometryMergeFactory();             
		     IndexedFaceSet result=mergeFact.mergeGeometrySets(root);
	         root =  SceneGraphUtility.createFullSceneGraphComponent("merged");
	          root.setGeometry(result);
		}
		Appearance ap = root.getAppearance();
		ap.setAttribute(CommonAttributes.EDGE_DRAW, false);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		ap.setAttribute(CommonAttributes.SMOOTH_SHADING, false);
		Viewer v = JRViewer.display(root);
		Scene scene = JRViewer.getLastJRViewer().getPlugin(Scene.class);
		MatrixBuilder.euclidean().translate(0,0,60).assignTo(scene.getAvatarComponent());
		FlyTool flytool = new FlyTool();
		flytool.setGain(1);
		scene.getAvatarComponent().addTool(flytool);
		CameraUtility.encompass(v);
	}

}

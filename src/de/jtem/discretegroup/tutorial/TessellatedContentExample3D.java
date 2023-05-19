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

import java.awt.Color;

import charlesgunn.jreality.plugin.TermesSpherePlugin;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.plugin.JRViewer;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;
import de.jreality.util.SystemProperties;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupColorPicker;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.discretegroup.groups.Platycosm;
import de.jtem.discretegroup.plugin.TessellatedContent;
import de.jtem.discretegroup.util.TranslateTool;

public class TessellatedContentExample3D  {

	DiscreteGroup dg;
	TessellatedContent tessellatedContent = new TessellatedContent();
	static boolean copycat = false;
	
	public SceneGraphComponent getContent()	{
		// construct a scene graph component to represent one fundamental domain
		SceneGraphComponent fundDomSGC = new SceneGraphComponent("fundDomSGC");
		fundDomSGC.setGeometry(Primitives.cube());
		SceneGraphComponent wrapper = new SceneGraphComponent("wrapper");
		wrapper.addChild(fundDomSGC);
		wrapper.addTool(new TranslateTool());
		MatrixBuilder.euclidean().translate(.5,.3,0).scale(.3).assignTo(fundDomSGC);
		return wrapper;
	}
	public static void main(String[] args) {
		System.setProperty(SystemProperties.JOGL_COPY_CAT, copycat ? "true" : "false");
		TessellatedContentExample3D se2d = new TessellatedContentExample3D();
		se2d.doIt();
	}
	
	private void doIt() {
		dg = Platycosm.instanceOfGroup("c3");
		dg.setConstraint(new DiscreteGroupSimpleConstraint(500));
		dg.setColorPicker(new DiscreteGroupColorPicker.RotationColorPicker(3));
		dg.update();
		Appearance red = new Appearance();
		red.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.red);
		red.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.yellow);
		Appearance blue = new Appearance();
		blue.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.blue);
		blue.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.green);
		Appearance green = new Appearance();
		blue.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.green);
		blue.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.yellow);

		
		JRViewer jrv = new JRViewer();
		jrv.addBasicUI();
		tessellatedContent.setupJRViewer(jrv);
		jrv.registerPlugin(new TermesSpherePlugin(false));
		jrv.registerPlugin(tessellatedContent);
		jrv.startup();
		tessellatedContent.setFlySpeed(.5);
		tessellatedContent.setScale(.5);
		tessellatedContent.setFollowsCamera(false);
		tessellatedContent.setClipToCamera(false);
		tessellatedContent.setGroup(dg, copycat);
		tessellatedContent.setContent(getContent());
		tessellatedContent.getTheRepn().setAppList(new Appearance[]{red,blue,green});
		tessellatedContent.getTheRepn().update();
		CameraUtility.getCamera(jrv.getViewer()).setFar(30);
		jrv.getViewer().getSceneRoot().getAppearance().setAttribute("backgroundColors", Appearance.INHERITED);
		jrv.getViewer().getSceneRoot().getAppearance().setAttribute("backgroundColor",new Color(0,0,0,0));
	}

}

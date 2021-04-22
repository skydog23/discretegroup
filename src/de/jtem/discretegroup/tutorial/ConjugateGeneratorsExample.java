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

import de.jreality.math.Matrix;
import de.jreality.math.Rn;
import de.jreality.plugin.JRViewer;
import de.jreality.scene.SceneGraphComponent;
import de.jtem.discretegroup.core.DirichletDomain;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.discretegroup.groups.WallpaperGroup;
/**
 * AN example to show how to change the generators of a "factory" group by an affine transformation.
 * Caution:  the example group remains a discrete group under any affine transformation; most
 * wallpaper groups do not, hence one must be careful when distorting the generators in this way.
 * @author gunn
 *
 */
public class ConjugateGeneratorsExample  {

	DiscreteGroup dg;
	DiscreteGroupSceneGraphRepresentation dgsgr;
	public void doIt()	{
		dg = WallpaperGroup.instanceOfGroup("O");
		dg.setConstraint(new DiscreteGroupSimpleConstraint(6.0, -1, 500));
		DiscreteGroupElement[] gens = dg.getGenerators();
		// cook up a change of basis matrix which moves y-axis to unit vector making 
		// angle of 60 degrees with x-axis.
		Matrix cob = new Matrix();
		cob.setColumn(0, new double[]{1,0,0,0});
		cob.setColumn(1, new double[]{.5, Math.sqrt(3)/2.0,0,0});
		// conjugate generators by this matrix
		for (int i = 0; i<gens.length; ++i)	{
			double[] g = gens[i].getArray();
			g = Rn.conjugateByMatrix(null, g, cob.getArray());
			gens[i].setArray(g);
		}
		dg.update();
		// create a scene graph representation of the group
		dgsgr = new DiscreteGroupSceneGraphRepresentation(dg);
		// construct a scene graph component to represent one fundamental domain
		SceneGraphComponent fundDomSGC = new SceneGraphComponent("fundDomSGC"),
			fundDom2SGC = new SceneGraphComponent("fundDom2SGC");
		fundDomSGC.addChild(fundDom2SGC);
		DirichletDomain dd = new DirichletDomain(dg);
		dd.update();
		// this should be a regular hexagon
		fundDom2SGC.setGeometry(dd.getDirichletDomain());
		fundDomSGC.addTool(new de.jtem.discretegroup.util.TranslateTool());
		dgsgr.setWorldNode(fundDomSGC);
		dgsgr.getRepresentationRoot().getAppearance().setAttribute("polygonShader.diffuseColor", Color.white);
		// this will generate a jReality scene graph
		dgsgr.update();
	}
	public static void main(String[] args) {
		ConjugateGeneratorsExample se2d = new ConjugateGeneratorsExample();
		se2d.doIt();
		JRViewer.display(se2d.dgsgr.getRepresentationRoot());	
	}

}

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

import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.plugin.JRViewer;
import de.jreality.scene.SceneGraphComponent;
import de.jtem.discretegroup.core.DirichletDomain;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.discretegroup.core.DiscreteGroupUtility;
import de.jtem.discretegroup.groups.WallpaperGroup;

public class SimpleExample2D  {

	DiscreteGroup dg;
	DiscreteGroupSceneGraphRepresentation dgsgr;
	public void doIt()	{
		// construct a 3d discrete group
		dg = new DiscreteGroup();
		dg.setMetric(Pn.EUCLIDEAN);	// only indirectly used, when creating various sorts of geometry associated to the group
		dg.setDimension(2);			// ditto
		// create the generators: in this case reflections in the sides of square.
		DiscreteGroupElement[] gens = new DiscreteGroupElement[4];
		double[][] planes = {
			{1,0,0,0},
			{1,0,0,-1},
			{0,1,0,0},
			{0,1,0,-1}};
		for (int i = 0; i<4; ++i)	{
			double[] matrix =  MatrixBuilder.euclidean().reflect(planes[i]).getArray();
			gens[i] = new DiscreteGroupElement( Pn.EUCLIDEAN, matrix, DiscreteGroupUtility.genNames[i]);
		}
		dg.setGenerators(gens);
		// set up a constraint that generates only 50 group elements
		dg.setConstraint(new DiscreteGroupSimpleConstraint(50));
		dg.update();
		dg = WallpaperGroup.instanceOfGroup("O");
		dg.setConstraint(new DiscreteGroupSimpleConstraint(50));
		gens = dg.getGenerators();
		Matrix cob = new Matrix();
		cob.setColumn(0, new double[]{1,0,0,0});
		cob.setColumn(1, new double[]{.5, Math.sqrt(3)/2.0,0,0});
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
		dg.setCenterPoint(new double[]{.5,.5,0,1});
		DirichletDomain dd = new DirichletDomain(dg);
		dd.update();
		fundDom2SGC.setGeometry(dd.getDirichletDomain());//Primitives.regularPolygon(4,.5));
		fundDomSGC.addTool(new de.jtem.discretegroup.util.TranslateTool());
//		MatrixBuilder.euclidean().translate(.5,.3,0).scale(.2).assignTo(fundDom2SGC);
		// attach it to the scene graph representation
		dgsgr.setWorldNode(fundDomSGC);
		// this will generate a jReality scene graph
		dgsgr.update();
	}
	public static void main(String[] args) {
		SimpleExample2D se2d = new SimpleExample2D();
		se2d.doIt();
		JRViewer.display(se2d.dgsgr.getRepresentationRoot());	
	}

}

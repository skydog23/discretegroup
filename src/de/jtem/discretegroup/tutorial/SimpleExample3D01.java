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

import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.plugin.JRViewer;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.tools.RotateTool;
import de.jtem.discretegroup.core.DirichletDomain;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;

public class SimpleExample3D01 {
	static boolean infiniteGroup = true;
	public static void main(String[] args) {
		// construct a 3d discrete group
		DiscreteGroup dg = new DiscreteGroup();
		dg.setMetric(Pn.EUCLIDEAN);	// only indirectly used, when creating various sorts of geometry associated to the group
		dg.setDimension(3);			// ditto
		dg.setConstraint(new DiscreteGroupSimpleConstraint(20));
		DiscreteGroupElement[] gens = null;
		if (infiniteGroup)	 {
			dg.setFinite(false);			// this is a 'hint' that can help optimize the group element generation
			gens = new DiscreteGroupElement[4];
			// create the generators: in this case reflections in the faces of a tetrahedron that is 1/48 of a cube
			double[] xplane = {0,0,1,-1},
					yplane = {0,-1,0,0},
					zplane = {1,0,-1,0},
					wplane = {-1,1,0,0};
			gens[0] = new DiscreteGroupElement( Pn.EUCLIDEAN, MatrixBuilder.euclidean().reflect(xplane).getArray(), "x");
			gens[1] = new DiscreteGroupElement( Pn.EUCLIDEAN, MatrixBuilder.euclidean().reflect(yplane).getArray(), "y");
			gens[2] = new DiscreteGroupElement( Pn.EUCLIDEAN, MatrixBuilder.euclidean().reflect(zplane).getArray(), "z");
			gens[3] = new DiscreteGroupElement( Pn.EUCLIDEAN, MatrixBuilder.euclidean().reflect(wplane).getArray(), "w");
		} else {
			dg.setFinite(true);			// this is a 'hint' that can help optimize the group element generation
			// create the generators: in this case reflections in the three coordinate axes.
			gens = new DiscreteGroupElement[3];
			// create the generators: in this case reflections in the three coordinate axes.
			double[] xplane = {1,0,0,0},
				yplane = {0,1,0,0},
				zplane = {0,0,1,0};
			gens[0] = new DiscreteGroupElement( Pn.EUCLIDEAN, MatrixBuilder.euclidean().reflect(xplane).getArray(), "x");
			gens[1] = new DiscreteGroupElement( Pn.EUCLIDEAN, MatrixBuilder.euclidean().reflect(yplane).getArray(), "y");
			gens[2] = new DiscreteGroupElement( Pn.EUCLIDEAN, MatrixBuilder.euclidean().reflect(zplane).getArray(), "z");
		}
		dg.setGenerators(gens);
		// now generate the group elements, in this simple case we get them all without having to specify anything
		dg.update();
		DirichletDomain dd = new DirichletDomain(dg);
		dg.setCenterPoint(new double[]{.1,.2,.3});
		dd.update();
		// create a scene graph representation of the group
		DiscreteGroupSceneGraphRepresentation dgsgr = new DiscreteGroupSceneGraphRepresentation(dg);
		// construct a scene graph component to represent one fundamental domain
		SceneGraphComponent fundDomSGC = new SceneGraphComponent("fundDomSGC");
		fundDomSGC.setGeometry(dd.getDirichletDomain());
		fundDomSGC.addTool(new RotateTool());
//		MatrixBuilder.euclidean().scale(.5).assignTo(fundDomSGC);
		// attach it to the scene graph representation
		dgsgr.setWorldNode(fundDomSGC);
		// this will generate a jReality scene graph
		dgsgr.update();

		JRViewer.display(dgsgr.getRepresentationRoot());
		
	}

}

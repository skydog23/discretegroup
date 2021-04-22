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
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.tools.RotateTool;
import de.jtem.discretegroup.core.DirichletDomain;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.discretegroup.core.DiscreteGroupUtility;
public class ConstraintExample  {

	DiscreteGroup dg;
	DiscreteGroupSceneGraphRepresentation dgsgr;
	public void doIt()	{
		// construct a 3d discrete group
		dg = new DiscreteGroup();
		dg.setMetric(Pn.EUCLIDEAN);	// only indirectly used, when creating various sorts of geometry associated to the group
		dg.setDimension(2);			// ditto
		// create the generators: in this case translations in two non-perpendicular directions
		DiscreteGroupElement[] gens = new DiscreteGroupElement[4];
		double[][] tlates = {{1,0,0},{.5,1,0}};
		for (int i = 0; i<2; ++i)	{
			double[] matrix =  MatrixBuilder.euclidean().translate(tlates[i]).getArray();
			gens[i] = new DiscreteGroupElement( Pn.EUCLIDEAN, matrix, DiscreteGroupUtility.genNames[i]);
			gens[i+2] = gens[i].getInverse();
		}
		dg.setGenerators(gens);
		// set up a constraint that accepts a group element only when:
		// 	it moves the origin less than 6 units
		//  its wordlength (in the generators) is less than or equal to 8
		//  fewer than 200 group elements have been generated
		DiscreteGroupSimpleConstraint dgsc = new DiscreteGroupSimpleConstraint(6.0, -1, 200);
		// uncomment out the following 
		dgsc.setManhattan(true);
		dg.setConstraint(dgsc);
		dg.update();
		// create a scene graph representation of the group
		dgsgr = new DiscreteGroupSceneGraphRepresentation(dg);
		// construct a scene graph component to represent one fundamental domain
		SceneGraphComponent fundDomSGC = new SceneGraphComponent("fundDomSGC");
		fundDomSGC.setTransformation(new Transformation());
		DirichletDomain dirdom = new DirichletDomain(dg);
		dirdom.update();
		IndexedFaceSet fd = dirdom.getDirichletDomain();
//		IndexedFaceSet fd = DiscreteGroupUtility.calculateDirichletDomain(null, dg);
		fundDomSGC.setGeometry(fd);
		// add a rotate tool since for this group, translating the tile is the same as translating the world
		fundDomSGC.addTool(new RotateTool());
		// attach it to the scene graph representation
		dgsgr.setWorldNode(fundDomSGC);
		// this will generate a jReality scene graph
		dgsgr.update();
	}
	public static void main(String[] args) {
		ConstraintExample cex = new ConstraintExample();
		cex.doIt();
		JRViewer.display(cex.dgsgr.getRepresentationRoot());
		
	}

}

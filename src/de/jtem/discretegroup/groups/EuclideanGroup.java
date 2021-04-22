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


package de.jtem.discretegroup.groups;

import de.jreality.math.Matrix;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupConstraint;
import de.jtem.discretegroup.core.DiscreteGroupElement;

/**
 * @author Charles Gunn
 *
  */

public class EuclideanGroup extends DiscreteGroup {


	public static final int COB_ROTATE = 1;
	public static final int COB_XSCALE = 2;
	public static final int COB_YSCALE = 4;
	public static final int COB_ZSCALE = 8;
	public static final int COB_SHEAR = 6;
	public static final int COB_SCALE = 16;
	
	WallpaperGroup translationSubgroup;
	DiscreteGroupElement[] oneCell;
	protected int allowedChangeOfBasis;
		
	public EuclideanGroup() {			
		super();
	}

	protected void init()	{
		super.init();
		metric = Pn.EUCLIDEAN;
		centerPoint = (double[]) P3.originP3.clone();
	}
	

	
//	protected void generateElements()	{
//		if (translationSubgroup == null || oneCell == null)	{
//			super.generateElements();
//			return;
//			} 
//		// else ...
//		if (hasChanged) translationSubgroup.hasChanged = true;
//		hasChanged = false;
//		translationSubgroup.generateElements();
//		elementList = DiscreteGroupElement.cartesianProduct(elementList, translationSubgroup.getElementList(), oneCell);
//		if (colorPicker != null) colorPicker.assignColorIndices(elementList);
//	}
	
	/*(
	public SceneGraphComponent representAsSceneGraph(SceneGraphComponent cob, List geometry)	{
		if (translationSubgroup == null || oneCell == null)	{
			return super.representAsSceneGraph(cob, geometry);
			} 
		if (cob == null) {
			cob = new SceneGraphComponent(); 
			cob.setName("Change of Basis");
		} //else 
		cob.getTransformAt(0).setMatrix(changeOfBasis.getMatrix());
		if (theSceneGraphRepn == null)	{
			theSceneGraphRepn = new CallBack();
			theSceneGraphRepn.setName("Reprn");
		}
		if (cellKit == null)	{
			cellKit = new SceneGraphComponent();
			cellKit.setName("cellKit");
			cellKit.setTransforms(oneCell);
		}
		cob.removeChildren();
		cob.addChild(theSceneGraphRepn);
		theSceneGraphRepn.setTransforms(translationSubgroup.getTransforms());
		theSceneGraphRepn.removeChildren();
		theSceneGraphRepn.addChild(cellKit);
		//cellKit.removeChildren();
		//cellKit.getChildren().addAll(geometry);
		cellKit.setChildren(geometry);
		return cob;
	}
	*/

	/* (non-Javadoc)
	 * @see discreteGroup.DiscreteGroup#setChangeOfBasis(charlesgunn.gv2.Transformation)
	 */
	public void setChangeOfBasis(Matrix transform) {
		super.setChangeOfBasis(transform);
		if (translationSubgroup != null)
			translationSubgroup.setChangeOfBasis(getChangeOfBasis());
	}

	/* (non-Javadoc)
	 * @see discreteGroup.DiscreteGroup#setConstraint(discreteGroup.DiscreteGroupConstraint)
	 */
	public void setConstraint(DiscreteGroupConstraint constraint) {
		// TODO Auto-generated method stub
		super.setConstraint(constraint);
		if (translationSubgroup != null)
			translationSubgroup.setConstraint(constraint);	
	}

	public int getAllowedChangeOfBasis() {
		return allowedChangeOfBasis;
	}
	public void setAllowedChangeOfBasis(int allowedChangeOfBasis) {
		this.allowedChangeOfBasis = allowedChangeOfBasis;
	}
}

/*
 * Created on 24 Apr 2023
 *
 */
package de.jtem.discretegroup.core;

import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.SceneGraphUtility;

public class SimpleDGSGR extends AbstractDGSGR {

	DiscreteGroupElement[] els = null;
	SceneGraphComponent root = SceneGraphUtility.createFullSceneGraphComponent("simple dgsgr"),
			fundDom = SceneGraphUtility.createFullSceneGraphComponent("fund domain");
	DiscreteGroupConstraint constraint = null;
	Appearance[] aplist = null;
	private boolean dirty = true;
	
	public SimpleDGSGR()	{
		super();
	}
	
	public SimpleDGSGR(DiscreteGroup g)	{
		super();
		setElementList(g.getElementList());
	}
	
	@Override
	public void setElementList(DiscreteGroupElement[] list) {
		els = list;
	}

	@Override
	public DiscreteGroupElement[] getElementList() {
		return els;
	}

	@Override
	public SceneGraphComponent getRepresentationRoot() {
		return root;
	}

	@Override
	public SceneGraphComponent getSceneGraphRepn() {
		return root;
	}

	@Override
	public SceneGraphComponent getFundamentalRegion() {
		return fundDom;
	}

	@Override
	public void setConstraint(DiscreteGroupConstraint c) {
		constraint = c;
		if (constraint != null) AbstractDGSGR.applyConstraint(this, constraint);
		else {
			int n = root.getChildComponentCount();
			for (int i = 0; i<n;++i) {
				root.getChildComponent(i).setVisible(true);
			}
		}
	}
	
	@Override
	public DiscreteGroupConstraint getConstraint() {
		return constraint;
	}
	
	@Override
	public void setAppList(Appearance[] aplist) {
		this.aplist = aplist;
		dirty = true;
	}

	public void update() {
		// only do anything when the # of children of the root
		// is different from the # of elements in the list
		// this allows the user to set up the scene graph himself
		if (els == null) return;
		boolean mismatch = (root.getChildComponentCount() != els.length);
		if (!dirty && !mismatch) return;
		if (mismatch) root.removeAllChildren();
		int n = els.length;
		SceneGraphComponent child = null;
		for (int i = 0; i < n; ++i) {
//			System.err.println("dge word = "+els[i].getWord());
			if (mismatch) {
				child = SceneGraphUtility.createFullSceneGraphComponent("dge "+els[i].getWord());
				root.addChild(child);
			} else {
				child = root.getChildComponent(i);
				child.setVisible(true);
			}
			els[i].getMatrix().assignTo(child);
			if (aplist != null) {
				child.setAppearance(aplist[els[i].getColorIndex()]);
				System.err.println("setting appearance to "+els[i].getColorIndex());
			}
			child.addChild(fundDom);
		}
		if (constraint != null) AbstractDGSGR.applyConstraint(this, constraint);
		dirty = false; 
	}
	
}

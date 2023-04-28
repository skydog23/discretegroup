package de.jtem.discretegroup.core;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;

/*
 * Created on 24 Apr 2023
 *
 */

public abstract class AbstractDGSGR {

	public AbstractDGSGR() {};
	
	public abstract void setElementList(DiscreteGroupElement[] list);
	
	public abstract DiscreteGroupElement[] getElementList();
	
	// this is the highest level SGC in the representation
	public abstract SceneGraphComponent getRepresentationRoot();
	
	// this is the lowest level, containing the group element copies
	public abstract SceneGraphComponent getSceneGraphRepn();
	
	// this is the scene graph which is copied
	public abstract SceneGraphComponent getFundamentalRegion();
	
	public abstract void setConstraint(DiscreteGroupConstraint c);
	
	public abstract DiscreteGroupConstraint getConstraint();
	
	public abstract void setAppList(Appearance[] aplist);
	
	public void update() {};
	
	public static void applyConstraint(AbstractDGSGR dgsgr, DiscreteGroupConstraint c) {
		SceneGraphComponent root = dgsgr.getSceneGraphRepn();
		if (root == null) return;
		int n = root.getChildComponentCount();
		DiscreteGroupElement dge = new DiscreteGroupElement();
		// a terrible hack
		if (c instanceof DiscreteGroupSimpleConstraint) {
			((DiscreteGroupSimpleConstraint) c).setUseCount(true);
			((DiscreteGroupSimpleConstraint) c).reset();
		}
		for (int i = 0; i<n; ++i)	{
			SceneGraphComponent child =  root.getChildComponent(i);
			double[] m = child.getTransformation().getMatrix();
			dge.setArray(m);
			String split[] = child.getName().split(" ");
			if (split.length == 1) dge.setWord("");
			else dge.setWord(split[1]);
			child.setVisible( c.acceptElement(dge) ? true : false);
		}
		
	}
}

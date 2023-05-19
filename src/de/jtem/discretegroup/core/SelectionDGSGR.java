/*
 * Created on 5 May 2023
 *
 */
package de.jtem.discretegroup.core;

import java.util.ArrayList;
import java.util.List;

import charlesgunn.jreality.SelectionComponent;
import de.jreality.scene.SceneGraphComponent;

public class SelectionDGSGR extends SimpleDGSGR {

	SelectionComponent selectionSGC = new SelectionComponent();
	List<AbstractDGSGR> dgsgrList = new ArrayList<AbstractDGSGR>();
	AbstractDGSGR selectedDGR;
	
	public SelectionDGSGR() {
		super();
		selectionSGC.setName("SelectionDGSGR");
	}
	
	public void addSGR(AbstractDGSGR a) {
		dgsgrList.add(a);
		selectionSGC.addChild(a.getRepresentationRoot());
		if (selectedDGR == null) selectedDGR = a;
	}
	
	public void removeSGC(AbstractDGSGR a) {
		dgsgrList.remove(a);
		selectionSGC.removeChild(a.getRepresentationRoot());
	}
	
	public void setSelected(int i) {
		selectionSGC.setSelectedChild(i);
		selectedDGR = dgsgrList.get(i);
	}
	
	public int getSelected() {
		return selectionSGC.getSelectedChild();
	}

	@Override
	public SceneGraphComponent getRepresentationRoot() {
		return selectionSGC;
	}

	@Override
	public SceneGraphComponent getSceneGraphRepn() {
		return selectionSGC.getSelectedChildAsSGC();
	}

	@Override
	public DiscreteGroupElement[] getElementList() {
		return selectedDGR.getElementList();
	}

	@Override
	public SceneGraphComponent getFundamentalRegion() {
		return selectedDGR.getFundamentalRegion();
	}

	@Override
	public DiscreteGroupConstraint getConstraint() {
		return selectedDGR.getConstraint();
	}
	
	
}

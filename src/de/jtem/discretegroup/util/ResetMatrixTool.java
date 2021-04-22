package de.jtem.discretegroup.util;

import de.jreality.math.MatrixBuilder;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;

public class ResetMatrixTool extends AbstractTool{
	
	public ResetMatrixTool(InputSlot ... act) {
		super(act);
	}

    transient protected SceneGraphComponent comp;
	@Override
    public void activate(ToolContext tc) {
      comp = tc.getRootToToolComponent().getLastComponent();
      MatrixBuilder.euclidean().assignTo(comp);
      System.err.println("In activate");
    }
//	@Override
//    public void perform(ToolContext tc) {
//      comp = tc.getRootToToolComponent().getLastComponent();
//      MatrixBuilder.euclidean().assignTo(comp);
//      System.err.println("In perform");
//    }
}

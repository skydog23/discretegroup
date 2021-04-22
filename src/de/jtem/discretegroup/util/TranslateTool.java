package de.jtem.discretegroup.util;

import de.jreality.math.Matrix;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.jreality.tools.DraggingTool;

public class TranslateTool extends DraggingTool {

    static InputSlot activationSlot = InputSlot.getDevice("RotateActivation");
    static InputSlot evolutionSlot = InputSlot.getDevice("PointerEvolution");
    
    public TranslateTool() {
        super(activationSlot);
        addCurrentSlot(evolutionSlot);
    }
    
    public TranslateTool(InputSlot ... activation)	{
    	super(activation);
    	addCurrentSlot(evolutionSlot);
    }
	@Override
	public void perform(ToolContext tc) {
		// TODO Auto-generated method stub
		super.perform(tc);
		if (dragInViewDirection) return;
		result.getArray()[11] = 0.0; // force z-translation to be zero
		if (metric == Pn.EUCLIDEAN)	{
			double[] tmp = Rn.identityMatrix(4);
			tmp[3] = result.getArray()[3];
			tmp[7] = result.getArray()[7];
			result = new Matrix(tmp);
		}
	    comp.getTransformation().setMatrix(result.getArray());
	}

}

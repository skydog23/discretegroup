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


package de.jtem.discretegroup.plugin;

import java.awt.Event;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingConstants;

import de.jreality.plugin.basic.View;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.discretegroup.util.TextSlider;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;

public class DiscreteGroupSimpleConstraintSP extends ShrinkPanelPlugin {


	public DiscreteGroupSimpleConstraintSP() {
		shrinkPanel.setName("Simple constraint");
		setupGUI();
//		this.tc = tc;
	}

	double maxD = 16.0;
	int maxW = 16;	
	int maxNumElements = 3000;
	DiscreteGroupSimpleConstraint constraint = new DiscreteGroupSimpleConstraint();
	View view;
	TessellatedContent tc;
	private TextSlider<Integer> maxWSl;
	private TextSlider<Double> maxDSl;
	private TextSlider<Double> maxNSl;
	
	protected void setupGUI()
	{

		Insets insets = new Insets(1,0,1,0);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.insets = insets;
		c.weighty = 0.0;
		c.anchor = GridBagConstraints.WEST;
		
		shrinkPanel.setLayout(new GridBagLayout());
		
		maxWSl = new TextSlider.Integer("Word length:",
				SwingConstants.HORIZONTAL,0,30,maxW);
		maxWSl.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				maxW = maxWSl.getValue().intValue();
				System.err.println("Setting maxW  to "+maxW);
				constraint.setMaxWordLength(maxW);
				fireSimpleConstraintChanged();
			}
			
		});
					
		maxDSl = new TextSlider.DoubleLog("Max. distance:",
				SwingConstants.HORIZONTAL,.1,30,maxD);
		maxDSl.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				maxD = maxDSl.getValue().doubleValue();
				System.err.println("Setting maxD  to "+maxD);
				constraint.setMaxDistance(maxD);
				System.err.println("maxD = "+constraint.getMaxDistance());
				fireSimpleConstraintChanged();
			}
			
		});
		
		
		maxNSl = new TextSlider.IntegerLog("Total number:",
				SwingConstants.HORIZONTAL, 1, 10000, maxNumElements);
		maxNSl.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				maxNumElements = maxNSl.getValue().intValue();
				System.err.println("Setting maxN  to "+maxNumElements);
				constraint.setMaxNumberElements(maxNumElements);
				fireSimpleConstraintChanged();
			}
			
		});
		
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		shrinkPanel.add(maxWSl, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		shrinkPanel.add(maxDSl, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		shrinkPanel.add(maxNSl, c);
		
	}
		
	public void setConstraint(DiscreteGroupSimpleConstraint constraint) {
		this.constraint = constraint;
		maxWSl.setValue(constraint.getMaxWordLength());
		maxDSl.setValue(constraint.getMaxDistance());
		maxNSl.setValue((double)constraint.getMaxNumberElements());
	}


	public DiscreteGroupSimpleConstraint getConstraint() {
		//constraint.getGraphicsContext().setAspectRatio(CameraUtility.getAspectRatio(view.getViewer()));
		return constraint;
	}

	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		// TODO Auto-generated method stub
		return View.class;
	}

	public static interface SimpleConstraintChangedListener {
		
		public void simpleConstraintChanged(Event e);
		
	}
	
	protected List<SimpleConstraintChangedListener>
		listeners = new LinkedList<SimpleConstraintChangedListener>();
	
	public synchronized void fireSimpleConstraintChanged(Event cce) {
		for (SimpleConstraintChangedListener l : listeners) {
			l.simpleConstraintChanged(cce);
		}
	}
	
	
	public synchronized void fireSimpleConstraintChanged() {
		Event cce = new Event(this, 0, null);
		for (SimpleConstraintChangedListener l : listeners) {
			l.simpleConstraintChanged(cce);
		}
	}
	
	
	public synchronized boolean addSimpleConstraintChangedListener(SimpleConstraintChangedListener l) {
		return listeners.add(l);
	}
	
	public synchronized boolean removeSimpleConstraintChangedListener(SimpleConstraintChangedListener l) {
		return listeners.remove(l);
	}
	

	
}

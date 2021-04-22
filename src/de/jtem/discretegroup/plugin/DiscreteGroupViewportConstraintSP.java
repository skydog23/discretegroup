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
import de.jtem.discretegroup.core.DiscreteGroupViewportConstraint;
import de.jtem.discretegroup.util.TextSlider;
import de.jtem.discretegroup.util.TextSlider.DoubleLog;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;

public class DiscreteGroupViewportConstraintSP extends ShrinkPanelPlugin {

	private static final long serialVersionUID = 1L;

	double minD = 2.0, maxD = 12.0,
			ztlate = .5, fudge = 1.2;
	int minW = 2, maxW = 12;	// word lengths for constraint: not currently used
	int maxNumElements = 1500;
	private DiscreteGroupViewportConstraint constraint = new DiscreteGroupViewportConstraint(3.0,2,12.0,30, null);

	private DoubleLog minDSl;
	private DoubleLog maxDSl;
	private TextSlider.Double zTlateSlider, fudgeSlider;
	
	public DiscreteGroupViewportConstraintSP() {
		shrinkPanel.setName("Viewport constraint");
		constraint.setZtlate(ztlate);
		constraint.setFudge(fudge);
		setupGUI();
	}

	protected void setupGUI()
	{

		Insets insets = new Insets(1,0,1,0);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.insets = insets;
		c.weighty = 0.0;
		c.anchor = GridBagConstraints.WEST;
		
		shrinkPanel.setLayout(new GridBagLayout());
		
		minDSl = new TextSlider.DoubleLog("Min. distance:",
				SwingConstants.HORIZONTAL,.1,10,minD);
		minDSl.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				minD = minDSl.getValue().doubleValue();
				System.err.println("Setting minD  to "+minD);
				constraint.setMinDistance(minD);
				constraint.update();
				fireViewportConstraintChanged();
//				tc.updateViewportConstraint();
			}
			
		});
		maxDSl = new TextSlider.DoubleLog("Max. distance:",
				SwingConstants.HORIZONTAL,.1,30,maxD);
		maxDSl.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				maxD = maxDSl.getValue().doubleValue();
				System.err.println("Setting maxD  to "+maxD);
				constraint.setMaxDistance(maxD);
				constraint.update();
				System.err.println("maxD = "+constraint.getMaxDistance());
				fireViewportConstraintChanged();
//				tc.updateViewportConstraint();
			}
			
		});
		

				
//		zTlateSlider = new TextSlider.Double("Z-tlate:", SwingConstants.HORIZONTAL, .01, 3, ztlate);
//		zTlateSlider.addActionListener(new ActionListener() {
//
//			public void actionPerformed(ActionEvent arg0) {
//				double zTlate = zTlateSlider.getValue().doubleValue();
//				System.err.println("Setting ztlate to: "+zTlate);
//				constraint.setZtlate(zTlate);
//				constraint.update();
//				fireViewportConstraintChanged();
////				tc.updateViewportConstraint();
//			}
//			
//		});

		fudgeSlider = new TextSlider.Double("fudge", SwingConstants.HORIZONTAL, 1, 3, fudge);
		fudgeSlider.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				fudge = fudgeSlider.getValue().doubleValue();
				System.err.println("Setting fudge to: "+fudge);
				constraint.setFudge(fudge);
				constraint.update();
				fireViewportConstraintChanged();
//				tc.updateViewportConstraint();
			}
			
		});

		
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		shrinkPanel.add(minDSl, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		shrinkPanel.add(maxDSl, c);
//		c.gridwidth = GridBagConstraints.REMAINDER;
//		c.weightx = 1.0;
//		shrinkPanel.add(zTlateSlider, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		shrinkPanel.add(fudgeSlider, c);
		
	}
		
	public void setConstraint(DiscreteGroupViewportConstraint constraint) {
		this.constraint = constraint;
		minDSl.setValue(constraint.getMinDistance());
		maxDSl.setValue(constraint.getMaxDistance());
//		zTlateSlider.setValue(constraint.getZtlate());
	}

	public void update()	{
		minDSl.setValue(constraint.getMinDistance());
		maxDSl.setValue(constraint.getMaxDistance());
		zTlateSlider.setValue(constraint.getZtlate());		
	}

	public DiscreteGroupViewportConstraint getConstraint() {
		//constraint.getGraphicsContext().setAspectRatio(CameraUtility.getAspectRatio(view.getViewer()));
		return constraint;
	}

	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}
	
	public static interface ViewportConstraintChangedListener {
		
		public void viewportConstraintChanged(Event e);
		
	}
	
	protected List<ViewportConstraintChangedListener>
		listeners = new LinkedList<ViewportConstraintChangedListener>();
	
	public synchronized void fireViewportConstraintChanged(Event cce) {
		for (ViewportConstraintChangedListener l : listeners) {
			l.viewportConstraintChanged(cce);
		}
	}

	public synchronized void fireViewportConstraintChanged() {
		Event cce = new Event(this, 0, null);
		for (ViewportConstraintChangedListener l : listeners) {
			l.viewportConstraintChanged(cce);
		}
	}
	
	public synchronized boolean addViewportConstraintChangedListener(ViewportConstraintChangedListener l) {
		return listeners.add(l);
	}
	
	public synchronized boolean removeViewportConstraintChangedListener(ViewportConstraintChangedListener l) {
		return listeners.remove(l);
	}

	
}

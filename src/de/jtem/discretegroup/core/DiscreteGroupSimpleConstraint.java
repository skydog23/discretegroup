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


package de.jtem.discretegroup.core;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jtem.discretegroup.util.TextSlider;

/**
 * @author Charles Gunn
 *
 */
public class DiscreteGroupSimpleConstraint implements DiscreteGroupConstraint {

	public double maxDistance = -1;
	public int maxWordLength = -1;
	int maxNumberElements = 1500;
	protected double[] centerPoint = {0,0,0,1};
	boolean manhattan = false;
	private int accepted = 0;
	private boolean countAccepted = false;
	
	public static int globalMaxNumberElements = 1500;
	private static int debug = 0;
	public static DiscreteGroupSimpleConstraint defaultConstraint = new DiscreteGroupSimpleConstraint();
	
	protected transient double[] tmp = new double[4];

	public DiscreteGroupSimpleConstraint()	{
		this(-1, -1, globalMaxNumberElements);
	}
	public DiscreteGroupSimpleConstraint(int max) {
		this(-1, -1, max);
	}

	public DiscreteGroupSimpleConstraint(double d, int m) {
		this(d, m, globalMaxNumberElements );
	}
	
	public DiscreteGroupSimpleConstraint(double d, int m, int max) {
		super();
		maxDistance = d;
		maxWordLength = m;
		maxNumberElements = max;
//		System.err.println("d+m+max="+d+" "+m+" "+max);
	}

	public void setManhattan(boolean b)	{
		manhattan = b;
	}

	public boolean acceptElement(DiscreteGroupElement dge) {
		if (accepted >= maxNumberElements) return false;
		if (maxDistance < 0 && maxWordLength < 0) return true;
		if (maxWordLength >= 0)	{
			boolean accept = dge.getWord().length() <= maxWordLength;
//			System.err.println(dge.getWord()+" "+accept);
			if (!accept) return accept;
		}
		if (maxDistance >= 0)	{
			double[] mat = dge.getArray();
			//tmp[0] = mat[3];  tmp[1] = mat[7];  tmp[2] = mat[11];  tmp[3] = mat[15];
			tmp = Rn.matrixTimesVector(tmp, mat, centerPoint);
			Pn.dehomogenize(tmp, tmp);
			double d = 0;
			if (!manhattan)
				d = Pn.distanceBetween(tmp, centerPoint, dge.getMetric());
			else {
				double[] diff =  Rn.abs(null, Rn.subtract(null, tmp, centerPoint));
//				if (diff[0] < 0 || diff[1] < 0 || diff[2] < 0) return false;
				d = Math.max(diff[0], Math.max(diff[1], diff[2]));				
			}
//			System.err.println("Word, dist: "+dge.getWord()+" "+d+" "+(d>maxDistance));
			if (d > maxDistance) return false;
		}
		if (countAccepted) accepted++;
		return true;
	}
	
	public void setUseCount(boolean b) {
		countAccepted = b;
	}
	
	public void reset() {
		accepted = 0;
	}
	
	public int getMaxNumberElements() {
		return maxNumberElements;
	}

	public void setMaxNumberElements(int i) {
		maxNumberElements = i;
		broadcastChange();
	}

	public double[] getCenterPoint() {
		return centerPoint;
	}

	public void setCenterPoint(double[] centerPoint) {
		this.centerPoint[3] = 1.0;
		System.arraycopy(centerPoint, 0, this.centerPoint, 0, centerPoint.length);
//		System.err.println("Setting centerpoint to "+Rn.toString(this.centerPoint));
		broadcastChange();
	}

	public void update() {
	}
	public double getMaxDistance() {
		return maxDistance;
	}
	public void setMaxDistance(double maxDistance) {
		this.maxDistance = maxDistance;
		System.err.println("max dist = "+maxDistance);
		broadcastChange();
	}
	public int getMaxWordLength() {
		return maxWordLength;
	}
	public void setMaxWordLength(int maxWordLength) {
		this.maxWordLength = maxWordLength;
		broadcastChange();
	}
	
	public Component getInspector() {
		Box container = Box.createVerticalBox();
		container.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5),
				BorderFactory.createTitledBorder(BorderFactory
						.createEtchedBorder(), "Discrete group simple constraint")));

		Box hbox = Box.createHorizontalBox();
		container.add(hbox);
		JCheckBox manB = new JCheckBox("Manhattan word metric");
		hbox.add(manB);
		manB.setSelected(manhattan);
		manB.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				manhattan = (((JCheckBox)e.getSource()).isSelected());
				broadcastChange();
			}
			
		});
		
		final TextSlider.Double rtSlider = new TextSlider.Double("max distance",
				SwingConstants.HORIZONTAL, -1.0, 12.0, maxDistance);
		rtSlider.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				maxDistance = rtSlider.getValue();
				broadcastChange();
			}
		});
		container.add(rtSlider);
		final TextSlider.Integer lSlider = new TextSlider.Integer("max word length",
				SwingConstants.HORIZONTAL, -1, 20, maxWordLength);
		lSlider.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				maxWordLength = lSlider.getValue();
				broadcastChange();
			}
		});
		container.add(lSlider);
		final TextSlider.DoubleLog numSlider = new TextSlider.DoubleLog("max number elements",
				SwingConstants.HORIZONTAL, 1.0, 5000.0, (double) (maxNumberElements));
		numSlider.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				maxNumberElements = numSlider.getValue().intValue();
				broadcastChange();
			}
		});
		container.add(numSlider);
		return container;

	}

	List<ActionListener> listeners = new  Vector<ActionListener>();
	public void addListener(ActionListener l)	{
		listeners.add(l);
	}
	
	public void removeListener(ActionListener l)	{
		listeners.remove(l);
	}
	
	public void broadcastChange() 	{	
		if (!listeners.isEmpty())	{
			for (int i = 0; i<listeners.size(); ++i)	{
				ActionListener al = listeners.get(i);
				al.actionPerformed(new ActionEvent(this, 0, null));
			}
		}
	}

}

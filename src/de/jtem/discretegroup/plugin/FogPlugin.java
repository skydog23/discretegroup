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

import static de.jreality.shader.CommonAttributes.FOG_DENSITY;
import static de.jreality.shader.CommonAttributes.FOG_ENABLED;
import static de.jreality.shader.CommonAttributes.FOG_MODE;
import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.REMAINDER;
import static java.awt.GridBagConstraints.WEST;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import de.jreality.plugin.basic.View;
import de.jreality.scene.Viewer;
import de.jreality.util.Color;
import de.jtem.discretegroup.util.TextSlider;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;

public class FogPlugin extends ShrinkPanelPlugin {

	boolean fogEnabled = true;
	double fogDensity = .1, fogBegin = 1.0, fogEnd = 5.0;
	Color fogColor = Color.black;
	int fogMode = 1;
	Viewer viewer;
	
	public FogPlugin()	{
	}

	public void setDensity(double d) {
		fogDensity = d;
	}
	
	private void updateFog() {
		viewer.getSceneRoot().getAppearance().setAttribute(FOG_ENABLED, fogEnabled);
		viewer.getSceneRoot().getAppearance().setAttribute(FOG_DENSITY, fogDensity);
		viewer.getSceneRoot().getAppearance().setAttribute(FOG_MODE, fogMode);
		viewer.renderAsync();
	}

@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Fog", "Charles Gunn");
		info.isDynamic = false;
		return info;
	}

	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		setupGUI();
		viewer = c.getPlugin(View.class).getViewer();
		updateFog();
	}

	private void setupGUI()	{
		
		JCheckBox jcb = new JCheckBox("Fog");
		jcb.setSelected(fogEnabled);
		jcb.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				fogEnabled = (((JCheckBox)e.getSource()).isSelected());
				updateFog();
			}
			
		});


		final TextSlider<Double> fogSl = new TextSlider.Double("",
				SwingConstants.HORIZONTAL, 0.0, 1.0, fogDensity);
		fogSl.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				fogDensity = fogSl.getValue().doubleValue();
				updateFog();
			}
			
		});
		
		Insets insets = new Insets(2, 2, 2, 2);
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = BOTH;
		c.insets = insets;
		c.weighty = 0.0;
		c.anchor = WEST;
		
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		c.gridwidth = 1;
		c.weightx = 0.0;
		panel.add(jcb, c);
		c.gridwidth = REMAINDER;
		c.weightx = 1.0;
		panel.add(fogSl, c);
		
		shrinkPanel.add(panel,c);

	}
}

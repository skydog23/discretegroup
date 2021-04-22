/*
 * Created on Nov 18, 2009
 *
 */
package de.jtem.discretegroup.plugin;

import de.jreality.plugin.basic.View;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;

/**
 * This needs to be a separate (non-anonymous) class in order for the documentation system to work.
 *
 */
public class TessellatedContentSPP extends ShrinkPanelPlugin {

	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Tessellated Content ", "Charles Gunn");
		info.isDynamic = false;
		return info;
	}
	@Override
	public String getHelpDocument() {
		return "TessellatedContent.html";
	}
	
	@Override
	public String getHelpPath() {
		return "/de/jtem/discretegroup/plugin/help/";
	}
	
	@Override
	public Class<?> getHelpHandle() {
		return getClass();
	}



}

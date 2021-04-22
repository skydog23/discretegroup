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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import de.jreality.math.Pn;
import de.jreality.plugin.basic.View;
import de.jreality.plugin.basic.ViewMenuBar;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.discretegroup.core.DiscreteGroupUtility;
import de.jtem.discretegroup.core.DiscreteGroupViewportConstraint;
import de.jtem.discretegroup.core.FiniteStateAutomaton;
import de.jtem.discretegroup.core.FiniteStateAutomatonUtility;
import de.jtem.discretegroup.core.ImportGroup;
import de.jtem.discretegroup.groups.BorromeanUtility;
import de.jtem.discretegroup.groups.CrystallographicGroup;
import de.jtem.discretegroup.groups.Platycosm;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;
import de.jtem.jrworkspace.plugin.PluginInfo;

/**
 * Note: this class is actually an example class, and works in its current form only with the class {@link TessellatedContent}.
 * To be generally useful, there needs to be some way to set the groups which are to be loaded.
 * @author gunn
 *
 */
public class DiscreteGroupLoader extends Plugin {

	View view;
	static protected String[] noneuclideanNames =  new String[]{"borromean order-4","120 cell"};
	DiscreteGroup theGroup;
	
	public static interface GroupLoadedListener {
		
		public void GroupLoaded(Event e);
		
	}
	
	protected List<GroupLoadedListener>
		listeners = new LinkedList<GroupLoadedListener>();
	
	public synchronized void fireGroupLoaded(Event cce) {
		for (GroupLoadedListener l : listeners) {
			l.GroupLoaded(cce);
		}
	}
	
	
	public synchronized void fireGroupLoaded() {
		Event cce = new Event(this, 0, null);
		for (GroupLoadedListener l : listeners) {
			l.GroupLoaded(cce);
		}
	}
	
	
	public synchronized boolean addGroupLoadedListener(GroupLoadedListener l) {
		return listeners.add(l);
	}
	
	public synchronized boolean removeGroupLoadedListener(GroupLoadedListener l) {
		return listeners.remove(l);
	}
	
	
	public DiscreteGroup getGroup() {
		return theGroup;
	}

	String menuName = "Group",
		platyName = "Platycosm",
		borromName = "Borromean",
		otherName = "Other";
	JMenu loadMenu = new JMenu(menuName),
		platyMenu = new JMenu(platyName),
		borromMenu = new JMenu(borromName),
		otherMenu = new JMenu(otherName);

	protected JMenu setupGUI()
	{
		loadMenu.setMnemonic(KeyEvent.VK_G);
		viewMenuBar.addMenu(getClass(), 0, loadMenu);
		JMenu groupM = new JMenu("Platycosm");
		platyMenu = groupM;
		ButtonGroup bg = new ButtonGroup();
		final String[] gnames = Platycosm.names;
		for (int i = 0; i<gnames.length; ++i)	{
			final int j = i;
			JMenuItem jm = groupM.add(new JRadioButtonMenuItem(gnames[i]));
			jm.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e)	{
					replacePlatycosm(gnames[j]);
				}
			});
			bg.add(jm);
		}
		loadMenu.add(groupM);

		int[] borromExamples = {2,3,4,5,6,7,8,10,15, -1};
		groupM = new JMenu("Borromean");
		borromMenu = groupM;
		bg = new ButtonGroup();
		for (int i = 0; i<borromExamples.length; ++i)	{
			final int j = borromExamples[i];
			JMenuItem jm = groupM.add(new JRadioButtonMenuItem("order "+j));
			jm.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent e)	{
					DiscreteGroup dg = BorromeanUtility.borromeanGroupOfOrder(j);
					initializeGroup(dg);
					replaceGroup(dg);
				}
			});
			bg.add(jm);
		}
		loadMenu.add(groupM);

		otherMenu = groupM = new JMenu("Other");
		JMenuItem jm = groupM.add(new JMenuItem("120-cell"));
		jm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				replaceGroup(noneuclideanNames[1]);
				System.err.println("Loading 120 cell");
			}
			
		});
		groupM.addSeparator();
		jm = groupM.add(new JMenuItem("**"));
		jm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DiscreteGroup dg = CrystallographicGroup.instanceOfGroup("**");
				dg.setCenterPoint(new double[]{.2,.2,.2});
				initializeGroup(dg);
				replaceGroup(dg);
			}
			
		});
		groupM.addSeparator();
		jm = groupM.add(new JMenuItem("load..."));
		jm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadGroup();
			}
			
		});
		loadMenu.add(groupM);
		return loadMenu;
	}
		
	static String curvedSpaceExamples = System.getProperty("user.home")+"/Software/Curved Spaces/Sample Spaces/";
	JFileChooser fc = new JFileChooser(curvedSpaceExamples);
	protected void loadGroup()	{
		int result = fc.showOpenDialog(new JFrame());
		if (result == JFileChooser.APPROVE_OPTION)	{
			File file = fc.getSelectedFile();
			theGroup = ImportGroup.initFromFile(file, Pn.PROJECTIVE); //120cell.gens");			
			initializeGroup(theGroup);
			replaceGroup(theGroup);
			fc.setCurrentDirectory(file);
		} else {
			System.out.println("Unable to open file");
			return;
		}

	}
	protected void replaceGroup(DiscreteGroup dg) {
		theGroup = dg;
		theGroup.setConstraint(masterConstraint);
		fireGroupLoaded();
		}
	
	protected void replaceGroup(String string) {
		DiscreteGroup dg = ImportGroup.initFromResource("resources/groups/120cell.gens", Pn.ELLIPTIC); //120cell.gens");		
		dg.setFinite(true);
		dg.setMetric(Pn.ELLIPTIC);
		dg.setName(string);
		initializeGroup(dg);
		replaceGroup(dg);
		}
	
	protected void replacePlatycosm(String string)	{
		theGroup = Platycosm.instanceOfGroup(string);
		FiniteStateAutomaton fsa = FiniteStateAutomaton.fsaForName(string+".wa", de.jtem.discretegroup.ResourceClass.class);
		if (fsa != null && fsa.getTransitions().length != 0) theGroup.setFsa(fsa);
		clipToCamera = true;
		followCamera = true;
		maxNumElements = string == "c3" ? 500 : 3000;
		flySpeed = .5;
		masterConstraint = new DiscreteGroupSimpleConstraint(maxNumElements);
		minW = 2; maxW = 10; minD = 4.5; maxD = 12.0;
		replaceGroup(theGroup);
	}
	
	DiscreteGroupSimpleConstraint masterConstraint;
	DiscreteGroupViewportConstraint viewportConstraint;
	double flySpeed = .1, minD, maxD, portalScale = 1.0;
	int maxNumElements = 3000, maxDirDomOrbitSize = 75, minW, maxW, pickCopies = 100;
	boolean clipToCamera = true, followCamera = true;
	private ViewMenuBar viewMenuBar;
	private void initializeGroup(DiscreteGroup dg) {
		if (dg.getMetric() == Pn.EUCLIDEAN) {
			flySpeed = .5;
	//		FiniteStateAutomaton fsa = FiniteStateAutomaton.generateFiniteStateAutomatonForGroup(dg);
	//		dg.setFsa(fsa);
			minW = 2; maxW = 16; minD = 4.5; maxD = 16.0;
			maxNumElements = 800;
			maxDirDomOrbitSize = 800;
		}
		else if (dg.getMetric() == Pn.ELLIPTIC) {
			flySpeed = .2;
			clipToCamera = false; followCamera = false;
			portalScale = .2;
		}
		else {  // hyperbolic
			flySpeed = .15;
			minW = 2; maxW = 12; minD = 2.5; maxD = 4.5;
			portalScale = .2;
			if (dg.getFsa() == null)	{
				FiniteStateAutomaton fsa = FiniteStateAutomatonUtility.generateFiniteStateAutomatonForGroup(dg);
				dg.setFsa(fsa);				
			}
			maxNumElements = 3000;
			masterConstraint = new DiscreteGroupSimpleConstraint(3000);
			DiscreteGroupElement[] rawlist = DiscreteGroupUtility.generateElements(dg, masterConstraint);
			DiscreteGroupUtility.sort(rawlist, Pn.HYPERBOLIC);
			masterConstraint = new DiscreteGroupSimpleConstraint(4.3, -1, maxNumElements);
			maxDirDomOrbitSize = 225;

		}  
		if (dg.getMetric() != Pn.ELLIPTIC && masterConstraint == null) {
			masterConstraint = new DiscreteGroupSimpleConstraint(dg.getFsa() != null ? maxNumElements : 500);//maxNumElements)); //setMaxNumberElements(300);
		}
	}

	@Override
	public void install(Controller c) throws Exception {
		// TODO Auto-generated method stub
		super.install(c);
		viewMenuBar = c.getPlugin(ViewMenuBar.class);
		setupGUI();
	}


	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Discrete Group Loader Plugin", "Charles Gunn");
		info.isDynamic = false;
		return info;
	}

}

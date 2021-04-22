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

import static de.jreality.shader.CommonAttributes.ONE_TEXTURE2D_PER_IMAGE;

import java.awt.Color;
import java.awt.Component;
import java.awt.Event;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import de.jreality.geometry.BoundingBoxUtility;
import de.jreality.geometry.Primitives;
import de.jreality.jogl.InstrumentedViewer;
import de.jreality.jogl.plugin.HelpOverlay;
import de.jreality.jogl.plugin.InfoOverlay;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.basic.Content;
import de.jreality.plugin.basic.Scene;
import de.jreality.plugin.basic.ToolSystemPlugin;
import de.jreality.plugin.basic.View;
import de.jreality.plugin.experimental.ViewerKeyListener;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.PointLight;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;
import de.jreality.scene.event.CameraEvent;
import de.jreality.scene.event.CameraListener;
import de.jreality.scene.pick.Graphics3D;
import de.jreality.scene.tool.Tool;
import de.jreality.tools.RotateTool;
import de.jtem.discretegroup.util.FlyTool;
import de.jreality.util.CameraUtility;
import de.jreality.util.DefaultMatrixSupport;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;
import de.jreality.util.SystemProperties;
import de.jtem.discretegroup.core.DirichletDomain;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.discretegroup.core.DiscreteGroupUtility;
import de.jtem.discretegroup.core.DiscreteGroupViewportConstraint;
import de.jtem.discretegroup.util.TextSlider;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.sidecontainer.widget.ShrinkPanel;

public class TessellatedContent extends Content {

	DiscreteGroup theGroup = DiscreteGroupUtility.trivialGroup();
	DiscreteGroupSceneGraphRepresentation theRepn;
	SceneGraphComponent fundDomSGC = new SceneGraphComponent("fundDomSGC!");
	boolean clipToCamera = false, 
		followsCamera = false,
		copycat = true,
		fogEnabled  = true,
		showGroupLoader = true;
	DiscreteGroupSimpleConstraint masterConstraint  = 
			new DiscreteGroupSimpleConstraint(18.0, 16, bigNumber);
	DiscreteGroupViewportConstraint viewportConstraint = new DiscreteGroupViewportConstraint(3.0,2,12.0,-1, null);
	DiscreteGroupViewportConstraintSP viewConstraintSP;
	DiscreteGroupSimpleConstraintSP masterConstraintSP;
	static final int bigNumber = 5000;

	// following has to be a separate class (subclass of ShrinkPanelPlugin) 
	// so that the plugin system accesses the help
	// system properly (I think I remember that correctly -- was a while ago).
//	ShrinkPanelPlugin shrinkPanel = new TessellatedContentSPP();
	TessellatedContentSPP shrinkPanel;

	private TextSlider<Double> speedSl,
			scaleSl;
	Scene scene;
	View view;
	FlyTool flyTool;
	Viewer viewer;
	double flySpeed = .5,
		scale = 1.0;
	private int pickCopies = 100;
	InfoOverlay info, perfInfo;
	HelpOverlay helpOverlay;
	static boolean	canCopycat = true;
	boolean initialized = false;
	
	static {
		String foo = System.getProperty(SystemProperties.JOGL_COPY_CAT);
		if (foo != null && foo.indexOf("true") == -1) canCopycat = false;
	}

	public void setupJRViewer(JRViewer jrv)	{
		initConstraints();
		jrv.registerPlugin(viewConstraintSP);
		jrv.registerPlugin(masterConstraintSP);
	}
	
	public TessellatedContent()	{
	}
	
	private void initConstraints() {
		if (viewConstraintSP != null && masterConstraintSP != null) return;
		viewConstraintSP = new DiscreteGroupViewportConstraintSP();
		viewConstraintSP.setConstraint(viewportConstraint);
		viewConstraintSP.addViewportConstraintChangedListener(new DiscreteGroupViewportConstraintSP.ViewportConstraintChangedListener() {
			
			public void viewportConstraintChanged(Event e) {
				updateViewportConstraint();
			}
		});
		masterConstraintSP = new DiscreteGroupSimpleConstraintSP();
		masterConstraintSP.setConstraint(masterConstraint);
		masterConstraintSP.getShrinkPanel().setName("master constraint");
		masterConstraintSP.addSimpleConstraintChangedListener(new DiscreteGroupSimpleConstraintSP.SimpleConstraintChangedListener() {
			
			public void simpleConstraintChanged(Event e) {
				updateMasterConstraint();
			}
		});


	}
	private void setupGUI() {
		
		initConstraints();
		
		Insets insets = new Insets(1,0,1,0);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.insets = insets;
		c.weighty = 0.0;
		c.anchor = GridBagConstraints.WEST;
		
		ShrinkPanel mainPanel = shrinkPanel.getShrinkPanel();
		mainPanel.removeAll();
		mainPanel.setLayout(new ShrinkPanel.MinSizeGridBagLayout());
						
		followCameraBox = new JCheckBox("Follow camera");
		clipCameraBox = new JCheckBox("Clip to camera");
		
		followCameraBox.setSelected(followsCamera);
		followCameraBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				followsCamera = (((JCheckBox)e.getSource()).isSelected());
				setFollowsCamera(followsCamera);
			}
			
		});
		
		clipCameraBox.setSelected(clipToCamera);
		clipCameraBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				clipToCamera = (((JCheckBox)e.getSource()).isSelected());
				setClipToCamera(clipToCamera);
			}
			
		});

		speedSl = new TextSlider.DoubleLog("Fly speed:",
				SwingConstants.HORIZONTAL, 0.01, 2.0, flySpeed);
		speedSl.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				flySpeed = speedSl.getValue().doubleValue();
				System.err.println("Setting speedSl  to "+flySpeed);
				flyTool.setGain(flySpeed);
			}
			
		});

		scaleSl = new TextSlider.Double("Scale:",
				SwingConstants.HORIZONTAL, 0, 1, scale);
		scaleSl.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				scale = scaleSl.getValue().doubleValue();
				System.err.println("Setting scale  to "+scale);
//				setScale(scale);
				MatrixBuilder.euclidean().scale(scale).assignTo(fundDomSGC);
				viewer.renderAsync();
			}
			
		});
		
		
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		c.gridwidth = 1;
		c.weightx = 0.0;
		panel.add(followCameraBox, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		panel.add(clipCameraBox, c);

		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		panel.add(speedSl, c);
		
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		panel.add(scaleSl, c);
		
		mainPanel.add(panel, c);
//		mainPanel.add(masterConstraintSP,c);
//		mainPanel.add(viewConstraintSP, c);
		// TODO figure out how to turn this on and off
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Tessellated Content", "Charles Gunn");
		info.isDynamic = false;
		return info;
	}
	
	@Override
	public void setContent(SceneGraphNode node) {
		boolean fire = getContentNode() != node;
		if (fire) {
			ContentChangedEvent cce = new ContentChangedEvent(ChangeEventType.ContentChanged);
			cce.node = node;
			fireContentChanged(cce);
		}
		SceneGraphUtility.removeChildren(fundDomSGC);
		if (node instanceof Geometry) 
			fundDomSGC.setGeometry((Geometry) node);
		else if (node instanceof SceneGraphComponent){
			fundDomSGC.setGeometry(null);
			fundDomSGC.addChild( (SceneGraphComponent) node);
		} else
			throw new IllegalArgumentException("can't tessellate class "+node.getClass());
		setContentNode(node);
	}

	public DiscreteGroup getGroup() {
		return theGroup;
	}

	public void setGroup(DiscreteGroup theGroup) {
		setGroup(theGroup, copycat);
	}
	
	boolean firsttime = true;
	public void setGroup(DiscreteGroup theGroup, boolean copycat) {
		if (viewer == null) 
			throw new IllegalStateException("No viewer");
		this.theGroup = theGroup;
		this.copycat = copycat;
		viewportConstraint.setGraphicsContext(new Graphics3D(viewer));

		if (theGroup.getConstraint() == null ||
				!(theGroup.getConstraint() instanceof DiscreteGroupSimpleConstraint)) {
			setMasterConstraint(masterConstraint);	
		}
		else {
			setMasterConstraint((DiscreteGroupSimpleConstraint) theGroup.getConstraint());
		}
					
		if (copycat && !canCopycat) {
			throw new IllegalArgumentException("can't apply copycat");
		}
		if (theRepn != null)	{
			getContentRoot().removeChild(theRepn.getRepresentationRoot());
			theRepn.dispose();
		}
		System.err.println("copycat = "+copycat);
		theRepn = new DiscreteGroupSceneGraphRepresentation(theGroup, copycat);
		theRepn.setWorldNode(fundDomSGC);
		theRepn.setClipDelay(500);
		theRepn.setFollowDelay(500);
		theRepn.setFollowsCamera(followsCamera);
		theRepn.setClipToCamera(clipToCamera);
		// for picking and bounding don't need such a full set
		if (copycat)	{
			DiscreteGroupSimpleConstraint dgsc = new DiscreteGroupSimpleConstraint(pickCopies);
			theRepn.setOfficialElementList(
					DiscreteGroupUtility.generateElements(theGroup, dgsc));
		}
		theRepn.setElementList(theGroup.getElementList());
		SceneGraphPath pathToWorld = SceneGraphUtility.getPathsBetween(
			viewer.getSceneRoot(), getContentRoot()).get(0);
			
		theRepn.attachToViewer(viewer, pathToWorld); 			
		theRepn.setViewportConstraint(viewportConstraint);
		updateViewportConstraint();
		theRepn.update();
		if (flyTool != null) flyTool.setMetric(theGroup.getMetric());
//		CameraUtilityOverflow.reset(CameraUtility.getCamera(viewer), theGroup.getMetric());
		DefaultMatrixSupport.getSharedInstance().storeDefaultMatrices(viewer.getSceneRoot());
		SceneGraphUtility.setMetric(viewer.getSceneRoot(), theGroup.getMetric());
		if (viewer != null) viewer.renderAsync();
		fireGroupChanged();
		if (firsttime) {
			CameraUtility.getCamera(viewer).addCameraListener(new CameraListener() {

				public void cameraChanged(CameraEvent ev) {
					if (!clipToCamera) return;
					System.err.println("updating camera");
					getViewportConstraint().update();
					getTheRepn().update();
				}
				
			});
			((Component) viewer.getViewingComponent()).addComponentListener(new ComponentAdapter() {

				@Override
				public void componentResized(ComponentEvent e) {
					if (!clipToCamera) return;
					System.err.println("component resized");
					getViewportConstraint().update();
					getTheRepn().update();
				}
				
			});
			firsttime = false;
		}

//		if (view != null) adjustConstraints(framerate);
	}

	public DiscreteGroupSimpleConstraint getMasterConstraint() {
		return masterConstraint;
	}
	public DiscreteGroupViewportConstraint getViewportConstraint() {
		return viewportConstraint;
	}

	public void setMasterConstraint(DiscreteGroupSimpleConstraint mc)	{
		masterConstraint = mc;
		theGroup.setConstraint(masterConstraint);
		theGroup.update();
		masterConstraintSP.setConstraint(masterConstraint);
	}
	
	public void setViewportConstraint(DiscreteGroupViewportConstraint theConstraint) {
		this.viewportConstraint = theConstraint;
		theRepn.setViewportConstraint(theConstraint);				
		viewConstraintSP.setConstraint(theConstraint);
	}


	public void updateViewportConstraint() {
		// something needs to be done here but I don't know exactly what ...
		theRepn.setViewportConstraint(viewportConstraint);
	}
	
	public void updateMasterConstraint()	{
		DiscreteGroupElement[] list = DiscreteGroupUtility.generateElements(theGroup, masterConstraint);
		theGroup.setElementList(list);
		theRepn.setElementList(list);
		theRepn.update();
	}

	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		shrinkPanel = c.getPlugin(TessellatedContentSPP.class);
		scene = c.getPlugin(Scene.class);
		view = c.getPlugin(View.class);
		// this has to be here, don't remove
		ToolSystemPlugin tsp = c.getPlugin(ToolSystemPlugin.class);
		viewer = view.getViewer().getCurrentViewer(); 
		scene.getContentComponent().addTool(new RotateTool());
		setupGUI();
		setGroup(theGroup, copycat);
		setContent(Primitives.sharedIcosahedron);
		instrumentSceneGraph();
//		fundDomSGC.getAppearance().setAttribute("useGLSL", true);

	}

	private void instrumentSceneGraph() {
		flyTool = new FlyTool();
		flyTool.setGain(flySpeed);
		flyTool.addChangeListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				viewer.renderAsync();
			}
			
		});
		flyTool.setMetric(theGroup.getMetric());
		scene.getAvatarComponent().addTool(flyTool);			
		
//		RenderTrigger rt = new RenderTrigger();
//		rt.addSceneGraphComponent(scene.getAvatarComponent());
//		rt.addViewer(viewer);
//		
		viewer.getSceneRoot().getAppearance().setAttribute(ONE_TEXTURE2D_PER_IMAGE, true);
//		viewer.getSceneRoot().addTool(new PickShowTool());
		viewportConstraint.setGraphicsContext(new Graphics3D(viewer));
		SceneGraphPath pathToWorld = SceneGraphUtility.getPathsBetween(
				viewer.getSceneRoot(), getContentRoot()).get(0);
		
		theRepn.attachToViewer(viewer, pathToWorld); 	
		
		setupLights();
		updateLights();
		scene.getAvatarComponent().addChildren(euclideanLights, hyperbolicLights);

		if (viewer instanceof InstrumentedViewer) {
			perfInfo =  InfoOverlay.perfInfoOverlayFor();
			perfInfo.setVisible(true);
			info = new InfoOverlay();
			info.setInstrumentedViewer((InstrumentedViewer) viewer);
			info.setPosition(InfoOverlay.LOWER_LEFT);
			info.setVisible(true);
			info.setInfoProvider(new InfoOverlay.InfoProvider() {

				List<String> infoStrings = new Vector<String>();
				public void updateInfoStrings(InfoOverlay io)	{
					//JOGLConfiguration.theLog.log(Level.INFO,"Providing info strings");
					infoStrings.clear();
					if (theGroup != null && theRepn != null && theRepn.getElementList() != null)	{
						infoStrings.add("# elements rendered: "+theRepn.getCopyCatCount());
//						infoStrings.add("min/max dist:"+String.format("%4.2g %4.2g",minD,maxD));
						infoStrings.add("# elements: "+theRepn.getElementList().length);
						infoStrings.add("group name: "+theGroup.getName());
					}
					io.setInfoStrings(infoStrings);
				}
			});
		}
		if (viewer.hasViewingComponent() && 
				viewer.getViewingComponent() instanceof Component) {
			ViewerKeyListener vkl = new ViewerKeyListener(viewer, helpOverlay, perfInfo);
			((Component) viewer.getViewingComponent()).addKeyListener(vkl);
		}

	}

	public double getScale() {
		return scale;
	}

	public void setScale(double scale) {
		this.scale = scale;
		MatrixBuilder.euclidean().scale(scale).assignTo(fundDomSGC);
		if (scaleSl != null) scaleSl.setValue(scale);
	}


	@Override
	protected SceneGraphComponent getToolComponent() {
		return fundDomSGC;
	}

	@Override
	public boolean addContentTool(Tool tool) {
		return false;
	}


	public double getFlySpeed() {
		return flySpeed;
	}


	public void setFlySpeed(double flySpeed) {
		this.flySpeed = flySpeed;
		flyTool.setGain(flySpeed);
		speedSl.setValue(flySpeed);
	}

	public void adjustConstraints(int framerate)	{
		de.jreality.jogl.JOGLViewer joglViewer;
		if (!(viewer instanceof de.jreality.jogl.JOGLViewer)) {
			System.err.println("Not a jogl viewer, can't get framerate");
			return;
		} 
		joglViewer = (de.jreality.jogl.JOGLViewer) viewer;
		if (joglViewer.getRenderer() == null) return;
		double frate = joglViewer.getRenderer().getFramerate();
		System.err.println("Frame rate = "+frate);
		viewportConstraint = theRepn.getViewportConstraint();
		int count = 0;
		double ofrate = frate;
		DirichletDomain dd = new DirichletDomain(theGroup);
		dd.update();
		IndexedFaceSet ddifs = dd.getDirichletDomain();
		Rectangle3D bound = BoundingBoxUtility.calculateBoundingBox(ddifs);
		double[] extent = bound.getExtent();
		double norm = Rn.euclideanNorm(extent);
		System.err.println("Bound is so big: "+norm);
		while (count < 10 && (frate > 2* framerate || frate < .5 * framerate))	{
			double factor = frate/framerate;
			// following only works for euclidean case
			factor = Math.pow(factor, 1/3.0);
			if (factor > 2) factor = 2;
			if (factor < .5) factor = .5;
			double maxd = viewportConstraint.getMaxDistance() * factor;
			if (maxd > masterConstraint.getMaxDistance()) {
				masterConstraint.setMaxDistance(maxd);
				updateMasterConstraint();
			}
			viewportConstraint.setMaxDistance(maxd);
			viewportConstraint.update();
			viewConstraintSP.update();
//			try {
//				Thread.sleep(500);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			for (int i = 0; i<200; ++i) 
				joglViewer.render();
			frate = joglViewer.getRenderer().getFramerate();
			System.err.println("adjusting framerate = "+frate);
			if (count > 0)	{
				if (factor > 2 && frate > ofrate) {
					System.err.println("viewport constraint no traction");
					break;
				}
			}
			ofrate = frate;
			count++;
		}
	}

	SceneGraphComponent euclideanLights;
	private SceneGraphComponent lightSGC1, lightSGC2;
	private SceneGraphComponent hyperbolicLights;
	
	private Color[] color = {new Color(255, 255, 200), 
			new Color(255,200,255), 
			new Color(200,255,255), 
			new Color(255,255,255)};
	private JCheckBox followCameraBox;
	private JCheckBox clipCameraBox;
	private DirichletDomainSP dirichletDomainSP;
//	double[][] positions = {{0,0,1}, {0,1,0}, {1,0,0},{0,0,-1}, {0,-1,0}, {-1,0,0}};
	static double[] randmat = P3.makeRotationMatrix(null, new double[]{.3,-.5,.7}, new double[]{-.6,.5,-.2});
	static Matrix randM = new Matrix(P3.makeScaleMatrix(null, new double[]{-1,-1,-1}));//randmat);
	
	double[][] positions = { {-1,-1,-1}, {-.1, 1, .2},{1, .3, -.1}, {.2, -.1, 1}}; //, 
//			{1,1,1}, {1,-1,-1},{-1,1,-1}, {-1,-1,1}}; //{ {-1,-1,-1}, {-.3, 1, .2},{1, .3, -.4}, {.2, -.4, 1}};
	public void setupLights()	{
		euclideanLights = new SceneGraphComponent("Euclidean Lights");
		double intensity = .5;
		for (int i = 0; i<positions.length; ++i)	{
			SceneGraphComponent lightNode=new SceneGraphComponent("light"+i);
			DirectionalLight light = new DirectionalLight();
			light.setIntensity(intensity);
			lightNode.setLight(light);
			MatrixBuilder.euclidean().rotateFromTo(new double[]{0,0,1}, positions[i]).assignTo(lightNode);
			euclideanLights.addChild(lightNode);			
//			lightNode=new SceneGraphComponent("light"+i);
//			lightNode.setLight(light);
//			MatrixBuilder.euclidean(randM).rotateFromTo(new double[]{0,0,1}, positions[i]).assignTo(lightNode);
//			euclideanLights.addChild(lightNode);			
		}

//		light = new DirectionalLight();
//		light.setIntensity(intensity);
//		lightNode.setLight(light);
////		light.setColor(color[1]);
//		lightNode2.setLight(light);
//		MatrixBuilder.euclidean().rotateFromTo(new double[]{0,0,1}, new double[]{-1,1,1}).assignTo(lightNode2);
//		euclideanLights.addChild(lightNode2);
//
//		light = new DirectionalLight();
//		light.setIntensity(intensity);
//		lightNode.setLight(light);
////		light.setColor(color[2]);
//		lightNode3.setLight(light);
//		MatrixBuilder.euclidean().rotateFromTo(new double[]{0,0,1}, new double[]{1,1,-1}).assignTo(lightNode3);
//		euclideanLights.addChild(lightNode3);
//
//		light = new DirectionalLight();
//		light.setIntensity(intensity);
//		lightNode.setLight(light);
////		light.setColor(color[3]);
//		lightNode4.setLight(light);
//		MatrixBuilder.euclidean().rotateFromTo(new double[]{0,0,1}, new double[]{-1,-1,-1}).assignTo(lightNode4);
//		euclideanLights.addChild(lightNode4);

		hyperbolicLights = new SceneGraphComponent("Hyperbolic lights");
		lightSGC1 = SceneGraphUtility.createFullSceneGraphComponent("l1");
//		lightSGC1.addChild(Primitives.sphere(.05, 0,0,0));
		lightSGC1.getAppearance().setAttribute("polygonShader.diffuseColor",Color.WHITE);
 		PointLight pointLight1 = new PointLight();
		pointLight1.setColor(Color.white);
 		pointLight1.setIntensity(1.0);
   		lightSGC1.setLight(pointLight1);
  		hyperbolicLights.addChild(lightSGC1);
		lightSGC2 = SceneGraphUtility.createFullSceneGraphComponent("l2");
//		lightSGC2.addChild(Primitives.sphere(.05, 0,0,0));
		lightSGC2.getAppearance().setAttribute("polygonShader.diffuseColor",new Color(255, 255, 200));
 		PointLight pointLight2 = new PointLight();
  		pointLight2.setColor(new Color(255, 255, 200));
 		pointLight2.setIntensity(1.0);
  		lightSGC2.setLight(pointLight2);
  		hyperbolicLights.addChild(lightSGC2);
  		MatrixBuilder.hyperbolic().translate(.2, .2, .2).assignTo(lightSGC2);
	}
	
	private void updateLights() {
		euclideanLights.setVisible(theGroup.getDimension() == 2 || theGroup.getMetric() == Pn.EUCLIDEAN);
		hyperbolicLights.setVisible(!euclideanLights.isVisible());
	}


	public boolean isClipToCamera() {
		return clipToCamera;
	}


	public void setClipToCamera(boolean clipToCamera) {
		this.clipToCamera = clipToCamera;
		clipCameraBox.setSelected(clipToCamera);
		if (theRepn != null) theRepn.setClipToCamera(clipToCamera);
		viewConstraintSP.getShrinkPanel().setVisible(clipToCamera);
	}


	public boolean isFollowsCamera() {
		return followsCamera;
	}


	public void setFollowsCamera(boolean followsCamera) {
		this.followsCamera = followsCamera;
		followCameraBox.setSelected(followsCamera);
		if (theRepn != null) theRepn.setFollowsCamera(followsCamera);
	}


	public boolean isShowGroupLoader() {
		return showGroupLoader;
	}


	public void setShowGroupLoader(boolean showGroupLoader) {
		this.showGroupLoader = showGroupLoader;
	}

	public static interface GroupChangedListener {
		
		public void groupChanged(Event e);
		
	}
	
	// provide for listeners, typically other plugins which depend on the choice of group
	// for example the DirichletDomainSP plugin.
	protected List<GroupChangedListener>
		listeners = new LinkedList<GroupChangedListener>();
	
	public synchronized void fireGroupChanged(Event cce) {
		for (GroupChangedListener l : listeners) {
			l.groupChanged(cce);
		}
	}
	
	public synchronized void fireGroupChanged() {
		Event cce = new Event(this, 0, null);
		for (GroupChangedListener l : listeners) {
			l.groupChanged(cce);
		}
	}
	
	public synchronized boolean addGroupChangedListener(GroupChangedListener l) {
		return listeners.add(l);
	}
	
	public synchronized boolean removeGroupChangedListener(GroupChangedListener l) {
		return listeners.remove(l);
	}

	public DiscreteGroupSceneGraphRepresentation getTheRepn() {
		return theRepn;
	}


}

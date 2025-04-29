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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import charlesgunn.anim.jreality.SceneGraphAnimator;
import charlesgunn.util.TextSlider;
import de.jreality.geometry.GeometryMergeFactory;
import de.jreality.jogl.MatrixListData;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.Scene;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.scene.pick.Graphics3D;
import de.jreality.toolsystem.ToolSystem;
import de.jreality.util.CameraUtility;
import de.jreality.util.DefaultMatrixSupport;
import de.jreality.util.SceneGraphUtility;
import de.jreality.util.SystemProperties;
import de.jtem.discretegroup.util.WingedEdge;

/**
 * @author gunn
 *
 */
public class DiscreteGroupSceneGraphRepresentation extends AbstractDGSGR{
	protected SceneGraphComponent 
		followCameraNode,
			changeOfBasisNode,
				theSceneGraphRepn,		// this node has the children, one per copy
				flatSceneGraphRepn,		// this node has all the children flattened into one geometry
					fundamentalRegion,
						worldNode, oldWorldNode = null,
						cameraRepn;
	protected DiscreteGroupElement[] elementList, officialElementList;
	protected boolean 
		newElementList = true, 
		newAppList = true,
		newCameraRepn, 
		newWorldNode,
		active = true,
		flatten = false,
		shadeGeometry = true,
		copyCat = false;
	DiscreteGroup theGroup;
	private Graphics3D graphicsContext = null;
	DirichletDomain dirdom;
	String name = "";
	Appearance[] appList = null;
	MatrixListData theDropBox = new MatrixListData("discrete group data");
	int cutoff = -1;
	boolean doCutoff = true;
	SceneGraphPath pathToMatrices, avatarPath;
	DiscreteGroupConstraint constraint = null;
	
	public DiscreteGroupSceneGraphRepresentation(DiscreteGroup g) {
		this(g, false, "");
	}
	public DiscreteGroupSceneGraphRepresentation(DiscreteGroup g, boolean c) {
		this(g, c, "");
	}
	public DiscreteGroupSceneGraphRepresentation(DiscreteGroup g, boolean c, String n) {
		System.err.println("copycat = "+c);
		theGroup = g;
		dirdom = new DirichletDomain(theGroup);
		copyCat = c;
		theDropBox.setCopycat(copyCat);
		name = n;
		// set up scene graph representation
		followCameraNode = SceneGraphUtility.createFullSceneGraphComponent(name+" DG Follow Camera"); 
		followCameraNode.getTransformation().setReadOnly(false);
		changeOfBasisNode = SceneGraphUtility.createFullSceneGraphComponent(name+" DG Change of Basis"); 
		changeOfBasisNode.setAppearance(null);
		changeOfBasisNode.getTransformation().setReadOnly(false);
		followCameraNode.addChild( changeOfBasisNode);
		theSceneGraphRepn = SceneGraphUtility.createFullSceneGraphComponent(name+" DG Parent");
		changeOfBasisNode.addChild( theSceneGraphRepn);
		fundamentalRegion = new SceneGraphComponent(name+" DG fundamental Domain"); //SceneGraphUtility.createFullSceneGraphComponent("DG Geometry");
		fundamentalRegion.setAppearance(new Appearance());
		
		theSceneGraphRepn.getAppearance().setAttribute(SceneGraphAnimator.ANIMATED, false);
	}

	// a new feature added to allow a constraint to be applied "in place" by changing visibility.  
	@Override
	public void setConstraint(DiscreteGroupConstraint c) {
		constraint = c;
		if (constraint != null) AbstractDGSGR.applyConstraint(this, constraint);
		else {
			int n = theSceneGraphRepn.getChildComponentCount();
			for (int i = 0; i<n;++i) {
				theSceneGraphRepn.getChildComponent(i).setVisible(true);
			}
		}
//		System.err.println("applied constraint, sgr has # "+theSceneGraphRepn.getChildComponentCount());
	}
	
	@Override
	public DiscreteGroupConstraint getConstraint() {
		return constraint;
	}
	
	public void update()		{
		if (newAppList || newElementList || elementList == null)	{
			if (elementList == null) {
				theGroup.generateElements();
				elementList = theGroup.getElementList();
			}
			int n =  elementList.length;
			DiscreteGroupColorPicker cp = theGroup.getColorPicker();
			if (cp != null && elementList[0] != null && cp != null)  {
				theGroup.getColorPicker().assignColorIndices(elementList);
			}
//			Appearance[] aplist = DiscreteGroupColorPicker.appearanceList;
			SceneGraphComponent theNewSGR = SceneGraphUtility.createFullSceneGraphComponent(name+" DG Parent");
			if (copyCat)	{
				theNewSGR.setGeometry(theDropBox);
//				theNewSGR.getAppearance().setAttribute("dgsgr", this);
				theDropBox.setClipToCamera(clipToCamera);
				theDropBox.setDelay(clipDelay + 25);
				updateMatrixList(elementList);
			} 
			DiscreteGroupElement[] showTheWorld = officialElementList == null ? elementList : officialElementList;
			n = showTheWorld.length;
			for (int i = 0 ;i<n; ++i)	{
				SceneGraphComponent tmp = new SceneGraphComponent();
				Transformation newTrans = new Transformation(showTheWorld[i].getArray());
				newTrans.setName(showTheWorld[i].getWord());
				tmp.setName("dge "+showTheWorld[i].getWord());
				//newTrans.setReadOnly(true);
				tmp.setTransformation(newTrans);
				if (showTheWorld[i] != null && appList != null)		{
					DiscreteGroupElement dge = showTheWorld[i];
					int index = dge.getColorIndex() % appList.length;
					tmp.setAppearance(appList[index]);
				}
				tmp.addChild(fundamentalRegion);
				theNewSGR.addChild(tmp);		
			}	
			newAppList = false;
			SceneGraphComponent old = theSceneGraphRepn;
			theSceneGraphRepn = theNewSGR;				
			changeOfBasisNode.addChild(theSceneGraphRepn);
//			System.err.println("Deleting old");
			changeOfBasisNode.removeChild(old);
//			System.err.println("Adding new");
			newElementList = false;
			DefaultMatrixSupport.getSharedInstance().storeDefaultMatrices(theSceneGraphRepn);
		}
		if (newWorldNode || newCameraRepn)	{
			if (oldWorldNode != null && fundamentalRegion.isDirectAncestor(oldWorldNode)) 
				fundamentalRegion.removeChild(oldWorldNode);
			if (worldNode != null) fundamentalRegion.addChild(worldNode);
			if (cameraRepn != null) {
				fundamentalRegion.addChild(cameraRepn);
				setupCameraRepn();
			}
			newWorldNode = newCameraRepn = false;
		}
		updateFlatten();
		SceneGraphUtility.setMetric(followCameraNode,theGroup.getMetric());
		System.err.println("DGSGR: element list # = "+theSceneGraphRepn.getChildComponentCount());
		if (constraint != null) AbstractDGSGR.applyConstraint(this, constraint);
	}
	private void updateFlatten() {
		if (flatten) {
			SceneGraphComponent flat = SceneGraphUtility.flatten(theSceneGraphRepn);
			GeometryMergeFactory gmf = new GeometryMergeFactory();
			IndexedFaceSet ifs = gmf.mergeGeometrySets(flat);
			flat = new SceneGraphComponent("flat"+name+" DG Parent");
			flat.setAppearance(worldNode.getAppearance());
			flat.setGeometry(ifs);
			changeOfBasisNode.addChild(flat);
//			System.err.println("Deleting old");
			if (flatSceneGraphRepn != null) {
				if (changeOfBasisNode.isDirectAncestor(flatSceneGraphRepn))
					changeOfBasisNode.removeChild(flatSceneGraphRepn);				
			}
			flatSceneGraphRepn = flat;			
		}
		theSceneGraphRepn.setVisible(!flatten);
		if (flatSceneGraphRepn != null) flatSceneGraphRepn.setVisible(flatten);
	}
	
	public SceneGraphComponent getRepresentationRoot()	{
		return followCameraNode;
	}
	
	public SceneGraphComponent getChangeOfBasisNode() {
		return changeOfBasisNode;
	}
	

	public DiscreteGroupElement[] getElementList() {
		return elementList;
	}
	
	public void setElementList(DiscreteGroupElement[] tlist)	{
//		if (tlist == elementList) return;
		elementList = (DiscreteGroupElement[]) tlist;
		newElementList = true;
	}
	
	public SceneGraphComponent addElement(DiscreteGroupElement dge)	{
		SceneGraphComponent tmp = new SceneGraphComponent();
		Transformation newTrans = new Transformation(dge.getArray());
		newTrans.setName(dge.getWord());
		tmp.setName("dge "+dge.getWord());
		tmp.setTransformation(newTrans);
		tmp.addChild(fundamentalRegion);
		theSceneGraphRepn.addChild(tmp);
		if (copyCat) updateMatrixList(theSceneGraphRepn);
		return tmp;
	}
	
	public void removeElement(SceneGraphPath sgp)	{
		SceneGraphPath copy = (SceneGraphPath) sgp.clone();
		SceneGraphComponent lastComp = null;
		while (copy.getLength() > 0)	{
			lastComp = sgp.getLastComponent();
			if (theSceneGraphRepn.isDirectAncestor(lastComp)) {
				theSceneGraphRepn.removeChild(lastComp);		
				if (copyCat) updateMatrixList(theSceneGraphRepn);
				break;
			}
			copy.pop();
		}
	}

	public DiscreteGroupElement[] getOfficialElementList() {
		return officialElementList;
	}
	public void setOfficialElementList(DiscreteGroupElement[] officialElementList) {
		this.officialElementList = officialElementList;
	}
	public void updateMatrixList(SceneGraphComponent theSceneGraphRepn)	{
		int n = theSceneGraphRepn.getChildComponentCount();
		if (theDropBox.getMatrixList() == null ||
				theDropBox.getMatrixList().length != n) {
			theDropBox.setMatrixList(new double[n][]);
			theDropBox.setVisibleList(new boolean[n]);
		}
		double[][] mlist = theDropBox.getMatrixList();
		for (int k = 0; k<n; ++k)	 
			mlist[k] = theSceneGraphRepn.getChildComponent(k).getTransformation().getMatrix();
		System.err.println("Setting matrix list");
//		System.err.println("Updating matrix list length "+n);
	}
	
	public DiscreteGroupElement[] getElementListFromSGC()	{
		int n = theSceneGraphRepn.getChildComponentCount();
		DiscreteGroupElement[] ret = new DiscreteGroupElement[n];
		
		for (int k = 0; k<n; ++k)	 { 
			Transformation transf = theSceneGraphRepn.getChildComponent(k).getTransformation();
			ret[k] = new DiscreteGroupElement(theGroup.getMetric(), transf.getMatrix(), transf.getName());
		}
		System.err.println("Returning element list length "+n);
		return ret;
	}
	public void updateMatrixList(DiscreteGroupElement[] ellist)	{
		int n = ellist.length;
		if (theDropBox.getMatrixList() == null ||
				theDropBox.getMatrixList().length != n) {
			theDropBox.setMatrixList(new double[n][]);
		}
		if (theDropBox.getVisibleList() == null ||
				theDropBox.getVisibleList().length != n) {
			theDropBox.setVisibleList(new boolean[n]);
		}
		double[][] mlist = theDropBox.getMatrixList();
		boolean[] vlist = theDropBox.getVisibleList();
		for (int k = 0; k<n; ++k)	{ 
			mlist[k] = ellist[k].getArray();
			vlist[k] = true;
		}
		System.err.println("Setting matrix list");
		System.err.println("Updating matrix list length "+n+" "+cutoff);
	}
	
	public int getCopyCatCount()	{
//		System.err.println("name is "+theDropBox.getName());
		return theDropBox.getCount();
	}
	public SceneGraphComponent getCameraRepn() {
		return cameraRepn;
	}

	public void setCameraRepn(SceneGraphComponent cr) {
		if (cr == cameraRepn) return;
		cameraRepn = cr;
		newCameraRepn = true;
		//if (followsCamera)	
//		setupCameraRepn();
		
	}
	
	SceneGraphPath toCameraNode, toCameraRepn;
	private void setupCameraRepn()	{
		if (cameraRepn == null)	{
			if (cameraRepnTimer != null) cameraRepnTimer.stop();
			return;
		}
		if (graphicsContext == null )	{
			return;
			//throw new IllegalStateException("Bad state in setupCameraRepn");
		}
		List l = SceneGraphUtility.getPathsBetween(viewer.getSceneRoot(), cameraRepn); //toCameraRepn.removePathMatrixListener(tcrl);
		toCameraRepn = (SceneGraphPath) l.get(0);
		if (copyCat)	{
			if (cameraRepn.getTransformation() == null) 
				cameraRepn.setTransformation(new Transformation());
			cameraRepn.getAppearance().setAttribute("discreteGroup.cameraRep", toCameraRepn, SceneGraphPath.class);			
			//return;
		}
		toCameraRepn.pop();			// get rid of the node containing the camera representation
				
		toCameraNode = graphicsContext.getCameraPath();
		DiscreteGroupUtility.logger.log(Level.FINE,"camera path is "+toCameraNode.toString());
		if (cameraRepnTimer == null)	{
			cameraRepnTimer = new de.jreality.tools.Timer(20, new ActionListener()	{
				public void actionPerformed(ActionEvent e) {if (active) updateCameraRepn(); } } );			
		}
		cameraRepnTimer.attach(ToolSystem.getToolSystemForViewer(viewer));
		cameraRepnTimer.start();
	}
	
	public void updateCameraRepn()	{
		if (graphicsContext != null && cameraRepn != null && cameraRepn.isVisible())	{
			double[] rootToCameraRepn = toCameraRepn.getInverseMatrix(null);
			double[] cameraToRoot = toCameraNode.getMatrix(null);
			if (SystemProperties.isPortal)	{
				cameraToRoot = Rn.times(null, cameraToRoot, CameraUtility.inverseCameraOrientation.getArray());
				//System.err.println("Adjusting camera by "+Rn.matrixToString(CameraUtility.cameraOrientation.getArray()));
			} 
			final double[] cameraToCameraRepn = Rn.times(null, rootToCameraRepn, cameraToRoot);
			Scene.executeReader(cameraRepn, new Runnable()	{
				public void run() {
					if (active) cameraRepn.getTransformation().setMatrix(cameraToCameraRepn);					
				}
			});
		}
	}

	public SceneGraphComponent getWorldNode() {
		return worldNode;
	}

	public SceneGraphComponent getFundamentalRegion() {
		return fundamentalRegion;
	}
	public void setWorldNode(SceneGraphComponent w) {
		if (w == worldNode) return;
		oldWorldNode = worldNode;
		worldNode = w;
		newWorldNode = true;
		if (worldNode.getAppearance() == null) 
			worldNode.setAppearance(new Appearance());
	}
	
	private boolean followsCamera = false;
	private boolean clipToCamera = false;

	de.jreality.tools.Timer followTimer = null, clipTimer = null, cameraRepnTimer = null;
	DiscreteGroupCameraFollower dgcf = null;
	DiscreteGroupViewportConstraint viewportConstraint = null;
	public void followCamera( WingedEdge dd, int interval, boolean f)	{
		followsCamera = f;
		followDelay = interval;
		if (dd == null)	{
			dirdom.update();
			standardDD = (WingedEdge) dirdom.getDirichletDomain();
//			standardDD = (WingedEdge) DiscreteGroupUtility.calculateDirichletDomain(standardDD, theGroup);
		}
		if (graphicsContext == null) {
			throw new IllegalArgumentException("Graphics context is null; can't follow camera!");
		}
		if (cameraRepn != null) setupCameraRepn();
		SceneGraphPath sgp = graphicsContext.getCurrentPath();
		avatarPath = getAvatarPath();
		dgcf = new DiscreteGroupCameraFollower(standardDD, avatarPath, sgp, this);
		if (copyCat) return;
		if (followTimer != null ) followTimer.stop();
		else 	{
			followTimer = new de.jreality.tools.Timer(interval, new ActionListener()	{
				public void actionPerformed(ActionEvent e) {
					if (active) dgcf.update(); 
				} 
			} );
			ToolSystem toolSystemForViewer = ToolSystem.getToolSystemForViewer(viewer);
			if (toolSystemForViewer == null) {
				throw new IllegalStateException();
			}
			followTimer.attach(toolSystemForViewer);

		}
		if (followsCamera) followTimer.start();
	}
	
	public void clipToCamera(DiscreteGroupViewportConstraint theC, int interval, boolean c)	{
		clipToCamera = c;
		clipDelay = interval;
		setViewportConstraint(theC);
		viewportConstraint.setGraphicsContext(graphicsContext);
		if (copyCat) return;
//		if (copyCat && officialElementList == null) return;
		if (clipTimer != null ) clipTimer.stop();
		if (clipTimer == null) {
			clipTimer = new de.jreality.tools.Timer(interval, new ActionListener()	{
				public void actionPerformed(ActionEvent e) {
					if (active) {
						updateClipper();
//						System.err.println("updating clipper");
					}
				} 
			} );
			clipTimer.attach(ToolSystem.toolSystemForViewer(viewer));
		}
		System.err.println("clip to camera: "+clipToCamera);
		if (clipToCamera) {
			clipTimer.start();
			System.err.println();
		}
	}
	
	double[] oldModelToNDC = Rn.identityMatrix(4);
	private void updateClipper()	{
//		if (copyCat) return;
		viewportConstraint.update();
		double[] modeltoNDC =  viewportConstraint.getModelToNDC();
		if (! Rn.equals(oldModelToNDC, modeltoNDC, 10E-8)) 	{	
			Rn.copy(oldModelToNDC, modeltoNDC);
			double[] cameraToObject = viewportConstraint.getGraphicsContext().getCameraToObject(); //graphicsContext.getCameraToObject();
//			double[] tmp = Rn.matrixTimesVector(null, cameraToObject, P3.originP3);
//			viewportConstraint.setCenterPoint(tmp);
			System.err.println("clipping to camera");
			DiscreteGroupUtility.applyConstraint(viewportConstraint, theSceneGraphRepn, theGroup.getMetric());
		}
	}

	public void setFollowsCamera(boolean b)	{
		followsCamera = b;
		System.err.println("Follow camera is: "+followsCamera);
		if (followTimer != null)	{
			if (followsCamera) followTimer.start();
			else followTimer.stop();
		}
	}
	
	public boolean isFollowsCamera()	{
		return followsCamera;
	}
	
	public void setClipToCamera(boolean b)	{
		clipToCamera = b;
		if (clipTimer != null)	{
			if (clipToCamera) clipTimer.start();
			else clipTimer.stop();
		}
	}
	
	public boolean isClipToCamera()	{
		return clipToCamera;
	}
	
	public void dispose() {
		detachFromViewer();
		setFollowsCamera(false);
		setClipToCamera(false);
		if (ccaioTimer != null) ccaioTimer.stop();
	}

	WingedEdge standardDD = null;
	SceneGraphPath insertionPath = null;

	public void setViewer(Viewer v)	{
		viewer = v;
	}
	public void attachToViewer(Viewer v, SceneGraphPath sgp)		{
		viewer = v;
		if (sgp == null) {
			insertionPath = new SceneGraphPath();
			insertionPath.push(v.getSceneRoot());
		} else insertionPath = sgp;
		SceneGraphComponent insertAt = insertionPath.getLastComponent();
		if (insertAt ==null)	{
			throw new IllegalStateException("Path has no component");
		}
		if (!insertAt.isDirectAncestor(followCameraNode))  
			insertAt.addChild(followCameraNode);
		List l = SceneGraphUtility.getPathsBetween(v.getSceneRoot(), followCameraNode);
		SceneGraphPath pathToRepn = (SceneGraphPath) l.get(0);
		graphicsContext = new Graphics3D(v, pathToRepn);
		
//		if (followsCamera) 
		if (theGroup.getDimension() == 3)
			followCamera(null, followDelay, followsCamera);
//		if (clipToCamera) 
			clipToCamera(viewportConstraint, clipDelay, clipToCamera);
		if (copyCat)	{

			l = SceneGraphUtility.getPathsBetween(v.getSceneRoot(), theSceneGraphRepn);
			pathToMatrices = (SceneGraphPath) l.get(0);
			if (dummyCamera == null)	{
				dummyCamera = new SceneGraphComponent("cc dummy camera");
				dummyCamera.setCamera(CameraUtility.getCamera(v));
				dummyAvatar = new SceneGraphComponent("cc dummy avatar");
				dummyAvatar.setTransformation(new Transformation());
			}
//			if (!CameraUtility.getCameraNode(v).isDirectAncestor(dummyCamera))
//				CameraUtility.getCameraNode(v).addChild(dummyCamera);
			SceneGraphPath cp = new SceneGraphPath(v.getCameraPath());
			avatarPath = getAvatarPath();
			SceneGraphComponent avatarNode = avatarPath.getLastComponent();
			cp.replace(avatarNode, dummyAvatar);
			cp.insertAfter(dummyCamera, cp.getLastComponent());
			contextMatrixList = new Graphics3D(cp, pathToMatrices, CameraUtility.getAspectRatio(v));
			ccaio = new CopyCatAllInOne();
//			System.err.println("Follow, clip = "+followsCamera+" "+clipToCamera);
			if (ccaioTimer != null) ccaioTimer.stop();
			ccaioTimer = new Timer(clipDelay, new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					if (active) ccaio.update();
				}
				
			});
//			ccaioTimer.attach(ToolSystem.getToolSystemForViewer(viewer));
			ccaioTimer.start();			
		}

	}
	int followDelay = 200, clipDelay = 200;
	Viewer viewer;
	private Graphics3D contextMatrixList;
	private CopyCatAllInOne ccaio;
	private SceneGraphComponent dummyCamera, dummyAvatar;
	private Timer ccaioTimer;
	public void attachToViewer(Viewer v, SceneGraphPath sgp, boolean follow, int fdelay, boolean clip, int cdelay)		{
		attachToViewer(v, sgp);
//		if (copyCat) return;
		if (!copyCat) followCamera(null, fdelay, follow);	
		clipToCamera(viewportConstraint, clipDelay, clip);
	}
	
	public void detachFromViewer()	{
		if (insertionPath != null && insertionPath.getLastComponent().isDirectAncestor(followCameraNode))  
			insertionPath.getLastComponent().removeChild(followCameraNode);
	}
	
	public void setViewportConstraint(DiscreteGroupViewportConstraint theC)	{
		viewportConstraint = theC;
		if (viewportConstraint == null) {
			viewportConstraint = new DiscreteGroupViewportConstraint( 3.0, 3,12.0, 10, graphicsContext);
		}
		graphicsContext = viewportConstraint.getGraphicsContext();
	}

	public DiscreteGroupViewportConstraint getViewportConstraint() {
		return viewportConstraint;
	}
	public boolean isCopyCat() {
		return copyCat;
	}
	public SceneGraphComponent getSceneGraphRepn() {
		return theSceneGraphRepn;
	}
	public Appearance[] getAppList() {
		return appList;
	}
	public void setAppList(Appearance[] appList) {
		this.appList = appList;
		newAppList = true;
	}
	public MatrixListData getDropBox() {
		return theDropBox;
	}
	
	public void setAvatarPath(SceneGraphPath ap)	{
		avatarPath = ap;
	}
	
	public SceneGraphPath getAvatarPath()	{
		if (avatarPath == null)	{
			List<SceneGraphPath> paths = SceneGraphUtility.getPathsToNamedNodes(viewer.getSceneRoot(), "avatar");
			if (paths != Collections.EMPTY_LIST) avatarPath = paths.get(0);			
		}
		return avatarPath;
	}
	
	public int getFollowDelay() {
		return followDelay;
	}
	public void setFollowDelay(int followDelay) {
		this.followDelay = followDelay;
	}
	public int getClipDelay() {
		return clipDelay;
	}
	public void setClipDelay(int clipDelay) {
		this.clipDelay = clipDelay;
	}
	
	public void setActive(boolean b)	{
		active = b;
	}
	
	public boolean isActive()	{
		return active;
	}
	
	public boolean isFlatten() {
		return flatten;
	}
	public void setFlatten(boolean flatten) {
		this.flatten = flatten;
		updateFlatten();
	}

	public boolean isShadeGeometry() {
		return shadeGeometry;
	}
	public void setShadeGeometry(boolean shadeGeometry) {
		this.shadeGeometry = shadeGeometry;
	}
	
	
	public Component getInspector() {
		Box vbox = Box.createVerticalBox();
		vbox.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5),
				BorderFactory.createTitledBorder(BorderFactory
						.createEtchedBorder(), "diamond crystal")));
		Box hbox = Box.createHorizontalBox();
		vbox.add(hbox);
		final TextSlider<Integer> lSlider = new TextSlider.Integer("xyz # cutoff",  SwingConstants.HORIZONTAL, -1, 12000 ,cutoff);
		lSlider.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				cutoff = lSlider.getValue().intValue();
				getDropBox().setCutoff(doCutoff ? cutoff : -1);
			}
		});
		hbox.add(lSlider);
		
		final JCheckBox tcb = new JCheckBox("Do cutoff");
		tcb.setSelected(doCutoff);
		tcb.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				doCutoff = tcb.isSelected();
				getDropBox().setCutoff(doCutoff ? cutoff : -1);
			}
		});
		hbox.add(tcb);
		return vbox;
	}


	class CopyCatAllInOne	{
		
		private double[] mat = new double[16], mat2, mat3;
		private double[] tmp2 = new double[4];
		private double inverseDMin, inverseDMax;
		private double[] o2ndc;
		private double[] o2c;
		private double ndcFudge, ztlate;
		int sig = theGroup.getMetric();
		private double[] newShipMatrixs;
		private boolean allVisible = false;
		public void update()	{
			if (clipToCamera)	{
				viewportConstraint.update();
				ndcFudge = viewportConstraint.getFudge();
				ztlate = viewportConstraint.getZtlate();
				if (followsCamera) {
					dgcf.setWriteImmediately(false);
					dgcf.update();
					newShipMatrixs = dgcf.getNewShipMatrix();
				} else newShipMatrixs = avatarPath.getLastComponent().getTransformation().getMatrix();
				dummyAvatar.getTransformation().setMatrix(newShipMatrixs);
				// the following is an attempt to use a slightly larger
				// viewing frustum (moved back to include more of the world)
				// to clip against.
				double zcoord = Pn.coordForDistance(ztlate, theGroup.getMetric());
				MatrixBuilder.init(null, theGroup.getMetric()).
					translate(0,0,zcoord).
					assignTo(dummyCamera);
				contextMatrixList.setAspectRatio(CameraUtility.getAspectRatio(viewer));
				o2ndc = contextMatrixList.getObjectToNDC();
				o2c = contextMatrixList.getObjectToCamera();
				inverseDMin = inverseDistance(viewportConstraint.getMinDistance(), theGroup.getMetric());
				inverseDMax = inverseDistance(viewportConstraint.getMaxDistance()+zcoord, theGroup.getMetric());
				final int n = theDropBox.getMatrixList().length;
				int count = 0;
				final boolean[] accepted2 = new boolean[n];
				double[][] mlist = theDropBox.getMatrixList();
				for (int i = 0; i<n; ++i)	{
					boolean b = accept(mlist[i]);
					accepted2[i] = b;
					if (b) count++;
					// newAccepted[i] = accept(theDropBox.matrixList[i]);
				}
				theDropBox.setVisibleList(accepted2);
				while (theDropBox.isRendering())
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				avatarPath.getLastComponent().getTransformation().setMatrix(newShipMatrixs);
				theDropBox.setNewVisibleList(true);
				theDropBox.setCount(count);
//				System.err.println("Constraint = "+viewportConstraint.toString());
//				System.err.println("ccaio: count = "+count+" maxd = "+viewportConstraint.maxDistance);
//				viewer.renderAsync();
				allVisible = false;
			} else if (!allVisible)	{
					final int n = theDropBox.getMatrixList().length;
					final boolean[] accepted2 = new boolean[n];
					for (int i = 0; i<n; ++i)	{
						accepted2[i] = true;
					}
					theDropBox.setVisibleList(accepted2);
					theDropBox.setNewVisibleList(true);
					theDropBox.setCount(n);
					System.err.println("Setting all nodes visible");
					allVisible = true;
				}
				dgcf.setWriteImmediately(true);
				if (followsCamera) dgcf.update();	
			}
		
		protected  boolean accept(final double[] m) { //double[] objectToNDC, double[] o2c, double minDistance, double maxDistance, double[] m, int metric) {
				mat3 = m;
				mat2 = o2c; if (mat2 == null) return false; 
				fastTimes();
				tmp2[0] = mat[3];  tmp2[1] = mat[7];  tmp2[2] = mat[11];  tmp2[3] = mat[15];
				double d = inverseDistanceToOrigin(tmp2, sig);
//				System.err.println("coshd = "+d);
				if (inverseDMin > 0.0 &&  d < inverseDMin) return true;
				if (inverseDMax > 0 && d > inverseDMax) return false;
				mat2 = o2ndc; fastTimes();
				tmp2[0] = mat[3];  tmp2[1] = mat[7];  tmp2[2] = mat[11];  tmp2[3] = mat[15];
				Pn.dehomogenize(tmp2,tmp2);
				if (tmp2[0] > ndcFudge || tmp2[0] < -ndcFudge) return false;
				if (tmp2[1] > ndcFudge || tmp2[1] < -ndcFudge) return false;
				if (tmp2[2] > 1.0 || tmp2[2] < -1.0) return false;
				return true;
			}

		private void fastTimes()	{
			for (int i=0; i<4; ++i)	{	
				for (int j=0; j<4; ++j)	{
					mat[i*4+j] = 0.0;
					for (int k=0; k<4; ++k)		{
						// the (i,j)th position is the inner product of the ith row and 
						// the jth column of the two factors
						mat[i*4+j] += mat2[i*4+k]*mat3[k*4+j];
					}
				}
			}

		}
		

	}
	/**
	 * optimize calculation of the cosh of  the distance to (0,0,0,1)
	 * @param src
	 * @param metric
	 * @return
	 */
	public static double inverseDistanceToOrigin(double[] u, int sig)	{
		// assert dim checks
		double d = 0;
		int n = u.length;
		switch(sig)	{
			default:
				// error: no such metric.  fall through to euclidean case
			case Pn.EUCLIDEAN:
				double ul,  tmp;
				ul = u[n-1]; 
				d = Rn.innerProduct(u,u,n-1);
				d = Math.sqrt(d);
				if ( !(d==0 || d == 1.0))	d /= ul;
				break;
			case Pn.HYPERBOLIC:
				double uu, uv;
				uu = Pn.innerProduct(u, u, sig);
				uu = (uu>0? uu : -uu);
				uv = u[n-1] > 0 ? u[n-1] : -u[n-1];
				if (uu == 0) 	// error: infinite distance
					return (Double.MAX_VALUE);
				d = (uv)/Math.sqrt(uu);
				break;
			case Pn.ELLIPTIC:
				uu = Pn.innerProduct(u, u, sig);
				uv = u[n-1];
				d = (uv)/Math.sqrt(Math.abs(uu));
				break;
			}
		return d;
		
	}
	
	public static double inverseDistance(double d, int metric)	{
		switch (metric) {
			case Pn.EUCLIDEAN:
				return d;
			case Pn.HYPERBOLIC:
				return Pn.cosh(d);
			case Pn.ELLIPTIC:
				return Math.cos(d);
		}
		return d;
	}
	public static SceneGraphComponent getGroupElementOnPath(SceneGraphPath rootToLocal) {
		int n = rootToLocal.getLength();
		for (int i = n-1; i>=0; --i)	{
			SceneGraphNode sgn = rootToLocal.get(i);
			if (sgn instanceof SceneGraphComponent && sgn.getName().startsWith("dge")) {
//				System.err.println("Found "+sgn.getName());
				return ((SceneGraphComponent) sgn);
			}
		}
		return null;
	}


}
//DiscreteGroup dg = new DiscreteGroup();
//dg.setDimension(3);
//dg.setMetric(Pn.EUCLIDEAN);
//DiscreteGroupElement[] list = new DiscreteGroupElement[n*n];
//double dx = .2/n, dy = .1/n;
//
//for (int i = 0; i<n; ++i)	{
//	for (int j = 0; j<n; ++j)	{
//		Matrix m = new Matrix();
//		MatrixBuilder.euclidean().translate((i*dx - dx/2), j*dy-dy/2, 0).assignTo(m);
//		list[n*i+j] = new DiscreteGroupElement(Pn.EUCLIDEAN, m.getArray());
//	}
//}
//dg.setElementList(list);
//shadowWorld.setGeometry(regularPolygon);
//DiscreteGroupSceneGraphRepresentation dgsgr = new DiscreteGroupSceneGraphRepresentation(dg, false);
//dgsgr.setWorldNode(shadowWorld);
//dgsgr.update();
//return shadowWorld; //dgsgr.getRepresentationRoot();

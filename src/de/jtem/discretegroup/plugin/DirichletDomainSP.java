/**
 *
a * This package is open source software, made available under a BSD license:
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

import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.FACE_DRAW;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;

import java.awt.Color;
import java.awt.Event;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.SwingConstants;

import de.jreality.backends.label.LabelUtility;
import de.jreality.geometry.BoundingBoxUtility;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.Primitives;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.plugin.basic.View;
import de.jreality.reader.Readers;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.Tool;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.tools.RotateTool;
import de.jreality.tools.Timer;
import de.jreality.toolsystem.ToolSystem;
import de.jreality.toolsystem.ToolUtility;
import de.jreality.ui.viewerapp.FileLoaderDialog;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;
import de.jtem.beans.InspectorPanel;
import de.jtem.discretegroup.core.DirichletDomain;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.plugin.TessellatedContent.GroupChangedListener;
import de.jtem.discretegroup.util.RopeTextureFactory;
import de.jtem.discretegroup.util.TextSlider;
import de.jtem.discretegroup.util.TextSlider.Double;
import de.jtem.discretegroup.util.TextSlider.DoubleLog;
import de.jtem.discretegroup.util.TranslateTool;
import de.jtem.discretegroup.util.WingedEdge;
import de.jtem.discretegroup.util.WingedEdgeUtility;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;

public class DirichletDomainSP extends ShrinkPanelPlugin implements GroupChangedListener{

	private static final long serialVersionUID = 1L;

	Viewer viewer;
	IndexedFaceSet standardFundDomain, scaledFundDomain;
	SceneGraphComponent scaledDD, geometrySGC = new SceneGraphComponent("tlate"), 
			geometry2SGC  = new SceneGraphComponent("rotate"), beams, allSGC, letterSGC, loadSGC;
	DiscreteGroup theGroup;
	DirichletDomain dirdom, scaledDirDom;
	int maxDirDomOrbitSize = 500;
	boolean showLetters = true, loaded = false, autoRotate = false;
	
	private TessellatedContent tc;
	double radius = .06,		// size of beams
		stretchFactor = .2, // scale of reduced fundamental domain
		carouselSpeed = 1;		
	private DoubleLog beamRad, rotateSpeed;
	private Double stretch;
	private boolean applyTexture = true,
			installRotateTool = true;
	Appearance withTexture = new Appearance(), noTexture = new Appearance();
	Texture2D tex2d;
	RopeTextureFactory stf = new RopeTextureFactory(withTexture);
	Matrix dtm = MatrixBuilder.euclidean().rotateY(.015).getMatrix();
	Matrix gm = null;
	Timer carouselTimer = new Timer(20, new ActionListener() {
		public void actionPerformed(ActionEvent arg0) {
			dtm = MatrixBuilder.euclidean().rotateY(.02 * carouselSpeed).getMatrix();
			MatrixBuilder.init(gm, Pn.EUCLIDEAN).times(dtm).assignTo(geometry2SGC);
		}
	}) {

		@Override
		public void start() {
			gm = new Matrix(geometry2SGC.getTransformation().getMatrix());
			super.start();
		}

		@Override
		public void stop() {
			// TODO Auto-generated method stub
			super.stop();
		}
		
	};
	private JCheckBox showLetterB;

	private transient  String string = "AMS\nMAA"; //"d";	//"DFG""AMS"; //

	private Tool rotateT;
	
	public DirichletDomainSP(TessellatedContent tc) {
		shrinkPanel.setName("Dirichlet domain plugin");
		this.tc = tc;
		setupGUI();
		tc.addGroupChangedListener(this);
		stf.setN(15);
		stf.setM(1);
		stf.setBand2color(new Color(255,255,50)); //Color.black); //
		stf.setShadowwidth(.05);
		stf.setBandwidth(.8);
		stf.update();
		tex2d = stf.getTexture2D();
		System.err.println("Using rope texture");
//		if (theGroup.getMetric() == Pn.EUCLIDEAN)	
		loadSGC = new SceneGraphComponent("loaded");
		letterSGC = SceneGraphUtility.createFullSceneGraphComponent("letterSGC");
		double xStep = 1.5, yStep = xStep;
		double[] verts = {-xStep/2,-yStep/2,0, xStep/2,-yStep/2,0,xStep/2,yStep/2,0,-xStep/2,yStep/2,0};
		IndexedFaceSet square = Primitives.texturedQuadrilateral(verts);
//		letterSGC.setGeometry(square);
		letterSGC.setVisible(showLetters);
//		DefaultGeometryShader dgs = (DefaultGeometryShader) ShaderUtility.createDefaultGeometryShader(ap, true);
//        TwoSidePolygonShader tsps = (TwoSidePolygonShader) dgs.createPolygonShader("twoSide");
//        DefaultPolygonShader dpsb =  (DefaultPolygonShader) tsps.createBack("default");
//        DefaultPolygonShader dpsf =  (DefaultPolygonShader)tsps.createFront("default");
//        dpsb.setDiffuseColor(new Color(153, 255, 100));
//        dpsf.setDiffuseColor(new Color(255, 176, 158));

//			ap.setAttribute(FAST_AND_DIRTY, false);
		Appearance ap = new Appearance();
		ap.setAttribute(FACE_DRAW, true);
		ap.setAttribute(EDGE_DRAW, false);
		letterSGC.setAppearance(ap);
		BufferedImage im = LabelUtility.createImageFromString(string,new Font("Sans Serif",Font.BOLD,384), Color.white);//Color.yellow);
		Texture2D texture2d = TextureUtility.createTexture(ap, POLYGON_SHADER,new ImageData(im));
		texture2d.setApplyMode(Texture2D.GL_MODULATE);
		texture2d.setRepeatS(Texture2D.GL_CLAMP);
		texture2d.setRepeatT(Texture2D.GL_CLAMP);	
		texture2d.setTextureMatrix(MatrixBuilder.euclidean().translate(.5,.5,0).scale(1,-1,1).translate(-.5,-.5,0).getMatrix());
		SceneGraphComponent frontSGG = SceneGraphUtility.createFullSceneGraphComponent("front"),
				backSGC =  SceneGraphUtility.createFullSceneGraphComponent("front");
		MatrixBuilder.euclidean().translate(0,0,.005).assignTo(backSGC);
		letterSGC.addChildren(frontSGG, backSGC);
		frontSGG.setGeometry(square);
		backSGC.setGeometry(square);
		frontSGG.getAppearance().setAttribute("polygonShader.diffuseColor", new Color(153, 255, 100));
		backSGC.getAppearance().setAttribute("polygonShader.diffuseColor", new Color(255, 176, 158));

	}

	public DirichletDomain getDirdom() {
		return dirdom;
	}

	public void groupChanged(Event cce) {
		viewer = tc.viewer;
		theGroup = tc.getGroup();
		dirdom = new DirichletDomain(theGroup);
		dirdom.setDirichletDomainOrbit(maxDirDomOrbitSize);
		dirdom.update();
		// sigh .. copy visitor doesn't seem to work correctly
		scaledDirDom = new DirichletDomain(theGroup);
		scaledDirDom.setDirichletDomainOrbit(maxDirDomOrbitSize);
		scaledDirDom.update();
		updateGeometry();
		tc.setContent(allSGC);
	}
		
	protected void updateGeometry()	{
		dirdom.update();
		standardFundDomain =  dirdom.getDirichletDomain();
		scaledDirDom.update();
		scaledFundDomain =  scaledDirDom.getDirichletDomain();
		updateGeometryScale();
		scaledDD = new SceneGraphComponent("scaledDD");
		scaledFundDomain.setName("scaled fundamental domain");
		scaledDD.setGeometry(scaledFundDomain);
		scaledDD.setTransformation(new Transformation());
		geometrySGC = new SceneGraphComponent("tlate");
		geometry2SGC  = new SceneGraphComponent("rotate");
	    carouselTimer.attach(ToolSystem.getToolSystemForViewer(viewer));
		geometrySGC.addChild(geometry2SGC);
		geometrySGC.setTransformation(new Transformation());
		geometry2SGC.setTransformation(new Transformation());
		geometry2SGC.addChildren(scaledDD, letterSGC, loadSGC);
		beams = WingedEdgeUtility.createBeamsOnEdges((WingedEdge) standardFundDomain, null, radius, 4, 5);
		beams.setAppearance(applyTexture ? withTexture : noTexture);
		beams.setPickable(false);
		allSGC = SceneGraphUtility.createFullSceneGraphComponent("dirichlet domain collector");
		allSGC.addChildren(beams, geometrySGC);
		updateVisibility();
		rotateT = new RotateTool();
		geometry2SGC.addTool(rotateT);
		geometrySGC.addTool(new TranslateTool());
		geometry2SGC.addTool(new de.jtem.discretegroup.util.ResetMatrixTool(InputSlot.META_LEFT_BUTTON));
		geometrySGC.addTool(new de.jtem.discretegroup.util.ResetMatrixTool(InputSlot.META_LEFT_BUTTON));
	}

	private void updateVisibility() {
		loadSGC.setVisible(loaded);
		scaledDD.setVisible(!loaded && !showLetters);
		letterSGC.setVisible(!loaded && showLetters);
		viewer.renderAsync();
	}
	
	protected void updateGeometryScale()	{
		if (stretch != null) stretch.setValue(stretchFactor);
		double[] mat = MatrixBuilder.init(null,theGroup.getMetric()).scale(stretchFactor).getArray();
		
		double[][] verts = standardFundDomain.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		double[] center = Rn.average(null, verts);
		double[] tlate = P3.makeTranslationMatrix(null, center, theGroup.getMetric());
		mat = Rn.conjugateByMatrix(null, mat, tlate);
		verts = Rn.matrixTimesVector(null, mat, verts);
		scaledFundDomain.setVertexAttributes(Attribute.COORDINATES,StorageModel.DOUBLE_ARRAY.array(verts[0].length).createReadOnly(verts));
		IndexedFaceSetUtility.calculateAndSetFaceNormals(scaledFundDomain, Pn.EUCLIDEAN);//theGroup.getMetric());
		Matrix scaleM = new Matrix(mat);
		scaleM.assignTo(letterSGC);
		if (loadSGC != null) scaleM.assignTo(loadSGC);
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
		
		beamRad = new TextSlider.DoubleLog("beam radius",
				SwingConstants.HORIZONTAL,.001,.3,radius);
		beamRad.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				radius = beamRad.getValue().doubleValue();
				WingedEdgeUtility.createBeamsOnEdges(beams,(WingedEdge) standardFundDomain,(IndexedFaceSet) beams.getGeometry(), radius, 4, 5);
				beams.setAppearance(applyTexture ? withTexture : noTexture);
				viewer.renderAsync();
			}
			
		});
		stretch = new TextSlider.Double("dirdom scale",
				SwingConstants.HORIZONTAL,.001,1,stretchFactor);
		stretch.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				stretchFactor = stretch.getValue().doubleValue();
				updateGeometryScale();
				viewer.renderAsync();
			}
			
		});
		rotateSpeed = new TextSlider.DoubleLog("carousel speed",
				SwingConstants.HORIZONTAL, .1, 10, carouselSpeed);
		rotateSpeed.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				carouselSpeed = rotateSpeed.getValue().doubleValue();
				viewer.renderAsync();
			}
			
		});

		final JCheckBox applyTexB = new JCheckBox("apply texture");
		applyTexB.setSelected(applyTexture);
		applyTexB.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				applyTexture = applyTexB.isSelected();
				beams.setAppearance(applyTexture ? withTexture : noTexture);
			}
		});

		final JCheckBox rotateB = new JCheckBox("rotate tool");
		rotateB.setSelected(installRotateTool);
		rotateB.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				installRotateTool = rotateB.isSelected();
				if (installRotateTool) geometry2SGC.addTool(rotateT);
				else geometry2SGC.removeTool(rotateT);
			}
		});

		final JCheckBox carouselB = new JCheckBox("carousel");
		carouselB.setSelected(false);
		carouselB.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				boolean foo = carouselB.isSelected();
				if (foo) carouselTimer.start();
				else carouselTimer.stop();
			}
		});

		showLetterB = new JCheckBox("show letter");
		showLetterB.setSelected(showLetters);
		showLetterB.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				showLetters = showLetterB.isSelected();
				loaded = false;
				updateVisibility();
			}
		});

		final JButton loadB = new JButton("load ...");
		loadB.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				   File files = FileLoaderDialog.loadFile(shrinkPanel, (JComponent) null);
				    if (files == null) return;  //dialog cancelled
				    try {
						SceneGraphComponent loded  = Readers.read(files);
				    		setupLoadedScene(loded);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
			}
		});


		c.weightx = 1.0;
		shrinkPanel.add(applyTexB, c);
		c.weightx = 1.0;
		shrinkPanel.add(rotateB, c);
		c.weightx = 1.0;
		shrinkPanel.add(carouselB, c);
		c.weightx = 1.0;
		shrinkPanel.add(loadB, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		shrinkPanel.add(showLetterB, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		shrinkPanel.add(stretch, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		shrinkPanel.add(rotateSpeed, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		shrinkPanel.add(beamRad, c);
		InspectorPanel ip = new InspectorPanel();
		ip.setObject(stf, "update");
		shrinkPanel.add(ip);
		
		
	}

	public boolean isApplyTexture() {
		return applyTexture;
	}

	public void setApplyTexture(boolean applyTexture) {
		this.applyTexture = applyTexture;
		beams.setAppearance(applyTexture ? withTexture : noTexture);

	}

	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

	private void setupLoadedScene(SceneGraphComponent loded) {
		SceneGraphUtility.removeChildren(loadSGC);
		loadSGC.addChild(loded);
		Rectangle3D r3d = BoundingBoxUtility.calculateBoundingBox(loded);
		double[] center = Rn.times(null, -1, r3d.getCenter());
		double[] extent = r3d.getExtent();
		double scale = Rn.maxNorm(extent) / 2.0;
		scale = 1.0 / scale;
		MatrixBuilder.euclidean().scale(scale).translate(center)
				.assignTo(loded);
		loaded = true;
		showLetters = false;
		showLetterB.setSelected(showLetters);
		updateVisibility();
	}
		
	
}

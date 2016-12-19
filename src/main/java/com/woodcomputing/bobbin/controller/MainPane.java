/*
 * Bobbin
 * Copyright 2016 Jonathan Wood
 * Licensed under the Apache License, Version 2.0
 */
package com.woodcomputing.bobbin.controller;

import com.google.inject.Inject;
import com.woodcomputing.bobbin.format.BobbinFormat;
import com.woodcomputing.bobbin.format.jef.JEFFormat;
import com.woodcomputing.bobbin.model.Design;
import com.woodcomputing.bobbin.model.Stitch;
import com.woodcomputing.bobbin.model.StitchGroup;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javax.swing.SwingUtilities;
import lombok.extern.log4j.Log4j2;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.svg.GVTTreeBuilderAdapter;
import org.apache.batik.swing.svg.GVTTreeBuilderEvent;
import org.apache.batik.util.RunnableQueue;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Jonathan Wood
 * 
 */
@Log4j2
public class MainPane extends StackPane {
    
    @FXML SwingNode swingNode;
    @FXML MenuItem fileOpen;
    @FXML MenuItem fileQuit;
    
    private final JSVGCanvas svgCanvas;
    private RunnableQueue queue;
    private Element designs;
    
    @Inject
    public MainPane(JSVGCanvas svgCanvas) {
        this.svgCanvas = svgCanvas;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/woodcomputing/bobbin/fxml/MainPane.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException ex) {
            log.catching(ex);
        }
    }
    
    @FXML
    private void initialize() {
        svgCanvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);
        svgCanvas.addGVTTreeBuilderListener(new GVTTreeBuilderAdapter() {
            @Override
            public void gvtBuildCompleted(GVTTreeBuilderEvent e) {
                queue = svgCanvas.getUpdateManager().getUpdateRunnableQueue();
                Document doc = svgCanvas.getSVGDocument();
                designs = doc.getElementById("designs");
            }
        });
        URL url = getClass().getResource("/com/woodcomputing/bobbin/controller/template.svg");
        String template = url.toString();
        svgCanvas.setURI(template);
        SwingUtilities.invokeLater(() -> {
            swingNode.setContent(svgCanvas);
        });
        
        fileOpen.setOnAction((event) -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Open Design File");
            File selectedFile = chooser.showOpenDialog(this.getScene().getWindow());
            if(selectedFile != null) {
                Design design = BobbinFormat.loadDesign(selectedFile);
                renderDesign(design);
            }
        });
        
        fileQuit.setOnAction((event) -> {
            System.exit(0);
        });
    }
    
    private void renderDesign(Design design) {
        queue.invokeLater(() -> {
            StringBuilder path = new StringBuilder();
            Element designGroup = svgCanvas.getSVGDocument().createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, SVGConstants.SVG_G_TAG);
            Element group = svgCanvas.getSVGDocument().createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, SVGConstants.SVG_G_TAG);
            Element element = svgCanvas.getSVGDocument().createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, SVGConstants.SVG_PATH_TAG);

            for (StitchGroup stitchGroup : design.getStitchGroups()) {
                for (Stitch stitch : stitchGroup.getStitches()) {
                    path.append("M").append(stitch.getStartX()).append(" ").append(stitch.getStarty()).append(" ");
                    path.append("L").append(stitch.getEndX()).append(" ").append(stitch.getEndY()).append(" ");
                }
                element.setAttributeNS(null, "stroke", stitchGroup.getColor());
                element.setAttributeNS(null, "stroke-width", ".25");
                element.setAttributeNS(null, "d", path.toString().trim());

                group.appendChild(element);
                designGroup.appendChild(group);
                group = svgCanvas.getSVGDocument().createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, SVGConstants.SVG_G_TAG);
                element = svgCanvas.getSVGDocument().createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, SVGConstants.SVG_PATH_TAG);
                path = new StringBuilder();
            }
            designs.appendChild(designGroup);
        });
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.woodcomputing.bobbin.controller;

import com.google.inject.Inject;
import java.io.IOException;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;
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
import org.w3c.dom.svg.SVGDocument;

/**
 *
 * @author jwood
 */
@Log4j2
public class MainPane extends StackPane {
    
    @FXML SwingNode swingNode;
    
    private final JSVGCanvas svgCanvas;
    private RunnableQueue queue;
    
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
                queue.invokeLater(() -> {
                    Element rect = svgCanvas.getSVGDocument().createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, SVGConstants.SVG_RECT_TAG);
                    rect.setAttributeNS(null, SVGConstants.SVG_X_ATTRIBUTE, "100");
                    rect.setAttributeNS(null, SVGConstants.SVG_Y_ATTRIBUTE, "100");
                    rect.setAttributeNS(null, SVGConstants.SVG_WIDTH_ATTRIBUTE, "100");
                    rect.setAttributeNS(null, SVGConstants.SVG_HEIGHT_ATTRIBUTE, "100");
                    rect.setAttributeNS(null, SVGConstants.SVG_FILL_ATTRIBUTE, "#2222ee");
                    svgCanvas.getSVGDocument().getDocumentElement().appendChild(rect);
                });
            }
        });
        Document doc = SVGDOMImplementation.getDOMImplementation().createDocument(SVGDOMImplementation.SVG_NAMESPACE_URI, "svg", null);
        svgCanvas.setSVGDocument((SVGDocument) doc);        

        SwingUtilities.invokeLater(() -> {
            swingNode.setContent(svgCanvas);
        });
    }
    
}

/*
 * FXSlang
 * Copyright 2016 Jonathan Wood
 * Licensed under the Apache License, Version 2.0
 */
package com.woodcomputing.bobbin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.woodcomputing.bobbin.model.jef.Hoop;
import com.woodcomputing.bobbin.model.jef.JEF;
import com.woodcomputing.bobbin.model.jef.JEFColor;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

/**
 *
 * @author Jonathan Wood
 * 
 */
@Log4j2
public class MainPane extends StackPane {
    
    @FXML SwingNode swingNode;
    
    private final JSVGCanvas svgCanvas;
    private RunnableQueue queue;
    private Document doc;
    
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
                readJEF();
            }
        });
        doc = SVGDOMImplementation.getDOMImplementation().createDocument(SVGDOMImplementation.SVG_NAMESPACE_URI, "svg", null);
        svgCanvas.setSVGDocument((SVGDocument) doc);        

        SwingUtilities.invokeLater(() -> {
            swingNode.setContent(svgCanvas);
        });
    }
    
    private void readPES() {
        
    }
    
    private void readJEF() {
        queue.invokeLater(() -> {
            List<JEFColor> jefColors = null;
            ObjectMapper mapper = new ObjectMapper();
            try (InputStream is = getClass().getResourceAsStream("jef_colors.json")) {
                jefColors = mapper.readValue(is, mapper.getTypeFactory().constructCollectionType(List.class, JEFColor.class));
            } catch (IOException ex) {
                log.catching(ex);
            }
            Map<Integer, JEFColor> jefColorMap = new HashMap<>();
            for (JEFColor jc : jefColors) {
                jefColorMap.put(jc.getCode(), jc);
            }

            byte[] bytes = null;
            try (InputStream is = getClass().getResourceAsStream("poppi.jef")) {
                bytes = IOUtils.toByteArray(is);
            } catch (IOException ex) {
                log.catching(ex);
            }
            ByteBuffer bb = ByteBuffer.wrap(bytes);
            bb.order(ByteOrder.LITTLE_ENDIAN);
            JEF jef = new JEF();
            jef.setFirstStitchLocation(bb.getInt(0));
            log.debug("First Stitch Location: {}", jef.getFirstStitchLocation());
            jef.setThreadChangeCount(bb.getInt(24));
            log.debug("Threads Changes: {}", jef.getThreadChangeCount());
            jef.setHoop(Hoop.id2Hoop(bb.getInt(32)));
            log.debug("Hoop: {}", jef.getHoop());
            jef.setStitchCount(bb.getInt(28));
            log.debug("Stitch Count: {}", jef.getStitchCount());

            bb.position(116);
            JEFColor[] colors = new JEFColor[jef.getThreadChangeCount()];
            for (int i = 0; i < jef.getThreadChangeCount(); i++) {
                colors[i] = jefColorMap.get(bb.getInt());
            }
            jef.setThreadColors(colors);
            for (int i = 0; i < jef.getThreadChangeCount(); i++) {
                log.debug("ThreadType{}: {}", i, bb.getInt());
            }

            int dx = 0;
            int dy = 0;
            int cx = 0;
            int cy = 0;
            int nx = 0;
            int ny = 0;

            int change = 1;
            boolean isMove = false;
            bb.position(jef.getFirstStitchLocation());
            JEFColor color = jef.getThreadColors()[change - 1];
            Element group = svgCanvas.getSVGDocument().createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, SVGConstants.SVG_G_TAG);
            Element element = svgCanvas.getSVGDocument().createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, SVGConstants.SVG_PATH_TAG);
            log.debug("Pen Color changed to: {} ({})", color.getRgb(), color.getName());
            int stitches = 0;
            final Element svgRoot = doc.getDocumentElement();
            svgRoot.setAttributeNS(null, "viewBox", "-600 -400 1200 800");
            StringBuilder path = new StringBuilder();

            for (int stitch = 1; stitch < jef.getStitchCount(); stitch++) {
                dx = bb.get();
                dy = bb.get();
                if (dx == -128) {
                    switch (dy) {
                        case 1:
                            log.debug("change");
                            change++;
                            color = jef.getThreadColors()[change - 1];
                            element.setAttribute("style", "stroke:" + color.getRgb() + ";");
                            String d = path.toString().trim();
                            log.debug("Path: {}", d);
                            element.setAttribute("d", d);
                            group.appendChild(element);
                            svgRoot.appendChild(group);
                            group = svgCanvas.getSVGDocument().createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, SVGConstants.SVG_G_TAG);
                            element = svgCanvas.getSVGDocument().createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, SVGConstants.SVG_PATH_TAG);
                            path = new StringBuilder();
                            log.debug("Pen Color changed to: {} ({})", color.getRgb(), color.getName());
                            break;
                        case 2:
                            log.debug("move");
                            isMove = true;
                            break;
                        case 16:
                            log.debug("last");
                            isMove = true;
                            break;
                    }
                } else {
                    nx = cx + dx;
                    ny = cy + dy;
                    if (isMove) {
                        isMove = false;
                    } else {
                        stitches++;
                        log.trace("cx {} cy {} nx {} ny {}", cx, cy, nx, ny);
                        path.append("M").append(cx/2).append(" ").append(-cy/4).append(" ");
                        path.append("L").append(nx/2).append(" ").append(-ny/4).append(" ");
                    }
                    cx = nx;
                    cy = ny;
                }
            }
            log.debug("Changes: {} Stitches {}", change, stitches);
        });

//        DOMSource domSource = new DOMSource(doc);
//        StringWriter writer = new StringWriter();
//        StreamResult result = new StreamResult(writer);
//        TransformerFactory tf = TransformerFactory.newInstance();
//        Transformer transformer;
//        try {
//            transformer = tf.newTransformer();
//            transformer.transform(domSource, result);
//        } catch (TransformerException ex) {
//            log.catching(ex);
//        }
//        String dom = writer.toString();
//        log.debug(dom);
    }
}

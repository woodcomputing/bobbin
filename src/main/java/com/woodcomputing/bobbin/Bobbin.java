/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.woodcomputing.bobbin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.woodcomputing.bobbin.controller.MainPane;
import com.woodcomputing.bobbin.module.BobbinModule;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author jwood
 */
@Log4j2
public class Bobbin extends Application {

    private final int width = 1200;
    private final int height = 800;
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Bobbin");
        Injector injector = Guice.createInjector(new BobbinModule());
        MainPane mainPane = injector.getInstance(MainPane.class);
        primaryStage.setWidth(width);
        primaryStage.setHeight(height);
        primaryStage.setScene(new Scene(mainPane));
        primaryStage.show();
    }

    public void read(GraphicsContext gc) {
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

        gc.setStroke(Paint.valueOf("#000000"));
        gc.fillOval(0.0, 0.0, 5.0, 5.0);
        gc.stroke();
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
        gc.setStroke(Paint.valueOf(color.getRgb()));
        log.debug("Pen Color changed to: {} ({})", color.getRgb(), color.getName());
        int stitches = 0;

        for (int stitch = 1; stitch < jef.getStitchCount(); stitch++) {
            dx = bb.get();
            dy = bb.get();
            if (dx == -128) {
                switch (dy) {
                    case 1:
                        log.debug("change");
                        change++;
                        color = jef.getThreadColors()[change - 1];
                        gc.setStroke(Paint.valueOf(color.getRgb()));
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
                    log.debug("cx {} cy {} nx {} ny {}", cx, cy, nx, ny);
//                    gc.moveTo(cx + width/2, -cy + height/2);
//                    gc.lineTo(nx + width/2, -ny + height/2);
                    gc.strokeLine(cx + width / 4, -cy + height / 4, nx + width / 4, -ny + height / 4);
                }
                cx = nx;
                cy = ny;
            }
        }
        log.debug("Changes: {} Stitches {}", change, stitches);
    }

}

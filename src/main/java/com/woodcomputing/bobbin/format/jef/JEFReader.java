/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.woodcomputing.bobbin.format.jef;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.woodcomputing.bobbin.model.Design;
import com.woodcomputing.bobbin.model.Stitch;
import com.woodcomputing.bobbin.model.StitchGroup;
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
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author jwood
 */
@Log4j2
public class JEFReader {

    public Design readJEF() {
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
        int stitches = 0;
        boolean isMove = false;
        bb.position(jef.getFirstStitchLocation());
        JEFColor color = jef.getThreadColors()[change - 1];
        Design design = new Design();
        StitchGroup stitchGroup = new StitchGroup();
        stitchGroup.setColor(color.getRgb());

        for (int stitch = 1; stitch < jef.getStitchCount(); stitch++) {
            dx = bb.get();
            dy = bb.get();
            if (dx == -128) {
                switch (dy) {
                    case 1:
                        log.debug("change");
                        change++;
                        color = jef.getThreadColors()[change - 1];
                        design.getStitchGroups().add(stitchGroup);
                        stitchGroup = new StitchGroup();
                        stitchGroup.setColor(color.getRgb());
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
                    Stitch designStitch = new Stitch(cx, -cy, nx, -ny);
                    stitchGroup.getStitches().add(designStitch);
                }
                cx = nx;
                cy = ny;
            }
        }
        log.debug("Changes: {} Stitches {}", change, stitches);
        return design;
    }

}

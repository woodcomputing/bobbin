/*
 * Bobbin
 * Copyright 2016 Jonathan Wood
 * Licensed under the Apache License, Version 2.0
 */
package com.woodcomputing.bobbin.format;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.woodcomputing.bobbin.model.Design;
import com.woodcomputing.bobbin.model.Stitch;
import com.woodcomputing.bobbin.model.StitchGroup;
import com.woodcomputing.bobbin.model.jef.JEFColor;
import com.woodcomputing.bobbin.model.pes.PESColor;
import java.io.File;
import java.io.FileInputStream;
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
 * @author Jonathan Wood
 * 
 */
@Log4j2
public class PESFormat extends BobbinFormat {
    
    private static List<PESColor> pesColors;
    private static final Map<Integer, PESColor> pesColorMap = new HashMap<>();
    static {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = JEFFormat.class.getResourceAsStream("pes_colors.json")) {
            pesColors = mapper.readValue(is, mapper.getTypeFactory().constructCollectionType(List.class, PESColor.class));
        } catch (IOException ex) {
            log.catching(ex);
        }
        int index = 0;
        for (PESColor c : pesColors) {
            pesColorMap.put(index++, c);
        }
    }
    
    @Override
    public Design load(File file) {

        byte[] bytes = null;
        try (InputStream is = new FileInputStream(file)) {
            bytes = IOUtils.toByteArray(is);
        } catch (IOException ex) {
            log.catching(ex);
        }
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        Design design = new Design();   
        log.debug("Magic: {}{}{}{}", (char)bb.get(), (char)bb.get(), (char)bb.get(), (char)bb.get());
        int pecStart = bb.getInt(8);
        log.debug("PEC Start: {}", pecStart);
        byte colorCount = bb.get(pecStart + 48);
        log.debug("Color Count: {}", colorCount);
        int colors[] = new int[colorCount];
        for(int i = 0;i<colorCount;i++) {
            colors[i] = bb.get() & 0xFF;
            log.debug("Color[{}] = {}", i, colors[i]);
        }
        
        bb.position(pecStart + 532);
        int x;
        int y;
        int colorChanges = 0;
        PESColor color = pesColorMap.get(colors[colorChanges++]);
        StitchGroup stitchGroup = new StitchGroup();
        stitchGroup.setColor(color.getColor());

        while(true) {
            x = bb.get() & 0xFF;
            y = bb.get() & 0xFF;
            if(x == 0xFF && y == 0x00) {
                log.debug("End of stitches");
                break;
            } 
            if(x == 0xFE && y == 0xB0) {
                int colorIndex = bb.get() & 0xFF;
                log.debug("Color change: {}", colorIndex);
                color = pesColorMap.get(colors[colorChanges++]);
                stitchGroup = new StitchGroup();
                stitchGroup.setColor(color.getColor());
                continue;
            }
            if((x & 0x80) > 0) {
                log.debug("Testing X: {} -  X & 0x80: {}", x, x & 0x80 );
                if((x & 0x20) > 0) {
                    log.debug("Stich type TRIM");
                }
                if ((x & 0x10) > 0) {
                    log.debug("Stich type JUMP");
                }
                x = ((x & 0x0F) << 8) + y;
             
                if ((x & 0x800) > 0) {
                    x -= 0x1000;
                }
                y = bb.get() & 0xFF;

            } else if (x >= 0x40) {
                x -= 0x80;
            }
            if((y & 0x80) > 0) {
                log.debug("Testing Y: {} -  Y & 0x80: {}", y, y & 0x80 );
                if((y & 0x20) > 0) {
                    log.debug("Stich type TRIM");
                }
                if ((y & 0x10) > 0) {
                    log.debug("Stich type JUMP");
                }
                y = ((y & 0x0F) << 8) + bb.get() & 0xFF;
             
                if ((y & 0x800) > 0) {
                    y -= 0x1000;
                }
            } else if (y >= 0x40) {
                y -= 0x80;
            }
//            Stitch designStitch = new Stitch(cx, -cy, nx, -ny);
//            stitchGroup.getStitches().add(designStitch);
            log.debug("X: {} Y: {}", x, y);
        }
        log.debug("Color Changes: {}", colorChanges);
        return design;
    }

    @Override
    public void save(Design design) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}

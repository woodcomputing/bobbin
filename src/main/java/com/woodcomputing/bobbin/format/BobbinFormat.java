/*
 * Bobbin
 * Copyright 2016 Jonathan Wood
 * Licensed under the Apache License, Version 2.0
 */
package com.woodcomputing.bobbin.format;

import com.woodcomputing.bobbin.model.Design;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.log4j.Log4j2;

/**
 *
 * @author Jonathan Wood
 * 
 */
@Log4j2
public abstract class BobbinFormat {
    
    private  static final Map<String, Class<? extends BobbinFormat>> FormatMap = new HashMap<>();
    static {
        FormatMap.put("jef", JEFFormat.class);
        FormatMap.put("pes", PESFormat.class);
    }
    
    public abstract Design load(File file);
    public abstract void save(Design design);
    
    public static Design loadDesign(File file) {
        String filename = file.getName();
        String extension = filename.substring(filename.lastIndexOf(".") + 1);
        log.debug("Loading {} ({})", filename, extension);
        Class<? extends BobbinFormat> clazz = FormatMap.get(extension.toLowerCase());
        Design design = null;
        if(clazz != null) {
            try {
                design = clazz.newInstance().load(file);
            } catch (InstantiationException | IllegalAccessException ex) {
                log.catching(ex);
           }
        }
        return design;
    }
    
}

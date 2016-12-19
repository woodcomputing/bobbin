/*
 * Bobbin
 * Copyright 2016 Jonathan Wood
 * Licensed under the Apache License, Version 2.0
 */
package com.woodcomputing.bobbin.model.jef;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Jonathan Wood
 * 
 */
@Getter
@Setter
public class JEF {
    private int firstStitchLocation;
    private Hoop hoop;
    private int threadChangeCount;
    private int stitchCount;
    JEFColor[] threadColors;
}

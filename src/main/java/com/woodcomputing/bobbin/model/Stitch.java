/*
 * FXSlang
 * Copyright 2016 Jonathan Wood
 * Licensed under the Apache License, Version 2.0
 */
package com.woodcomputing.bobbin.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Jonathan Wood
 * 
 */
@Getter
@Setter
@AllArgsConstructor 
public class Stitch {
    private int startX;
    private int starty;
    private int endX;
    private int endY;
}

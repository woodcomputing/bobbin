/*
 * FXSlang
 * Copyright 2016 Jonathan Wood
 * Licensed under the Apache License, Version 2.0
 */
package com.woodcomputing.bobbin.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author Jonathan Wood
 * 
 */
@Getter
@Setter
@NoArgsConstructor
public class StitchGroup {
    private String color;
    private List<Stitch> stitches = new ArrayList<>();
}

/*
 * FXSlang
 * Copyright 2016 Jonathan Wood
 * Licensed under the Apache License, Version 2.0
 */
package com.woodcomputing.bobbin.model;

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
public class Stitches {
    private String name;
    private List<ThreadChange> threadChanges;
}

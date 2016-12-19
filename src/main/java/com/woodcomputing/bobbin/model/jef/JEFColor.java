/*
 * Bobbin
 * Copyright 2016 Jonathan Wood
 * Licensed under the Apache License, Version 2.0
 */
package com.woodcomputing.bobbin.model.jef;

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
public class JEFColor {
    private int code;
    private String rgb;
    private String number;
    private String name;
}

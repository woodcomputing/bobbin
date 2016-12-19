/*
 * Bobbin
 * Copyright 2016 Jonathan Wood
 * Licensed under the Apache License, Version 2.0
 */
package com.woodcomputing.bobbin.model.jef;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *
 * @author Jonathan Wood
 * 
 */
@Getter
@AllArgsConstructor
public enum Hoop {
    A(0, "A", 126, 110), 
    B(2, "B", 140, 200), 
    C(1, "C", 50, 50), 
    F(3, "F", 126, 110), 
    D(4, "D", 230, 200),
    UNKNOWN(-1,"UNKNOWN",-1,-1);
    
    private final int id;
    private final String name;
    private final int width;
    private final int height;
    
    public static Hoop id2Hoop(int id) {
        Hoop hoop;
        switch(id) {
            case 0:
                hoop = A;
                break;
            case 1:
                hoop = C;
                break;
            case 2:
                hoop = B;
                break;
            case 3:
                hoop = F;
                break;
            case 4:
                hoop = D;
                break;
            default:
                hoop = UNKNOWN;
        }
        return hoop;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.woodcomputing.bobbin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author jwood
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
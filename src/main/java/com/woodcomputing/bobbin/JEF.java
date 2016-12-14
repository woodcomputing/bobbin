/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.woodcomputing.bobbin;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author jwood
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

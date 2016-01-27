/* 
 * Copyright (c) 2015, S.F. Express Inc. All rights reserved.
 */

package com.ivy.freport.layout;

/**
 * 描述：
 * 
 * <pre>HISTORY
 * ****************************************************************************
 *  ID   DATE           PERSON          REASON
 *  1    2015年4月15日      Steven.Zhu         Create
 * ****************************************************************************
 * </pre>
 * @author Steven.Zhu
 * @since 
 */

public enum Align {
    
    LEFT(0),
    CENTER(1),
    RIGHT(2),
    TOP(4),
    MIDDLE(5),
    BOTTOM(6);

    private int value;
    
    private Align(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
    
    public static int getValue(String desc) {
        return Align.valueOf(desc.toUpperCase()).value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}

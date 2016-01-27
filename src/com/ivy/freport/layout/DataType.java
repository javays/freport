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
 *  1    2015年6月8日      Steven.Zhu         Create
 * ****************************************************************************
 * </pre>
 * @author Steven.Zhu
 * @since 
 */

public enum DataType {

    NUMBER("number"),
    DATE("date");
    
    private String value;
    
    private DataType(String value) {
        this.value = value;
    }
    
    public static DataType parse(String dataType) {
        if (dataType == null || "".equals(dataType.trim())) {
            return null;
        }
        return DataType.valueOf(dataType.toUpperCase());
    }

    public String getValue() {
        return value;
    }
}

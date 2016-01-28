/* 
 * Copyright (c) 2016, S.F. Express Inc. All rights reserved.
 */

package com.ivy.freport.layout;

/**
 * 描述：
 * 
 * <pre>HISTORY
 * ****************************************************************************
 *  ID   DATE           PERSON          REASON
 *  1    2016年1月28日      Steven.Zhu         Create
 * ****************************************************************************
 * </pre>
 * @author Steven.Zhu
 * @since 
 */

public class IvyParam {

    private String name;
    private String fullName;
    private DataType dataType;
    private String pattern;
    
    public IvyParam() {
        super();
    }
    
    /**
     * @param name
     * @param fullName
     */
    public IvyParam(String name, String fullName) {
        super();
        this.name = name;
        this.fullName = fullName;
    }

    /**
     * @param name
     * @param fullName
     * @param dataType
     * @param pattern
     */
    public IvyParam(String name, String fullName, DataType dataType,
            String pattern) {
        super();
        this.name = name;
        this.fullName = fullName;
        this.dataType = dataType;
        this.pattern = pattern;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public DataType getDataType() {
        return dataType;
    }
    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }
    public String getPattern() {
        return pattern;
    }
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((dataType == null) ? 0 : dataType.hashCode());
        result = prime * result
                + ((fullName == null) ? 0 : fullName.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((pattern == null) ? 0 : pattern.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        IvyParam other = (IvyParam) obj;
        if (dataType != other.dataType)
            return false;
        if (fullName == null) {
            if (other.fullName != null)
                return false;
        } else if (!fullName.equals(other.fullName))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (pattern == null) {
            if (other.pattern != null)
                return false;
        } else if (!pattern.equals(other.pattern))
            return false;
        return true;
    }
}

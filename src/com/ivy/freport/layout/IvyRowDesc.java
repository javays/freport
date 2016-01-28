package com.ivy.freport.layout;

import java.util.List;

/**
 * Table布局 描述：
 * 
 * <pre>
 * HISTORY
 * ****************************************************************************
 *  ID   DATE           PERSON          REASON
 *  1    2015年5月15日      Steven.Zhu         Create
 * ****************************************************************************
 * </pre>
 * 
 * @author Steven.Zhu
 * @since
 */
public class IvyRowDesc {
    
    public static final String HEAD = "head";

    private String name;
    private short height = 18;
    private int maxline;
    private String ds;
    private List<IvyCellDesc> ivyCellDescs;
    private String show;
    
    public String getName() {
        return name;
    }
    public String getDs() {
        return ds;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setDs(String ds) {
        this.ds = ds;
    }
    public List<IvyCellDesc> getIvyCellDescs() {
        return ivyCellDescs;
    }
    public void setIvyCellDescs(List<IvyCellDesc> ivyCellDescs) {
        this.ivyCellDescs = ivyCellDescs;
    }
    public int getMaxline() {
        return maxline;
    }
    public void setMaxline(int maxline) {
        this.maxline = maxline;
    }
    public short getHeight() {
        return height;
    }
    public void setHeight(short height) {
        this.height = height;
    }
    public String getShow() {
        return show;
    }
    public void setShow(String show) {
        this.show = show;
    }
    
    @Override
    public String toString() {
        return "IvyRowDesc [name=" + name
                + ", height="
                + height
                + ", maxline="
                + maxline
                + ", ds="
                + ds
                + ", cells="
                + ivyCellDescs
                + ", show="
                + show
                + "]";
    }
}
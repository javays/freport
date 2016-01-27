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
public class Row {
    
    public static final String HEAD = "head";

    private String name;
    private short height = 18;
    private int maxline;
    private String ds;
    private List<Cell> cells;
    private String show;
    
    public String getName() {
        return name;
    }
    public String getDs() {
        return ds;
    }
    public List<Cell> getCells() {
        return cells;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setDs(String ds) {
        this.ds = ds;
    }
    public void setCells(List<Cell> cells) {
        this.cells = cells;
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
        return "BillRow [name=" + name
                + ", height="
                + height
                + ", maxline="
                + maxline
                + ", ds="
                + ds
                + ", cells="
                + cells
                + ", show="
                + show
                + "]";
    }
}
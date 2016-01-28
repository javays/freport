package com.ivy.freport.layout;

import java.util.ArrayList;
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
public class IvyTableDesc {

    private String id;
    private int width = 100;
    private int align = Align.LEFT.getValue(); // 默认左对齐
    private List<IvyRowDesc> ivyRowDescs;
    private boolean br;
    private String show;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getAlign() {
        return align;
    }

    public void setAlign(int align) {
        this.align = align;
    }

    public List<IvyRowDesc> getIvyRowDescs() {
        return ivyRowDescs;
    }

    public void setIvyRowDescs(List<IvyRowDesc> ivyRowDescs) {
        this.ivyRowDescs = ivyRowDescs;
    }

    public void addRowDesc(IvyRowDesc ivyRowDesc) {
        if (ivyRowDescs == null) {
            ivyRowDescs = new ArrayList<IvyRowDesc>();
        }
        ivyRowDescs.add(ivyRowDesc);
    }

    public boolean isBr() {
        return br;
    }

    public void setBr(boolean br) {
        this.br = br;
    }

    public String getShow() {
        return show;
    }

    public void setShow(String show) {
        this.show = show;
    }
}
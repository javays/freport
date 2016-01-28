/* 
 * Copyright (c) 2016, S.F. Express Inc. All rights reserved.
 */

package com.ivy.freport.layout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

public class IvyDocDesc {

    private String name;
    private List<IvyTableDesc> ivyTableDescs;
    private Map<String, Object> attrs;
    
    public void addTableDesc(IvyTableDesc ivyTableDesc) {
        if (ivyTableDescs == null) {
            ivyTableDescs = new ArrayList<IvyTableDesc>();
        }
        ivyTableDescs.add(ivyTableDesc);
    }
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public List<IvyTableDesc> getIvyTableDescs() {
        return ivyTableDescs;
    }
    public void setIvyTableDescs(List<IvyTableDesc> ivyTableDescs) {
        this.ivyTableDescs = ivyTableDescs;
    }
    public Map<String, Object> getAttrs() {
        return attrs;
    }
    public void setAttrs(Map<String, Object> attrs) {
        this.attrs = attrs;
    }
}

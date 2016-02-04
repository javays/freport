/* 
 * Copyright (c) 2016, S.F. Express Inc. All rights reserved.
 */

package com.ivy.freport.writer.xls;

import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;

import com.ivy.freport.layout.IvyRowDesc;

/**
 * 描述：
 * 
 * <pre>HISTORY
 * ****************************************************************************
 *  ID   DATE           PERSON          REASON
 *  1    2016年1月29日      Steven.Zhu         Create
 * ****************************************************************************
 * </pre>
 * @author Steven.Zhu
 * @since 
 */

public class XlsRowDesc {

    private IvyRowDesc ivyRowDesc;
    private List<HSSFCellStyle> cellStyles;
    
    public XlsRowDesc() {
        super();
    }
    
    /**
     * @param ivyRowDesc
     * @param cellStyles
     */
    public XlsRowDesc(IvyRowDesc ivyRowDesc, List<HSSFCellStyle> cellStyles) {
        super();
        this.ivyRowDesc = ivyRowDesc;
        this.cellStyles = cellStyles;
    }
    
    public IvyRowDesc getIvyRowDesc() {
        return ivyRowDesc;
    }
    public void setIvyRowDesc(IvyRowDesc ivyRowDesc) {
        this.ivyRowDesc = ivyRowDesc;
    }
    public List<HSSFCellStyle> getCellStyles() {
        return cellStyles;
    }
    public void setCellStyles(List<HSSFCellStyle> cellStyles) {
        this.cellStyles = cellStyles;
    }
}

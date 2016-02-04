/* 
 * Copyright (c) 2016, S.F. Express Inc. All rights reserved.
 */

package com.ivy.freport.writer.xls;

import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.xml.sax.Attributes;

import com.ivy.freport.ds.IvyDataSource;
import com.ivy.freport.layout.DataType;
import com.ivy.freport.layout.IvyCellDesc;
import com.ivy.freport.layout.IvyDocDesc;

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

public class IvyXmlAttrDsXlsWriter extends IvyXlsWriter<Attributes> {

    
    /**
     * @param ivyDocDesc
     * @param dataSources
     * @param otherAttrs
     * @param output
     */
    public IvyXmlAttrDsXlsWriter(IvyDocDesc ivyDocDesc,
            Map<String, IvyDataSource<Attributes>> dataSources,
            Map<String, Object> otherAttrs, String output) {
        super(ivyDocDesc, dataSources, otherAttrs, output);
    }

    /* (non-Javadoc)
     * @see com.ivy.freport.writer.xls.IvyXlsWriter#nextElement(java.lang.Object)
     */
    @Override
    public boolean nextElement(Attributes attributes) {
        int height = getLoopRow().getIvyRowDesc().getHeight();
        List<IvyCellDesc> cellDescs = getLoopRow().getIvyRowDesc().getIvyCellDescs();
        List<HSSFCellStyle> hssfCellStyles = getLoopRow().getCellStyles();
        
        HSSFRow hssfRow = sheet.createRow(getRowId());
        hssfRow.setHeight((short)(height*20));
        
        System.out.println("rowId=" + rowId);
        System.out.println("curItemIndex=" + curItemIndex);
        
        for (int i = 0; i < cellDescs.size(); i++) {
            IvyCellDesc cellDesc = cellDescs.get(i);
            
            String columnValue = attributes.getValue(cellDesc.getValue());
            if (columnValue == null) columnValue = "";
            
            HSSFCell cell = hssfRow.createCell(cellDesc.getCellId());
            
            cell.setCellStyle(hssfCellStyles.get(i));
            
            if (cellDesc.getDataType() == DataType.NUMBER 
                    && !"".equals(columnValue.trim())) {
                cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                cell.setCellValue(Double.parseDouble(columnValue));
            } else {
                cell.setCellValue(columnValue.replaceAll("\\\\n", "\n"));
            }
            
            if (cellDesc.getColspan() > 0) {
                addMegeredRegion(rowId, 
                            cellDesc.getCellId(), 
                            0, 
                            cellDesc.getColspan(), 
                            cellDesc.getBorder());
            }
        }
        
        incItemIndex();
        
        checkLimit();
        
        return true;
    }

}

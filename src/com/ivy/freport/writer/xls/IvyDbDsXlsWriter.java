/* 
 * Copyright (c) 2016, S.F. Express Inc. All rights reserved.
 */

package com.ivy.freport.writer.xls;

import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;

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

public class IvyDbDsXlsWriter extends IvyXlsWriter<Map<String, Object>> {

    
    /**
     * @param ivyDocDesc
     * @param dataSources
     * @param otherAttrs
     * @param output
     */
    public IvyDbDsXlsWriter(IvyDocDesc ivyDocDesc,
            Map<String, IvyDataSource<Map<String, Object>>> dataSources,
            Map<String, Object> otherAttrs, String output) {
        super(ivyDocDesc, dataSources, otherAttrs, output);
    }

    /* (non-Javadoc)
     * @see com.ivy.freport.ds.IvyDSAccessListener#nextElement(java.lang.Object, int)
     */
    @Override
    public boolean nextElement(Map<String, Object> map, int seq) {
        int height = getLoopRow().getIvyRowDesc().getHeight();
        List<IvyCellDesc> cellDescs = getLoopRow().getIvyRowDesc().getIvyCellDescs();
        List<HSSFCellStyle> hssfCellStyles = getLoopRow().getCellStyles();
        
        HSSFRow hssfRow = sheet.createRow(getRowId());
        hssfRow.setHeight((short)(height*20));
        
        for (int i = 0; i < cellDescs.size(); i++) {
            IvyCellDesc cellDesc = cellDescs.get(i);
            
            String columnValue = null;
            
            String fieldName = cellDesc.getValue();
            if ("@no".equals(fieldName)) {
                columnValue = String.valueOf(seq);
            } else {
                Object obj_columnValue = map.get(cellDesc.getValue());
                if (obj_columnValue == null) obj_columnValue = "";
                columnValue = String.valueOf(obj_columnValue);
            }
            
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

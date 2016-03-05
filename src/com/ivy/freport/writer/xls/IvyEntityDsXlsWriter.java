/* 
 * Copyright (c) 2016, S.F. Express Inc. All rights reserved.
 */

package com.ivy.freport.writer.xls;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
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
 *  1    2016年3月5日      Steven.Zhu         Create
 * ****************************************************************************
 * </pre>
 * @author Steven.Zhu
 * @since 
 */

public class IvyEntityDsXlsWriter<T> extends IvyXlsWriter<T> {
    
    private static Logger logger = Logger.getLogger(IvyEntityDsXlsWriter.class);
    
    private Class<?> clazz;

    /**
     * @param ivyDocDesc
     * @param dataSources
     * @param otherAttrs
     * @param output
     */
    public IvyEntityDsXlsWriter(IvyDocDesc ivyDocDesc,
            Map<String, IvyDataSource<T>> dataSources,
            Map<String, Object> otherAttrs, String output) {
        super(ivyDocDesc, dataSources, otherAttrs, output);
    }

    /* (non-Javadoc)
     * @see com.ivy.freport.ds.IvyDSAccessListener#nextElement(java.lang.Object, int)
     */
    @Override
    public boolean nextElement(T entity, int seq) {
        if (clazz == null) {
            clazz = entity.getClass();
        }
        
        int height = getLoopRow().getIvyRowDesc().getHeight();
        List<IvyCellDesc> cellDescs = getLoopRow().getIvyRowDesc().getIvyCellDescs();
        List<HSSFCellStyle> hssfCellStyles = getLoopRow().getCellStyles();
        
        HSSFRow hssfRow = sheet.createRow(getRowId());
        hssfRow.setHeight((short)(height*20));
        
        try {
            for (int i = 0; i < cellDescs.size(); i++) {
                IvyCellDesc cellDesc = cellDescs.get(i);
                
                String columnValue = null;
                
                String fieldName = cellDesc.getValue();
                if ("@no".equals(fieldName)) {
                    columnValue = String.valueOf(seq);
                } else {
                    Field field = clazz.getField(cellDesc.getValue());
                    field.setAccessible(true);
                    Object obj_columnValue = field.get(entity);
                    
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
        } catch (NoSuchFieldException e) {
            logger.error(e);
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            logger.error(e);
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            logger.error(e);
            e.printStackTrace();
        }
        
        return false;
    }
}

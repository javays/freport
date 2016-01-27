/* 
 * Copyright (c) 2016, S.F. Express Inc. All rights reserved.
 */

package com.ivy.freport.writer.pdf;

import java.math.BigDecimal;
import java.util.function.Consumer;

import org.apache.tools.ant.util.FileUtils;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.ivy.freport.layout.Cell;
import com.ivy.freport.layout.Row;
import com.lowagie.text.pdf.PdfTable;

/**
 * 描述：
 * 
 * <pre>HISTORY
 * ****************************************************************************
 *  ID   DATE           PERSON          REASON
 *  1    2016年1月27日      Steven.Zhu         Create
 * ****************************************************************************
 * </pre>
 * @author Steven.Zhu
 * @since 
 */

public class IvyXmlAttrPdfConsumer implements Consumer<Map<String, Object>> {
    
    private PdfPTable curTable;
    private Row row;
    
    public IvyXmlAttrPdfConsumer(PdfPTable curTable, Row row) {
        this.curTable = curTable;
        this.row = row;
    }
    
    /* (non-Javadoc)
     * @see java.util.function.Consumer#accept(java.lang.Object)
     */
    @Override
    public void accept(Map<String, Object> line) {
        for (Cell cell : row.getCells()) {
            String columnName = cell.getValue();
            Object columnValue = line.get(columnName);
            
            if (EnumDataType.NUMBER == billCell.getDataType() 
                    && FileUtils.isNum(value)) {
                int scale = 0;
                String pattern = billCell.getPattern();
                int lastDot = pattern.lastIndexOf(".");
                if (lastDot != -1) {
                    scale = pattern.length() - lastDot - 1;
                }
                
                BigDecimal bigDecimal = new BigDecimal(value);
                value = bigDecimal.setScale(scale, BigDecimal.ROUND_HALF_UP).toString();
            }
            
            PdfParagraph paragraph = new PdfParagraph(value, billCell.getSize(), billCell.isBold());
            
            PdfPCell cell = new PdfPCell(paragraph);
            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
            cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
            cell.setBorderColor(BaseColor.BLACK);
            
            curTable.addCell(cell);
        }
        
        try {
            count ++;
            if (count % MAX_FLUSH_ROW_NUM == 0) {
                document.add(curTable);
                logger.info("flush pdf data to disk");
                pdfWriter.flush();
            }
        } catch (DocumentException e) {
            logger.error("", e);
            e.printStackTrace();
        }
    }
}

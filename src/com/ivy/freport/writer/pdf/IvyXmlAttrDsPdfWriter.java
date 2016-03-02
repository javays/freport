/* 
 * Copyright (c) 2016, S.F. Express Inc. All rights reserved.
 */

package com.ivy.freport.writer.pdf;

import java.text.DecimalFormat;
import java.util.Map;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfPCell;
import com.ivy.freport.ds.IvyDataSource;
import com.ivy.freport.layout.DataType;
import com.ivy.freport.layout.IvyCellDesc;
import com.ivy.freport.layout.IvyDocDesc;
import com.ivy.freport.utils.StringUtils;

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

public class IvyXmlAttrDsPdfWriter extends IvyPdfWriter<Attributes> {
    
    private static final Logger logger = Logger.getLogger(IvyXmlAttrDsPdfWriter.class);
    
    private int count = 0;

    /**
     * @param ivyDocDesc
     * @param dataSources
     * @param otherAttrs
     * @param output
     */
    public IvyXmlAttrDsPdfWriter(IvyDocDesc ivyDocDesc,
            Map<String, IvyDataSource<Attributes>> dataSources,
            Map<String, Object> otherAttrs, String output) {
        super(ivyDocDesc, dataSources, otherAttrs, output);
    }

    /* (non-Javadoc)
     * @see com.ivy.freport.writer.xls.IvyDSAccessListener#nextElement(java.lang.Object)
     */
    @Override
    public boolean nextElement(Attributes attributes, int seq) {
        for (IvyCellDesc ivyCellDesc : ivyRowDesc.getIvyCellDescs()) {
            String cellName = ivyCellDesc.getValue();
            DataType cellType = ivyCellDesc.getDataType();
            
            String cellValue = null;
            if ("@no".equals(cellName)) {
                cellValue = String.valueOf(seq);
            } else {
                cellValue = attributes.getValue(cellName);
                if (cellValue == null) cellValue = "";
            }
            
            if (DataType.NUMBER == cellType 
                    && !StringUtils.isEmpty(cellValue) 
                    && cellValue.matches("\\d+(\\.\\d+)?")) {
                double d = Double.parseDouble(cellValue);
                DecimalFormat df = new DecimalFormat(ivyCellDesc.getPattern());
                cellValue = df.format(d);
            }
            
            PdfParagraph paragraph = new PdfParagraph(cellValue, ivyCellDesc.getSize(), ivyCellDesc.isBold());
            
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
        
        return true;
    }
}

/* 
 * Copyright (c) 2015, S.F. Express Inc. All rights reserved.
 */

package com.ivy.freport.writer.xls;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.dom4j.Element;

import com.sf.ecbil.bill.support.BillCreateHelper;
import com.sf.ecbil.bill.support.XlsTmplParser;
import com.sf.ecbil.bill.util.FileUtils;
import com.sf.ecbil.bill.util.StringUtils;
import com.sf.ecbil.bill.vo.BillSubTotal;

/**
 * 描述：
 * 
 * <pre>HISTORY
 * ****************************************************************************
 *  ID   DATE           PERSON          REASON
 *  1    2015年4月22日      Steven.Zhu         Create
 * ****************************************************************************
 * </pre>
 * @author Steven.Zhu
 * @since 
 */

public class XlsBillStoCreator extends BillCreateHelper {
    
    public static final String NUM_PATTERN = "0.00";
    
    private static Logger logger = Logger.getLogger(XlsBillStoCreator.class);
    
    private BillParams billParams;
    
    private HSSFCellStyle numCellStyle;
    
    public XlsBillStoCreator(BillParams billParams) {
        super();
        this.billParams = billParams;
    }
    
    /**
     * 仓储账单创建入口
     * @param tmpl
     * @param xmlFile
     * @param params
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public boolean create() {
        boolean validate = validateParams(billParams);
        if (!validate) {
            return false;
        }
        
        List<String> xmlFiles = (List<String>) billParams.get(BillParams.PARAM_DS_ORDER);
        if(xmlFiles == null || xmlFiles.isEmpty()) {
            return true;
        }
        
        Map<String, Object> ds = parseStoXml(xmlFiles);
        if (ds.isEmpty()) {
            return true;
        }
        
        InputStream inputStream = null;
        FileOutputStream fileOut = null;
        try {
            double totalFee = (double) ds.get(TOTAL_FEE_KEY);
            billParams.put(TOTAL_FEE_KEY, totalFee);
            
            BillSubTotal billSubTotal = (BillSubTotal) billParams.get(BillParams.PARAM_BILL_SUBTOTAL);
            if (billSubTotal != null) {
                billSubTotal.setSubTotalDueAmt(totalFee - billSubTotal.getSpecialRebate());
                billSubTotal.setTotalDueAmt(billSubTotal.getSubTotalDueAmt() + billSubTotal.getUnpaidFee());
            }
            
            String tmplName = (String) billParams.get(BillParams.PARAM_TMPL);
            inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(tmplName);
            HSSFWorkbook hssfWorkbook = new HSSFWorkbook(inputStream);
            numCellStyle = hssfWorkbook.createCellStyle();
            
            HSSFSheet sheet = hssfWorkbook.getSheetAt(0);
            
            HSSFCellStyle style = null;
            for (int rowIndex = 0; rowIndex < sheet.getLastRowNum(); rowIndex++) {
                HSSFRow row = sheet.getRow(rowIndex);
                if (row == null) {
                    logger.debug("rowId="+ rowIndex +" is null");
                    continue;
                }
                
                boolean start = false;
                for (int cellIndex = 0; cellIndex < row.getLastCellNum(); cellIndex++) {
                    HSSFCell cell = row.getCell(cellIndex);
                    if (cell == null) {
                        logger.debug("rowId=" + rowIndex + ", cellId=" + cellIndex + " is null");
                        continue;
                    }
                    
                    style = hssfWorkbook.createCellStyle();  
                    style.cloneStyleFrom(cell.getCellStyle());
                    cell.setCellStyle(style);
                    
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_BLANK:
                            cell.setCellValue(cell.getStringCellValue());
                            break;
                        case Cell.CELL_TYPE_BOOLEAN:
                            cell.setCellValue(cell.getBooleanCellValue());
                            break;
                        case Cell.CELL_TYPE_ERROR:
                            cell.setCellErrorValue(cell.getErrorCellValue());
                            break;
                        case Cell.CELL_TYPE_FORMULA:
                            cell.setCellFormula(cell.getCellFormula());
                            break;
                        case Cell.CELL_TYPE_NUMERIC:
                            cell.setCellValue(cell.getNumericCellValue());
                            break;
                        case Cell.CELL_TYPE_STRING:
                            String c_value = cell.getStringCellValue();
                            if (XlsTmplParser.isShowRowCmd(c_value)) {      //行显示命令
                                boolean showSingleRow = XlsTmplParser.isShow(c_value, billParams);
                                if (!showSingleRow) {
                                    XlsUtils.delRow(sheet, rowIndex, rowIndex+1);
                                    rowIndex --;     //回退一行 
                                }else {
                                    String cmd = StringUtils.find(c_value, XlsTmplParser.CODE_TAG_SHOW_ROW_GROUP_REGEX);
                                    String cellValue = c_value.substring(cmd.length());
                                    
                                    Object o_cellValue = XlsTmplParser.fillParams(cellValue, billParams);
                                    if (o_cellValue instanceof Double) {
                                        numCellStyle.cloneStyleFrom(style);
                                        numCellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat(NUM_PATTERN));
                                        cell.setCellStyle(numCellStyle);
//                                        cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                                        cell.setCellValue((Double)o_cellValue);
                                    } else {
                                        cell.setCellValue((String)o_cellValue);
                                    }
                                }
                                break;
                            }else if ("[start]".equals(c_value.trim())) {
                                start = true;
                                cellIndex = row.getLastCellNum();  //exit while 
                            } else {
                                Object o_cellValue = XlsTmplParser.fillParams(c_value, billParams);
                                
                                if (o_cellValue instanceof Double) {
                                    numCellStyle.cloneStyleFrom(style);
                                    numCellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat(NUM_PATTERN));
                                    cell.setCellStyle(numCellStyle);
//                                    cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                                    cell.setCellValue((Double)o_cellValue);
                                } else {
                                    cell.setCellValue((String)o_cellValue);
                                }
                            }
                    }
                }
                
                if (start) {
                    rowIndex = fillFeeList(sheet, rowIndex, row.getHeight(), ds);
                } 
            }
                
            String fileName = (String)billParams.get(BillParams.PARAM_OUTPUT);
            fileOut = new FileOutputStream(new File(fileName));
            hssfWorkbook.write(fileOut);
            
            return true;
        } catch (IOException e) {
            logger.error("", e);
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(fileOut);
            IOUtils.closeQuietly(inputStream);
        }
        
        return false;
    }
    
    /**
     * 填充费用列表部分
     * @param curSheet
     * @param curRow
     * @param height
     * @param ds
     * @param params
     * @return
     */
    @SuppressWarnings({"unchecked"})
    public int fillFeeList(HSSFSheet curSheet, int curRow, short height, Map<String, Object> ds) {
        if (ds == null || ds.isEmpty()) {
            return curRow;
        }
        
        HSSFCellStyle cellStyle = curSheet.getWorkbook().createCellStyle();
        cellStyle.setBorderLeft((short)1);
        cellStyle.setBorderRight((short)1);
        cellStyle.setBorderBottom((short)1);
        cellStyle.setBorderTop((short)1);
        cellStyle.setWrapText(true);    
        cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        
        int feeLevelNo = 1;
        for (int i = 0; i < STO_FIRST_LEVEL_FEE.length; i++) {
            String first_level_fee = STO_FIRST_LEVEL_FEE[i];
            String[] second_level_fee = STO_SECOND_LEVEL_FEE[i];
            String[] fee_codes = STO_FEE_CODE[i];
            
            boolean delFirstLevelRow = true;
            
            HSSFRow newRow = curSheet.createRow(curRow);
            XlsUtils.insertRowBefore(curSheet, curRow, 1);
            newRow.setHeight(height);
            
            if (second_level_fee.length > 0) {
                fillFeeRow(newRow, cellStyle, true, 
                            feeLevelNo + "." + first_level_fee, 
                            "——", "——", "——", "", "");
                curRow ++;
            }else {
                String feeCode = fee_codes[0];    //保管费
                Object value = ds.get(feeCode);
                if (value != null) {
                    Element feeNode = (Element) value;
                    fillFeeRow(newRow, cellStyle, true, 
                            feeLevelNo + "." + first_level_fee, feeNode);
                    delFirstLevelRow = false;
                }
                curRow ++;
            }
            
            for (int j = 0; j < second_level_fee.length; j++) {
                String fee_name = second_level_fee[j];
                String fee_code = fee_codes[j];
                
                Object value = ds.get(fee_code);
                if (value == null) {
                    continue;
                }
                
                if (delFirstLevelRow) {    //存在按件计费用，不需要删除一级费用项行
                    delFirstLevelRow = false;
                }
                
                newRow = curSheet.createRow(curRow);
                XlsUtils.insertRowBefore(curSheet, curRow, 1);
                newRow.setHeight(height);
                if ("CC03100".equals(fee_code)) {     //按件计
                    fillFeeRow(newRow, cellStyle, false, TAB + fee_name, "——", "——", "——", "", "");
                    curRow ++;
                    
                    Collection<Element> feeNodes = ((Map<String, Element>) value).values();
                    for (Element element : feeNodes) {
                        newRow = curSheet.createRow(curRow);
                        XlsUtils.insertRowBefore(curSheet, curRow, 1);
                        newRow.setHeight(height);
                        curRow ++;
                        
                        String feeName = TAB + TAB + element.attributeValue("category");
                        fillFeeRow(newRow, cellStyle, false, feeName, element);
                    }
                } else {
                    if (value instanceof List) {
                        List<Element> result = (List<Element>)value;
                        for (int k=0; k<result.size(); k++) {
                            Element element = result.get(k);
                            if (k > 0) {
                                newRow = curSheet.createRow(curRow);
                                XlsUtils.insertRowBefore(curSheet, curRow, 1);
                                newRow.setHeight(height);
                            }
                            
                            fillFeeRow(newRow, cellStyle, false, TAB + fee_name, element);
                            curRow ++;
                        }
                    } else {
                        fillFeeRow(newRow, cellStyle, false, TAB + fee_name, (Element) value);
                        curRow ++;
                    }
                }
            }
            
            if (delFirstLevelRow) {    //删除一级目录
                XlsUtils.delRow(curSheet, curRow, curRow+1);
                curRow --;
            }else {
                feeLevelNo ++;
            }
        }
        
        XlsUtils.delRow(curSheet, curRow, curRow+1);
        return curRow-1;
    }
    
    /**
     * 创建一费用项行
     * @param newRow
     * @param cellStyle
     * @param category
     * @param unit
     * @param unitPrice
     * @param quantity
     * @param feeAmt
     * @param remark
     */
    private void fillFeeRow(HSSFRow newRow, 
                        HSSFCellStyle cellStyle,
                        boolean firstLevel,
                        String category,
                        String unit,
                        String unitPrice,
                        String quantity,
                        String feeAmt,
                        String remark) {
        
        HSSFWorkbook workbook = newRow.getSheet().getWorkbook();
        HSSFCellStyle firstRowCellStyle = workbook.createCellStyle();
        firstRowCellStyle.cloneStyleFrom(cellStyle);
        firstRowCellStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
        if (firstLevel) {
            HSSFFont font = workbook.createFont();
            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);//粗体显示
            font.setFontHeightInPoints((short) 11);
            firstRowCellStyle.setFont(font);
        }
        HSSFCell cell1 = newRow.createCell(0);
        cell1.setCellStyle(firstRowCellStyle);
        cell1.setCellValue(category);
        
        HSSFCell cell2 = newRow.createCell(1);
        cell2.setCellStyle(cellStyle);
        cell2.setCellValue(unit);
        
        numCellStyle.cloneStyleFrom(cellStyle);
        numCellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat(NUM_PATTERN));
        
        HSSFCell cell3 = newRow.createCell(2);
        if (FileUtils.isNum(unitPrice)) {
            cell3.setCellStyle(numCellStyle);
            cell3.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
            cell3.setCellValue(Double.parseDouble(unitPrice));
        }else {
            cell3.setCellStyle(cellStyle);
            cell3.setCellValue(new HSSFRichTextString(unitPrice));
        }
        
        HSSFCell cell4 = newRow.createCell(3);
        if (FileUtils.isNum(unitPrice)) {
            cell4.setCellStyle(numCellStyle);
            cell4.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
            cell4.setCellValue(Double.parseDouble(quantity));
        }else {
            cell4.setCellStyle(cellStyle);
            cell4.setCellValue(new HSSFRichTextString(quantity));
        }
        
        HSSFCell cell5 = newRow.createCell(4);
        if (FileUtils.isNum(unitPrice)) {
            cell5.setCellStyle(numCellStyle);
            cell5.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
            cell5.setCellValue(Double.parseDouble(feeAmt));
        }else {
            cell5.setCellStyle(cellStyle);
            cell5.setCellValue(new HSSFRichTextString(feeAmt));
        }
        
        HSSFCell cell6 = newRow.createCell(5);
        cell6.setCellStyle(cellStyle);
        cell6.setCellValue(remark);
    }
    
    /**
     * 创建一费用项行
     * @param newRow
     * @param cellStyle
     * @param element
     */
    private void fillFeeRow(HSSFRow newRow, 
                        HSSFCellStyle cellStyle,
                        boolean firstLevel,
                        String feeName,
                        Element element) {
        String unit = element.attributeValue("unit");
        String unitPrice = element.attributeValue("unitPrice");
        String quantity = element.attributeValue("quantity");
        String feeAmt = element.attributeValue("feeAmt");
        String remark = element.attributeValue("remark");
        
        fillFeeRow(newRow, cellStyle, firstLevel, feeName, unit, unitPrice, quantity, feeAmt, remark);
    }
}

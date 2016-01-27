/* 
 * Copyright (c) 2015, S.F. Express Inc. All rights reserved.
 */

package com.ivy.freport.writer.xls;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFPictureData;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFShape;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import com.sf.ecbil.bill.util.ExcelTmplParser;
import com.sf.ecbil.bill.util.StringUtils;
import com.sf.ecbil.bill.util.XmlDataSourceParser;
import com.sf.ecbil.bill.vo.BillBottom;
import com.sf.ecbil.bill.vo.BillHead;
import com.sf.ecbil.bill.vo.BillSubTotal;
import com.sf.ecbil.bill.vo.RowWrapper;

/**
 * 描述：
 * 
 * <pre>HISTORY
 * ****************************************************************************
 *  ID   DATE           PERSON          REASON
 *  1    2015年3月13日      Steven.Zhu         Create
 * ****************************************************************************
 * </pre>
 * @author Steven.Zhu
 * @since 
 */

public class ExcelBillGenerator {

    public static final Logger logger = Logger.getLogger(ExcelBillGenerator.class);
    
    public static final String PARAM_OUTPUT_FILE = "output_file";
    private static ExcelBillGenerator excelBillGenerator;
    
    private String tmplName;
    
    private ExcelBillGenerator(String tmplName) {
        super();
        this.tmplName = tmplName;
    }
    
    public static ExcelBillGenerator getInstance(String tmplName) {
        if (excelBillGenerator == null) {
            excelBillGenerator = new ExcelBillGenerator(tmplName);
        }
        return excelBillGenerator;
    }
    
    /**
     * 删除指定行跨行跨列单元格
     * @param sheet
     * @param rowIndex
     */
    public void removeMergeRegions(HSSFSheet sheet, int rowIndex) {
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress cellRangeAddress = sheet.getMergedRegion(i);
            int firstRow = cellRangeAddress.getFirstRow();
            if (firstRow == rowIndex) {
                sheet.removeMergedRegion(i);
            }
        }
    }

    /**
     * 插入行
     * @param sheet
     * @param startRow
     * @param forward
     */
    public void insertRowBefore(HSSFSheet sheet,
            int insertPoint,
            int insertRowNum) {
        
        sheet.shiftRows(insertPoint, sheet.getLastRowNum(), insertRowNum);
        for (int i = insertPoint; i < sheet.getLastRowNum(); i++) {
            HSSFRow everRow = sheet.getRow(i);
            if (everRow != null) {
                HSSFRow row = sheet.getRow(i + insertRowNum);
                if (row != null) {
                    short height = everRow.getHeight();
                    row.setHeight(height);
                }
            }
        }
        
        /*if(insertRowNum > (beforeRowCount - insertPoint)) {   //插入行数大于需要下移的行数
            
        } */
        
        /*int beforeRowCount = sheet.getLastRowNum();
        beforeRowCount = beforeRowCount > (insertRowNum+insertPoint) ? (insertRowNum+insertPoint) : beforeRowCount;
        for (int i = insertPoint; i < beforeRowCount; i++) {
            //remove megered regions   
            removeMergeRegions(sheet, i);
        }*/
    }
    
    /**
     * 删除行（delete）
     * @param sheet
     * @param startRow  开始行
     * @param endRow  结束行，不包括
     */
    public void delRow(HSSFSheet sheet, int startRow, int endRow) {
        for (int i = startRow; i < endRow; i++) {
            removeMergeRegions(sheet, i);
        }
        
        int forward = endRow-startRow;
        sheet.shiftRows(endRow, sheet.getLastRowNum(), -forward);
        for (int i=endRow; i<sheet.getLastRowNum(); i++) {
            HSSFRow everRow = sheet.getRow(i);
            if(everRow != null) {
                HSSFRow row = sheet.getRow(i-forward);
                if (row != null) {
                    short height = everRow.getHeight();
                    row.setHeight(height);
                }
            }
        }
    }
    
    /**
     * 移动文档中图片位置
     * @param hssfWorkbook
     * @param sheetId
     */
    public void movePic(HSSFWorkbook hssfWorkbook, int sheetId) {
        HSSFSheet sheet = hssfWorkbook.getSheetAt(sheetId);
        List<HSSFPictureData> hssfPictures = hssfWorkbook.getAllPictures();
        for (HSSFPictureData hssfPictureData : hssfPictures) {
            byte[] bytes = hssfPictureData.getData();
            int pictureIdx = hssfWorkbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);   //Adds a picture to the workbook
          
            CreationHelper helper = hssfWorkbook.getCreationHelper();
            Drawing drawing = sheet.createDrawingPatriarch();     //Creates the top-level drawing patriarch.
            
            ClientAnchor anchor = helper.createClientAnchor();   //Create an anchor that is attached to the worksheet
            anchor.setCol1(5);                      //set top-left corner for the image
            anchor.setRow1(sheet.getLastRowNum()+3);
            
            Picture pict = drawing.createPicture(anchor, pictureIdx);   //Creates a picture
            pict.resize();       //Reset the image to the original size
        }
        
        HSSFPatriarch hssfPatriarch = sheet.getDrawingPatriarch();
        hssfPatriarch.setCoordinates(1, 1, 20, 20);
        List<HSSFShape> hssfShapes = hssfPatriarch.getChildren();
        for (HSSFShape hssfShape : hssfShapes) {
            hssfPatriarch.removeShape(hssfShape);
        }
    }
    
    
    
    /**
     * 生成XLS文档
     * @param params
     * @throws IOException
     */
    public void generate(Map<String, Object> params) throws IOException {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(tmplName);
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook(inputStream);
        HSSFSheet sheet = hssfWorkbook.getSheetAt(0);
        
        boolean showRow = true;
        int hiddenStart = 0;
        boolean loopStarted = false;
        String dataSource = null;
        
        RowWrapper rowWrapper = null;
        HSSFCellStyle style = null;
        
        for (int rowIndex = 0; rowIndex < sheet.getLastRowNum(); rowIndex++) {
            HSSFRow row = sheet.getRow(rowIndex);
            if (row == null) {
                logger.debug("rowId="+ rowIndex +" is null");
                continue;
            }
            
            if (!showRow) {       //不显示行
                HSSFCell cell = row.getCell(0);
                if (cell != null && ExcelTmplParser.isShowEndCmd(cell.getStringCellValue())) {    //show标签结束符
                    delRow(sheet, hiddenStart, rowIndex+1);    //删除行
                    rowIndex = hiddenStart;    //将行指针回退
                    logger.debug("remove start rowId=" + rowIndex + ", end rowId=" + rowIndex);
                    showRow = true;
                }
                sheet.removeRow(row);
                continue;
            }
            
            break_cell_label:{
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
                            double num_value = cell.getNumericCellValue();
                            if (loopStarted) {
                                rowWrapper.addCell(num_value, false, style);
                            }else {
                                cell.setCellValue(num_value);
                            }
                            break;
                        case Cell.CELL_TYPE_STRING:
                            String value = cell.getStringCellValue();
                            logger.debug("rowId=" + rowIndex + ", cellId=" + cellIndex + " value=" + value);
                            
                            if (ExcelTmplParser.isShowRowCmd(value)) {      //行显示命令
                                boolean showSingleRow = ExcelTmplParser.isShow(value, params);
                                if (!showSingleRow) {
                                    delRow(sheet, rowIndex, rowIndex+1);
                                    break break_cell_label;
                                }else {
                                    String cmd = StringUtils.find(value, ExcelTmplParser.CODE_TAG_SHOW_ROW_GROUP_REGEX);
                                    String cellValue = value.substring(cmd.length());
                                    cellValue = ExcelTmplParser.fillParams(cellValue, params);
                                    cell.setCellValue(cellValue);
                                }
                                
                            } else if (ExcelTmplParser.isShowStartCmd(value)) {
                                showRow = ExcelTmplParser.isShow(value, params);
                                if (!showRow) {
                                    hiddenStart = rowIndex;
                                    logger.debug("rowId=" + rowIndex + ", cellId=" + cellIndex + " hiddenStart=" + hiddenStart);
                                    break;
                                }else {
                                    logger.debug("del rowId=" + rowIndex + ", cellId=" + cellIndex + " remove");
                                    delRow(sheet, rowIndex, rowIndex+1);
                                    rowIndex = rowIndex - 1;     //回退一行
                                }
                            } else if (ExcelTmplParser.isShowEndCmd(value)) {
                                logger.debug("rowId=" + rowIndex + ", cellId=" + cellIndex + " showEndCMD, remove cell=" + rowIndex);
                                delRow(sheet, rowIndex , rowIndex+1);
                                rowIndex = rowIndex - 1;
                            } else {
                                if (ExcelTmplParser.isLoopCmd(value)) {
                                    loopStarted = true;
                                    
                                    String cmd = StringUtils.find(value, "\\[loop:datasource=.+?\\/]");
                                    dataSource = ExcelTmplParser.getLoopDatasource(cmd);
                                    
                                    String cellValue = value.substring(cmd.length());
                                    boolean isParam = ExcelTmplParser.isParam(cellValue);
                                    
                                    rowWrapper = new RowWrapper(row.getHeight(), row.getLastCellNum());
                                    rowWrapper.addCell(ExcelTmplParser.extractParamName(cellValue), isParam, style);
                                } else if (loopStarted) {
                                    boolean isParam = ExcelTmplParser.isParam(value);
                                    rowWrapper.addCell(ExcelTmplParser.extractParamName(value), isParam, style);
                                } else {
                                    value = ExcelTmplParser.fillParams(value, params);
                                    cell.setCellValue(value);
                                }                        
                            }
                    }
                }
            }
            
            
            if (loopStarted) {
                int rowNum = 0;
                if (dataSource.equals("waybills")) {
                    rowNum = (Integer)params.get("count");
                }else {
                    rowNum = (Integer)params.get("cpt_count");
                }
                
                insertRowBefore(sheet, rowIndex+1, rowNum-1);
//                moveDown(sheet, rowIndex+1, rowNum-1);
                
                logger.debug("loostarted row="+ (rowIndex+1) +" to row="+ sheet.getLastRowNum() +" move down " + (rowNum-1));
                
                String fileName = (String)params.get(dataSource);
                XmlDataSourceParser dh = new XmlDataSourceParser(fileName, hssfWorkbook, rowIndex, rowWrapper);
                dh.parse();
                
                loopStarted = false;
                
                rowIndex = dh.getLastRowId()-1;
            }
        }
        
        
        movePic(hssfWorkbook, 0);
        
        String fileName = (String)params.get(PARAM_OUTPUT_FILE);
        FileOutputStream fileOut = new FileOutputStream(new File(fileName));
        hssfWorkbook.write(fileOut);
        fileOut.close();
    }
    
    
    public static BillHead getBillHead() {
        BillHead billHead = new BillHead();
        billHead.setBillStart("2015-03-01");
        billHead.setBillEnd("2015-04-31");
        billHead.setBillingMembers("61303380");
        billHead.setContact("ZJ");
        billHead.setCurrency("CNY");
        billHead.setCurrentAmount(100011.1);
        billHead.setCustCode("7550001234");
        billHead.setCustomerName("Lily");
        billHead.setDeptCode("755A");
        billHead.setDueDate("2015-02-06");
        billHead.setTheAmountOfUnpaid(80000);
        billHead.setTotalAmount(180011.1);
        
        return billHead;
    }
    
    public static BillBottom getBillBottom(){
        BillBottom billBottom = new BillBottom();
        billBottom.setCompanyName("顺丰速运深圳有限公司");
        billBottom.setBankName("招商银行深圳福田区支行");
        billBottom.setBankAccount("400562322252362");
        billBottom.setLinkMan("王小姐");
        billBottom.setTelphone("0755-86235895");
        billBottom.setEmail("Mr.Wang@sf-express.com");
        billBottom.setFaxNo("0755-86235895");
        
        return billBottom;
    }
    
    public static void main(String[] args) throws IOException {
        BillHead billHead = getBillHead();
        BillBottom billBottom = getBillBottom();
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("billHead", billHead);
        params.put("billBottom", billBottom);
        
        params.put("waybills", "E:/waybills_attr_4_dev.xml");
        params.put("count", 26);
        
        params.put("cpts", "E:/cpts.xml");
        params.put("cpt_count", 22);
        
        BillSubTotal billSubTotal = new BillSubTotal();
        billSubTotal.addSpecialRebate(20);
        params.put("billSubTotal", billSubTotal);
        
        params.put(PARAM_OUTPUT_FILE, "E:/test.xls");
        
//        getInstance("bill_storage_tmpl.xls").generate(params);
        
        getInstance("excel_template.xls").generate(params);
        
        
        /*for(int i = 1; i < 10; i++) {
            System.out.println("i = " + i);
            cellLoop:{
                for (int j = 0; j < 10; j++) {
                    System.out.println("j = " + j);
                    switch (i) {
                    case 2:
                        if(j == 2) {
                            System.out.println("------------------");
                            break cellLoop;
                        }
                        break;

                    default:
                        break;
                    }
                }
            }
            
        }*/
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
    }
}


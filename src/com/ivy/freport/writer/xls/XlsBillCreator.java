/* 
 * Copyright (c) 2015, S.F. Express Inc. All rights reserved.
 */

package com.ivy.freport.writer.xls;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.xml.sax.Attributes;

import com.sf.ecbil.bill.support.XlsTmplParser;
import com.sf.ecbil.bill.util.FileUtils;
import com.sf.ecbil.bill.util.StringUtils;
import com.sf.ecbil.bill.util.TmplConfig;
import com.sf.ecbil.bill.vo.CellWrapper;
import com.sf.ecbil.bill.vo.RowWrapper;

/**
 * 描述：
 * 
 * <pre>HISTORY
 * ****************************************************************************
 *  ID   DATE           PERSON          REASON
 *  1    2015年4月7日      Steven.Zhu         Create
 * ****************************************************************************
 * </pre>
 * @author Steven.Zhu
 * @since 
 */

public class XlsBillCreator implements XmlDSListener {
    
    private final Logger logger = Logger.getLogger(XlsBillCreator.class);
    
    public static final String PARAM_FILE_SEQ = "${file_seq}";
    
    private BillParams billParams;
    
    private HSSFWorkbook workbook;
    private InputStream tmplInputStream;
    private HSSFSheet sheet;
    private int curSheetId;
    
    private HSSFRow hssfRow;
    private int curRow = 1;
    
    private String ds;
    private RowWrapper rowWrapper;

    boolean showRows = true;
    boolean loopStarted = false;
    private int curOrder;
    
    private int xlsFileSeq = 1;
    private int orderCount;
    
    private int count = 1;
    
    private boolean moved = false;
    
    private String output;
    
    public XlsBillCreator(BillParams billParams) {
        super();
        
        if (billParams == null || billParams.isEmpty()) {
            logger.error("params is empty");
            throw new RuntimeException("params should not empty!!!");
        }
        
        this.billParams = billParams;
        orderCount = (int) billParams.get(BillParams.PARAM_DS_ORDER_COUNT);
        
        output = (String) billParams.get(BillParams.PARAM_OUTPUT);
        if (output == null) {
            logger.error("not supply excel file name, param output is null");
            throw new RuntimeException("not supply excel file name, param output is null");
        }else {
            int lastIndex = output.lastIndexOf(".");
            if (lastIndex == -1) {
                output = output + "_" + PARAM_FILE_SEQ;
            }else {
                output = output.substring(0, lastIndex) + "_" + PARAM_FILE_SEQ + output.substring(lastIndex);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    public boolean createXlsBill() {
        try {
            String tmplFile = FileUtils.getFilePath((String) billParams.get(BillParams.PARAM_TMPL));
            tmplInputStream = new FileInputStream(tmplFile);             
            workbook = new HSSFWorkbook(tmplInputStream);
            sheet = workbook.getSheetAt(0);
            
            for (curRow = 0; curRow < sheet.getLastRowNum(); curRow++) {
                hssfRow = sheet.getRow(curRow);
                if (hssfRow == null) {
                    logger.debug("curRow="+ curRow +" is null");
                    continue;
                }
                
                if (!showRows) {       //不显示行
                    HSSFCell cell = hssfRow.getCell(0);
                    if (cell != null && XlsTmplParser.isShowEndCmd(cell.getStringCellValue())) {    //show标签结束符
                        showRows = true;
                    }
                    
                    XlsUtils.delRow(sheet, curRow, curRow+1);    //删除行
                    curRow --;
                    logger.debug("remove row " + count);
                    continue;
                }
                
                for (int cellIndex = 0; cellIndex < hssfRow.getLastCellNum(); cellIndex++) {
                    HSSFCell cell = hssfRow.getCell(cellIndex);
                    handleCell(cell);
                }
                
                if (loopStarted) {
                    if (!BillParams.PARAM_DS_ORDER.equals(ds)) {
                        int count = (Integer)billParams.get(ds + "_count");
                        if (count > 0) {
                            XlsUtils.insertRowBefore(sheet, curRow, count-1);
                        }
                    }
//                    curRow --;
                    List<File> xmlFiles = (List<File>)billParams.get(ds);
                    XmlDSParser xmlDSParser = new XmlDSParser(xmlFiles);
                    xmlDSParser.addListener(this);
                    xmlDSParser.parse();
                    
                    rowWrapper = null;
                    ds = null;
                    loopStarted = false;
                    curRow--;
                }
                
                // cp sheet 2 to sheet 1, and delete sheet 2
                if (!moved && curRow == sheet.getPhysicalNumberOfRows() - 1) {
                    if (orderCount%TmplConfig.getMaxXmlFileOrderSize() < TmplConfig.getMaxOrderSizePerSheet()) {
                        HSSFSheet srcSheet = workbook.getSheetAt(1);
                        XlsUtils.copySheet(srcSheet, sheet, sheet.getPhysicalNumberOfRows());
                        workbook.removeSheetAt(1);
                        moved = true;
                    }
                }
                
                count ++;
            }
            
            return true;
        } catch (Exception e) {
            logger.error("", e);
            e.printStackTrace();
        } finally {
            close();
        }
        
        return false;
    }
        
    private void handleCell(HSSFCell cell) {
        if (cell == null) {
            return;
        }
        
        HSSFCellStyle style = workbook.createCellStyle();  
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
                
                if (XlsTmplParser.isShowRowCmd(value)) {      //行显示命令
                    boolean showSingleRow = XlsTmplParser.isShow(value, billParams);
                    if (!showSingleRow) {
                        XlsUtils.delRow(sheet, curRow, curRow+1);
                        curRow --;     //回退一行 
                    }else {
                        String cmd = StringUtils.find(value, XlsTmplParser.CODE_TAG_SHOW_ROW_GROUP_REGEX);
                        String cellValue = value.substring(cmd.length());
                        cellValue = (String)XlsTmplParser.fillParams(cellValue, billParams);
                        cell.setCellValue(cellValue);
                    }
                    break;
                } else if (XlsTmplParser.isShowStartCmd(value)) {
                    showRows = XlsTmplParser.isShow(value, billParams);
                    XlsUtils.delRow(sheet, curRow, curRow+1);
                    curRow --;     //回退一行
                    break;
                } else if (XlsTmplParser.isShowEndCmd(value)) {
                    XlsUtils.delRow(sheet, curRow, curRow+1);
                    curRow --;     //回退一行
                    break;
                } else {
                    if (XlsTmplParser.isLoopCmd(value)) {
                        loopStarted = true;
                        
                        String cmd = StringUtils.find(value, "\\[loop:datasource=.+?\\/]");
                        ds = XlsTmplParser.getLoopDs(cmd);
                        
                        String cellValue = value.substring(cmd.length());
                        boolean isParam = XlsTmplParser.isParam(cellValue);
                        
                        rowWrapper = new RowWrapper(hssfRow.getHeight(), hssfRow.getLastCellNum());
                        rowWrapper.addCell(XlsTmplParser.extractParamName(cellValue), isParam, style);
                    } else if (loopStarted) {
                        boolean isParam = XlsTmplParser.isParam(value);
                        rowWrapper.addCell(XlsTmplParser.extractParamName(value), isParam, style);
                    } else {
                        value = (String)XlsTmplParser.fillParams(value, billParams);
                        cell.setCellValue(value);
                    }                        
              }
        }
    }
    
    /**
     * 写账单文件到硬盘
     */
    private void close() {
        if (workbook == null) {
            return;
        }
        String fileName = output.replace(PARAM_FILE_SEQ, String.valueOf(xlsFileSeq));
        FileOutputStream fileOut = null;
        try {
            fileOut = new FileOutputStream(new File(fileName));
            workbook.write(fileOut);
            logger.info(fileName + " is written successfully");
        } catch (FileNotFoundException e) {
            logger.error("", e);
            e.printStackTrace();
        } catch (IOException e) {
            logger.error("", e);
            e.printStackTrace();
        } finally {
            try {
                if (fileOut != null) {
                    fileOut.close();
                }
                if (tmplInputStream != null) {
                    tmplInputStream.close();
                }
                workbook = null;
            } catch (IOException e) {
                logger.error("", e);
                e.printStackTrace();
            }
        }
    }
    
    

    /* (non-Javadoc)
     * @see com.sf.ecbil.bill.xls.XmlDSListener#readNextElement()
     */
    @Override
    public boolean readNextElement(int fileSeq, String qName, Attributes attributes) {
        HSSFRow newRow = sheet.createRow(curRow);
        newRow.setHeight(rowWrapper.getHeight());
        
        List<CellWrapper> cellWrappers = rowWrapper.getCellWrappers();
        for (int i = 0; i < cellWrappers.size(); i++) {
            HSSFCell cell = newRow.createCell(i);
            
            CellWrapper cellWrapper = cellWrappers.get(i);
            if (cellWrapper.isParam()) {
                cell.setCellValue(attributes.getValue(cellWrapper.getCellValue().toString()));
            }else {
                if (cellWrapper.getCellValue().getClass() == Double.class) {
                    cell.setCellValue((Double)cellWrapper.getCellValue()); 
                } else {
                    cell.setCellValue((String)cellWrapper.getCellValue());
                }
            }
            
            cell.setCellStyle(cellWrapper.getHssfCellStyle());
        }
        
        curRow ++;
        
        if (billParams.get(BillParams.PARAM_DS_ORDER).equals(ds)) {
            curOrder ++;
            
            if (curOrder > 0 && curOrder % TmplConfig.getMaxXmlFileOrderSize() == 0) {        //大于100K分文件
                int remain = orderCount - curOrder;
                if (remain == 0) {       //保留尾部
                    return true;
                }else {
                    close();
                }
                
                logger.info("create new xls file!!!");
                
                curSheetId = 0;
                xlsFileSeq ++;
                curRow = 0;
                
                try {
//                    String tmpl = (String)xlsBillParams.get("tmpl");
//                    tmpl = tmpl.substring(0, tmpl.lastIndexOf(".")) + "_multifile" + tmpl.substring(tmpl.lastIndexOf("."));
                    curRow = 1;   //skip head
                    
                    String extraTmpl = (String) billParams.get(BillParams.PARAM_EXTRA_TMPL);
                    if (extraTmpl == null || extraTmpl.isEmpty()) {
                        logger.error("extra tmpl not supplied!!!");
                        throw new RuntimeException("extra tmpl not supplied!!!");
                    }
                    
                    tmplInputStream = new FileInputStream(FileUtils.getFilePath(extraTmpl));
                    workbook = new HSSFWorkbook(tmplInputStream);
                    
                    for (int i = 0; i < cellWrappers.size(); i++) {
                        CellWrapper cellWrapper = cellWrappers.get(i);
                        HSSFCellStyle newCellStyle = workbook.createCellStyle();   
                        newCellStyle.cloneStyleFrom(cellWrapper.getHssfCellStyle()); 
                        cellWrapper.setHssfCellStyle(newCellStyle);
                    }
                    
                    sheet = workbook.getSheetAt(curSheetId);
                } catch (IOException e) {
                    logger.error("", e);
                    e.printStackTrace();
                }
                
            }else if (curOrder > 0 && curOrder % TmplConfig.getMaxOrderSizePerSheet() == 0) {        //大于50K分页
                curSheetId ++;
                sheet = workbook.getSheetAt(curSheetId);
                curRow = 0;
                
                int remain = orderCount - curOrder;
                if (remain <= TmplConfig.getMaxOrderSizePerSheet()) {       //保留尾部
                    XlsUtils.insertRowBefore(sheet, 0, remain);
                    /*XlsUtils.movePic(workbook, 1, remain);
                    HSSFPatriarch hssfPatriarch = curSheet.getDrawingPatriarch();
                    List<HSSFShape> hssfShapes = hssfPatriarch.getChildren();
                    for (HSSFShape hssfShape : hssfShapes) {
                        hssfPatriarch.removeShape(hssfShape);
                    }*/
                    
                }else{
                    workbook.removeSheetAt(curSheetId);
                    sheet = workbook.createSheet();
                }
                
                
                //panduan remain > 50000 删除sheet1所有内容，否则下移
            }
        }
        
        return true;
    }
    
    /*private static void test() {
        BillHead billHead = DataCreate.getBillHead();
        BillBottom billBottom = DataCreate.getBillBottom();
        
        List<File> xmlFiles = new ArrayList<File>();
        xmlFiles.add(new File("E:/order_datasource1.xml"));
        xmlFiles.add(new File("E:/order_ds_1.xml"));
//        xmlFiles.add(new File("E:/order_ds_2.xml"));
//        xmlFiles.add(new File("E:/order_ds_0.xml"));
        
        List<File> sdFiles = new ArrayList<File>();
        sdFiles.add(new File("E:/sd.xml"));
        
        BillParams billParams = new BillParams();
        billParams.put(BillParams.PARAM_TMPL, Config.getBillTmpl(EnumFileType.BILL_STANDARD));
        billParams.put("billHead", billHead);
        billParams.put("billBottom", billBottom);
        billParams.put("order", xmlFiles);
        billParams.put("order_count", 100005);
        billParams.put("cpt", sdFiles);
        billParams.put("cpt_count", 5);
        billParams.put("output", "D:/xx.xls");
        
        
        XlsBillParams xlsBillParams = new XlsBillParams("d:/user/613080/桌面/待处理邮件/tmpl_xls_bill_standard.xls", 
                "E:/aaxd.xls", billHead, billBottom, 100005, xmlFiles, 0, null, 5, sdFiles, null);
         
        new XlsBillCreator(billParams).createXlsBill();
    }
    
    public static void main(String[] args) throws IOException {
        test();
    }*/
}


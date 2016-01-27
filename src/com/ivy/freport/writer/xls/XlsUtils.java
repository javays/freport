/* 
 * Copyright (c) 2015, S.F. Express Inc. All rights reserved.
 */

package com.ivy.freport.writer.xls;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFPictureData;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * 描述：
 * 
 * <pre>HISTORY
 * ****************************************************************************
 *  ID   DATE           PERSON          REASON
 *  1    2015年4月8日      Steven.Zhu         Create
 * ****************************************************************************
 * </pre>
 * @author Steven.Zhu
 * @since 
 */

public class XlsUtils {
    
    /**
     * 插入行
     * @param sheet
     * @param startRow
     * @param forward
     */
    public static void insertRowBefore(HSSFSheet sheet,
            int insertPoint,
            int insertRowNum) {
        
        sheet.shiftRows(insertPoint, sheet.getPhysicalNumberOfRows(), insertRowNum);
        for (int i = insertPoint; i < sheet.getPhysicalNumberOfRows(); i++) {
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
    public static void delRow(HSSFSheet sheet, int startRow, int endRow) {
        for (int i = startRow; i < endRow; i++) {
            removeMergeRegions(sheet, i);
        }
        
        int forward = endRow-startRow;
        sheet.shiftRows(endRow, sheet.getPhysicalNumberOfRows(), -forward);
        for (int i=endRow; i<sheet.getPhysicalNumberOfRows(); i++) {
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
     * 删除指定行跨行跨列单元格
     * @param sheet
     * @param rowIndex
     */
    public static void removeMergeRegions(HSSFSheet sheet, int rowIndex) {
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress cellRangeAddress = sheet.getMergedRegion(i);
            int firstRow = cellRangeAddress.getFirstRow();
            if (firstRow == rowIndex) {
                sheet.removeMergedRegion(i);
            }
        }
    }
    
    /**
     * 获取制定行跨行
     * @param sheet
     * @param rowIndex
     * @return
     */
    public static List<CellRangeAddress> getMergeRegions(HSSFSheet sheet, int rowIndex) {
        List<CellRangeAddress> result = new ArrayList<CellRangeAddress>();
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress cellRangeAddress = sheet.getMergedRegion(i);
            int firstRow = cellRangeAddress.getFirstRow();
            if (firstRow == rowIndex) {
                result.add(cellRangeAddress);
            }
        }
        return result;
    }
    
    
    /**
     * 移动文档中图片位置
     * @param hssfWorkbook
     * @param sheetId
     */
    public static void movePic(HSSFWorkbook hssfWorkbook, int sheetId, int row) {
        HSSFSheet sheet = hssfWorkbook.getSheetAt(sheetId);
        List<HSSFPictureData> hssfPictures = hssfWorkbook.getAllPictures();
        for (HSSFPictureData hssfPictureData : hssfPictures) {
            byte[] bytes = hssfPictureData.getData();
            int pictureIdx = hssfWorkbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);   //Adds a picture to the workbook
          
            CreationHelper helper = hssfWorkbook.getCreationHelper();
            Drawing drawing = sheet.createDrawingPatriarch();     //Creates the top-level drawing patriarch.
            
            ClientAnchor anchor = helper.createClientAnchor();   //Create an anchor that is attached to the worksheet
            anchor.setCol1(5);                      //set top-left corner for the image
            anchor.setRow1(row);
            anchor.setRow2(row+5);
            
            Picture pict = drawing.createPicture(anchor, pictureIdx);   //Creates a picture
            pict.resize();       //Reset the image to the original size
        }
    }
    
    /**
     * 从一个sheet拷贝到另外一个sheet
     * @param srcSheet
     * @param targetSheet
     * @param insertPointRow
     */
    public static void copySheet(HSSFSheet srcSheet, HSSFSheet targetSheet, int insertPointRow) {
        for (int i = 0; i < srcSheet.getNumMergedRegions(); i++) {
            CellRangeAddress cellRangeAddress = srcSheet.getMergedRegion(i);
            cellRangeAddress.setFirstRow(cellRangeAddress.getFirstRow()+ insertPointRow);
            cellRangeAddress.setLastRow(cellRangeAddress.getLastRow() + insertPointRow);
            targetSheet.addMergedRegion(cellRangeAddress);
        }
        
        for (int i = 0; i < srcSheet.getLastRowNum(); i++) {
            HSSFRow srcRow = srcSheet.getRow(i);
            HSSFRow targetRow = targetSheet.createRow(i+insertPointRow);
            
            if (srcRow == null) {
                continue;
            }
            
            targetRow.setHeight(srcRow.getHeight());
            
            for (int j = 0; j < srcRow.getPhysicalNumberOfCells(); j++) {
                HSSFCell srcCell = srcRow.getCell(j);
                HSSFCell targetCell = targetRow.createCell(j);
                
                if (srcCell == null) {
                    continue;
                }
                copyCell(srcCell, targetCell);
            }
        }
    }
    
    /**
     * @param oldCell
     * @param newCell
     * @param styleMap
     */
    public static void copyCell(HSSFCell oldCell, HSSFCell newCell) {   
        if(oldCell.getSheet().getWorkbook() == newCell.getSheet().getWorkbook()){   
            newCell.setCellStyle(oldCell.getCellStyle());   
        } else{   
            HSSFCellStyle newCellStyle = newCell.getSheet().getWorkbook().createCellStyle();   
            newCellStyle.cloneStyleFrom(oldCell.getCellStyle());   
            newCell.setCellStyle(newCellStyle);   
        } 
        
        switch(oldCell.getCellType()) {   
            case HSSFCell.CELL_TYPE_STRING:   
                newCell.setCellValue(oldCell.getStringCellValue());   
                break;   
          case HSSFCell.CELL_TYPE_NUMERIC:   
                newCell.setCellValue(oldCell.getNumericCellValue());   
                break;   
            case HSSFCell.CELL_TYPE_BLANK:   
                newCell.setCellType(HSSFCell.CELL_TYPE_BLANK);   
                break;   
            case HSSFCell.CELL_TYPE_BOOLEAN:   
                newCell.setCellValue(oldCell.getBooleanCellValue());   
                break;   
            case HSSFCell.CELL_TYPE_ERROR:   
                newCell.setCellErrorValue(oldCell.getErrorCellValue());   
                break;   
            case HSSFCell.CELL_TYPE_FORMULA:   
                newCell.setCellFormula(oldCell.getCellFormula());   
                break;   
            default:   
                break;   
        }   
            
    }
    
    /**
     * 测试
     * @throws IOException
     */
    private static void test() throws IOException {
        // XlsBillParams xlsBillParams = new XlsBillParams("E:/aaxd.xls",
        // "E:/aaxd.xls", 0, 0, null, null, null, null, null);
        InputStream inputStream = new FileInputStream(
                "d:/user/613080/桌面/tmpl_xls_bill_standard_multifile.xls");
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook(inputStream);
        HSSFSheet curSheet = hssfWorkbook.getSheetAt(1);
        HSSFSheet sheet2 = hssfWorkbook.getSheetAt(0);

        // copySheets(curSheet, sheet2);
        copySheet(curSheet, sheet2, 1);

        // movePic(hssfWorkbook, 0);

        hssfWorkbook.removeSheetAt(1);

        FileOutputStream fos = new FileOutputStream("E:/aaxd.xls");
        hssfWorkbook.write(fos);
        fos.flush();
        fos.close();
    }
    
    public static void main(String[] args) throws IOException {
        test();
    }
}

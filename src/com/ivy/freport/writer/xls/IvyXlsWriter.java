/* 
 * Copyright (c) 2015, S.F. Express Inc. All rights reserved.
 */

package com.ivy.freport.writer.xls;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFPrintSetup;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.hssf.util.HSSFRegionUtil;
import org.apache.poi.ss.util.CellRangeAddress;
import org.dom4j.DocumentException;

import com.ivy.freport.ds.IvyDSAccessListener;
import com.ivy.freport.ds.IvyDataSource;
import com.ivy.freport.layout.Align;
import com.ivy.freport.layout.DataType;
import com.ivy.freport.layout.IvyCellDesc;
import com.ivy.freport.layout.IvyDocDesc;
import com.ivy.freport.layout.IvyRowDesc;
import com.ivy.freport.layout.IvyTableDesc;
import com.ivy.freport.utils.StringUtils;
import com.ivy.freport.utils.TmplParser;

/**
 * 描述：根据xml布局生成EXCEL文件
 * 
 * <pre>HISTORY
 * ****************************************************************************
 *  ID   DATE           PERSON          REASON
 *  1    2015年5月15日      Steven.Zhu         Create
 * ****************************************************************************
 * </pre>
 * @author Steven.Zhu
 * @since 
 */

public abstract class IvyXlsWriter<E> implements IvyDSAccessListener<E> {
    
    private final Logger logger = Logger.getLogger(IvyXlsWriter.class);
    
    public static final String PARAM_FILE_SEQ = "${file_seq}";
    public static final int SHEET_MAX_ITEM_ROW = 50000;
    public static final int WORKBOOK_SHEET_NUM = 2;
    
    private IvyDocDesc ivyDocDesc; 
    private Map<String, IvyDataSource<E>> dataSources;
    private Map<String, Object> otherAttrs;
    private String output;
    
    private IvyTableDesc ivyTableDesc;
    
    private List<IvyRowDesc> multiHeadRow;      //表格行头，保存，分文件使用
    private List<List<HSSFCellStyle>> multiHeadRowStyles;
    
    private List<HSSFCellStyle> loopRowStyles;
    
    private int itemCount;        //订单记录数
    private int curItemIndex = 0;     //当前订单索引
    
    protected HSSFWorkbook workbook;   //POI工作表
    protected HSSFSheet sheet;         //POI工作表单
    protected int rowId;               //POI行ID
    
    protected IvyRowDesc loopRowDesc;    //循环数据源，行定义
    
    private int fileSeq = 1;         //输出文件序列
    
    private Map<String, HSSFColor> colors = new HashMap<String, HSSFColor>();   //表单颜色
    
    /**
     * @param ivyDocDesc
     * @param dataSources
     * @param otherAttrs
     * @param output
     */
    public IvyXlsWriter(IvyDocDesc ivyDocDesc,
            Map<String, IvyDataSource<E>> dataSources,
            Map<String, Object> otherAttrs, String output) {
        super();
        this.ivyDocDesc = ivyDocDesc;
        this.dataSources = dataSources;
        this.otherAttrs = otherAttrs;
        this.output = output;
    }
    
    /**
     * 新增表格
     * @param tableId
     * @throws DocumentException
     */
    public boolean create() {
        int lastIndex = output.lastIndexOf(".");
        if (lastIndex == -1) {
            output = output + "_" + PARAM_FILE_SEQ;
        }else {
            output = output.substring(0, lastIndex) + "_" 
                    + PARAM_FILE_SEQ + output.substring(lastIndex);
        }
        
        ivyTableDesc = ivyDocDesc.getIvyTableDescs().get(0);
        
        createWorkbook();
        createSheet();
        
        List<IvyRowDesc> billRows = ivyTableDesc.getIvyRowDescs();
        for (IvyRowDesc billRow : billRows) {
            createRow(billRow);            
        }
        flush();
        
        return true;
    }
    
    /**
     * 创建工作表
     */
    private void createWorkbook() {
        workbook = new HSSFWorkbook();
        if (loopRowStyles != null) {
            for (int i=0; i<loopRowStyles.size(); i++) {    //重新定义样式
                HSSFCellStyle cellStyle = workbook.createCellStyle();
                cellStyle.cloneStyleFrom(loopRowStyles.get(i));
                loopRowStyles.set(i, cellStyle);
            }
        }
        if (multiHeadRowStyles != null) {
            for (List<HSSFCellStyle> headRowStyles : multiHeadRowStyles) {
                for (int j=0; j<headRowStyles.size(); j++) {
                    HSSFCellStyle cellStyle = workbook.createCellStyle();
                    cellStyle.cloneStyleFrom(headRowStyles.get(j));
                    headRowStyles.set(j, cellStyle);
                }
            }
        }
    }
    
    /**
     * 新增表单
     */
    private void createSheet() {
        sheet = workbook.createSheet();
        sheet.setDisplayGridlines(false);
        sheet.setPrintGridlines(false);
        sheet.setFitToPage(true);
        sheet.setHorizontallyCenter(true);
        
        sheet.setMargin(HSSFSheet.TopMargin, (double) 0.2); // 上边距
        sheet.setMargin(HSSFSheet.BottomMargin, (double) 0.2); // 下边距
        sheet.setMargin(HSSFSheet.LeftMargin, (double) 0.2); // 左边距
        sheet.setMargin(HSSFSheet.RightMargin, (double) 0.2); // 右边距
        
        HSSFPrintSetup printSetup = sheet.getPrintSetup();    
        printSetup.setPaperSize(HSSFPrintSetup.A4_PAPERSIZE); 
        
        IvyRowDesc billRow = ivyTableDesc.getIvyRowDescs().get(0);
        List<IvyCellDesc> billCells = billRow.getIvyCellDescs();
        for (int i = 0; i < billCells.size(); i++) {
            IvyCellDesc billCell = billCells.get(i);
            sheet.setColumnWidth(i, billCell.getWidth() * 256);
        }
    }
    
    /**
     * 输出文件
     */
    private void flush() {
        if (workbook == null) {
            return;
        }
        FileOutputStream fos = null;
        try {
            String fileName = output.replace(PARAM_FILE_SEQ, String.valueOf(fileSeq));
            fos = new FileOutputStream(fileName);
            workbook.write(fos);
            fileSeq ++;
            logger.info("create file " + fileName + " successfully!!!");
        } catch (FileNotFoundException e) {
            logger.error("", e);
            e.printStackTrace();
        } catch (IOException e) {
            logger.error("", e);
            e.printStackTrace();
        } finally {
            try {
                if(fos != null) fos.close();
                workbook = null;
            } catch (IOException e) {
                logger.error("", e);
                e.printStackTrace();
            }
        }
    }
    
    
    
    /**
     * 添加行
     * @param billRow
     * @throws DocumentException
     */
    private void createRow(IvyRowDesc rowDesc) {
        if (rowDesc == null) {
            return;
        }
        
        if (rowDesc.getShow() != null && 
           !TmplParser.validate(rowDesc.getShow(), otherAttrs)) {   //行不显示，返回
            return;
        }
        
        List<IvyCellDesc> billCells = rowDesc.getIvyCellDescs();
        if (billCells == null || billCells.isEmpty()) {     //行无单元格定义，返回
            return;
        }
        
        int maxline = rowDesc.getMaxline();
        if (maxline > 0) {                //根据换行符\n多行显示
            createMutilRow(rowDesc);
        } else if(!StringUtils.isEmpty(rowDesc.getDs())){   //根据数据源循环多行显示
            createLoopRow(rowDesc);            
        } else {       //其他直接创建行显示
            String rowName = rowDesc.getName();
            List<HSSFCellStyle> headCellStyles = null;
            if (IvyRowDesc.HEAD.equals(rowName)) {
                addHeadRow(rowDesc);
                headCellStyles = new ArrayList<HSSFCellStyle>();
                addHeadRowCellStyles(headCellStyles);
            }
            
            HSSFRow hssfRow = null;
            if (rowId < sheet.getPhysicalNumberOfRows()) {
                hssfRow = sheet.getRow(rowId);
            } else {
                hssfRow = sheet.createRow(rowId);
            }
            hssfRow.setHeight((short)(rowDesc.getHeight()*20));
            for (int j = 0; j < billCells.size(); j++) {
                IvyCellDesc billCell = billCells.get(j);
                HSSFCellStyle cellStyle = createCellStyle(billCell);
                if (IvyRowDesc.HEAD.equals(rowName)) {
                    headCellStyles.add(j, cellStyle);
                }
                
                createCell(hssfRow, billCell, cellStyle, null);
                if (billCell.getRowspan() > 1 || billCell.getColspan() > 1) {
                    addMegeredRegion(rowId, 
                                billCell.getCellId(), 
                                billCell.getRowspan(), 
                                billCell.getColspan(), 
                                billCell.getBorder());
                    
                    if (billCell.getRowspan() > 1) {
                        for (int i = 1; i<billCell.getRowspan(); i++) {
                            HSSFRow _hssfRow = null;
                            if ((rowId + i) < sheet.getPhysicalNumberOfRows()) {
                                _hssfRow = sheet.getRow(rowId + i);
                            } else {
                                _hssfRow = sheet.createRow(rowId + i);
                            }
                            
                            HSSFCell _cell = _hssfRow.createCell(billCell.getCellId());
                            _cell.setCellStyle(cellStyle);
                        }
                    }
                    if (billCell.getColspan() > 1) {
                        for (int i = 0; i<billCell.getColspan()-1; i++) {
                            HSSFCell cell = hssfRow.createCell(billCell.getCellId() + (i+1));
                            cell.setCellStyle(cellStyle);
                        }
                    }
                }
            }
            rowId ++;
        }
    }
    
    /**
     * 拆多行
     * @param billRow
     */
    private void createMutilRow(IvyRowDesc billRow) {
        List<IvyCellDesc> billCells = billRow.getIvyCellDescs();
        int maxline = billRow.getMaxline();
        
        List<HSSFCellStyle> cellStyles = new ArrayList<HSSFCellStyle>(billCells.size());
        List<String[]> cellValues = new ArrayList<String[]>(billCells.size());
        for (int j = 0; j < billCells.size(); j++) {   //没列拆分多行值，创建样式
            IvyCellDesc billCell = billCells.get(j);
            cellStyles.add(j, createCellStyle(billCell));
            
            String value = billCell.getValue();
            String[] cellValue = null;
            if (!StringUtils.isEmpty(value)) {
                cellValue = value.split("\n");
            }
            
            cellValues.add(j, cellValue);
        }
        
        HSSFRow hssfRow = null;
        for (int i = 0; i < maxline; i++) {    //创建maxline行
            hssfRow = sheet.createRow(rowId);
            hssfRow.setHeight((short)(billRow.getHeight()*20));
            
            for (int j = 0; j < billCells.size(); j++) {
                IvyCellDesc billCell = billCells.get(j);
                
                int rowspan = billCell.getRowspan();
                int colspan = billCell.getColspan();
                if (rowspan > 1 || colspan > 1) {
                    addMegeredRegion(rowId, 
                                billCell.getCellId(), 
                                rowspan, 
                                colspan, 
                                billCell.getBorder());
                }
                
                String _value = null;
                String[] arr_cellValues = cellValues.get(j);
                if (arr_cellValues == null || i > arr_cellValues.length-1) {
                    _value = "" ;
                } else {
                    _value = arr_cellValues[i];
                }
                
                HSSFCellStyle cellStyle = cellStyles.get(j);
                
                createCell(hssfRow, billCell, cellStyle, _value);
            }
            rowId ++;
        }
    }
    
    /**
     * 为分多文件行头 保存表格行信息
     * @param billRow
     */
    private void addHeadRow(IvyRowDesc rowDesc) {
        if (rowDesc == null) {
            return;
        }
        if (multiHeadRow == null) {
            multiHeadRow = new ArrayList<IvyRowDesc>();
        }
        multiHeadRow.add(rowDesc);
    }
    
    /**
     * 为分多文件行头 保存表格行样式
     * @param hssfCellStyles
     */
    private void addHeadRowCellStyles(List<HSSFCellStyle> hssfCellStyles) {
        if (this.multiHeadRowStyles == null) {
            this.multiHeadRowStyles = new ArrayList<List<HSSFCellStyle>>();
        }
        this.multiHeadRowStyles.add(hssfCellStyles);
    }
    
    /**
     * 循环数据源，输出行
     * @param billRow
     * @throws DocumentException
     */
    private void createLoopRow(IvyRowDesc loopRowDesc) {
        String ds = loopRowDesc.getDs();
        IvyDataSource<E> datasource = dataSources.get(ds);
        if (datasource == null) {
            return;
        }
        
        this.loopRowDesc = loopRowDesc;
        
        List<IvyCellDesc> rowCells = loopRowDesc.getIvyCellDescs();
        loopRowStyles = new ArrayList<HSSFCellStyle>(rowCells.size());
        for (IvyCellDesc ivyCellDesc : rowCells) {
            loopRowStyles.add(createCellStyle(ivyCellDesc));
        }
        
        itemCount = datasource.size();
        curItemIndex = 0;
        
        datasource.addListener(this);
        datasource.next();
        
        multiHeadRow = null;
        multiHeadRowStyles = null;
    }
    
    /**
     * 添加单元格
     * @param hssfRow
     * @param billCell
     * @param cellStyle
     * @param value
     */
    private void createCell(HSSFRow hssfRow, IvyCellDesc billCell, HSSFCellStyle cellStyle, String value) {
        HSSFCell cell = hssfRow.createCell(billCell.getCellId());
        
        value = value==null?billCell.getValue():value;
        cellStyle = cellStyle==null?createCellStyle(billCell):cellStyle;
        cell.setCellStyle(cellStyle);
        
        if (billCell.isHasParam()) {
            Object obj = TmplParser.fillParams(value, otherAttrs);
            obj = obj == null ? "" : obj;
            
            if (billCell.getDataType() == DataType.NUMBER 
                    && obj instanceof Number) {
                cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                cell.setCellValue(Double.parseDouble(obj.toString()));
            } else {
                cell.setCellValue(obj.toString().replaceAll("\\\\n", "\n"));
            }
        } else {
            cell.setCellValue(new HSSFRichTextString(value.replaceAll("\\\\n", "\n")));
        }
    }
    
    //大于10W记录分文件
    //大于5W分页
    protected void checkLimit() {
        if (curItemIndex % SHEET_MAX_ITEM_ROW*WORKBOOK_SHEET_NUM == 0 && curItemIndex < itemCount) {        
            flush();
            
            createWorkbook();
            createSheet();
//            addRow(headRow);
            
            rowId = 0;
            if (multiHeadRow != null) {
                for (int i=0; i<multiHeadRow.size(); i++) {
                    IvyRowDesc headRowDesc = multiHeadRow.get(i);
                    List<HSSFCellStyle> rowCellStyles = multiHeadRowStyles.get(i);
                    
                    HSSFRow hssfRow = sheet.createRow(rowId);
                    hssfRow.setHeight((short)(headRowDesc.getHeight()*20));
                    List<IvyCellDesc> headCellDescs = headRowDesc.getIvyCellDescs();
                    if (headCellDescs != null && !headCellDescs.isEmpty()) {
                        for (int j = 0; j < headCellDescs.size(); j++) {
                            IvyCellDesc headCellDesc = headCellDescs.get(j);
                            createCell(hssfRow, headCellDesc, rowCellStyles.get(j), null);
                        }
                        rowId ++;
                    }
                }
            }
        //恰好记录完结，则不重新创建页
        }else if (curItemIndex % SHEET_MAX_ITEM_ROW == 0 
                && curItemIndex < itemCount) {        
            createSheet();
            rowId = 0;
        }else {
            rowId ++;
        }
    }
    
    /**
     * 跨行跨列
     * @param sheet
     * @param firstRow
     * @param firstCol
     * @param colspan
     */
    protected void addMegeredRegion(int firstRow,
                                int firstCol,
                                int rowspan,
                                int colspan,
                                int border) {
        if (colspan > 1) {
            colspan = colspan - 1;
        }
        if (rowspan > 1) {
            rowspan = rowspan - 1;
        }
        
        CellRangeAddress cellRangeAddress = new CellRangeAddress(
                                            firstRow,
                                            firstRow + rowspan,
                                            firstCol,
                                            firstCol + colspan);
        sheet.addMergedRegion(cellRangeAddress);
        
        HSSFRegionUtil.setBorderTop(border, cellRangeAddress, sheet, workbook);
        HSSFRegionUtil.setBorderLeft(border, cellRangeAddress, sheet, workbook);
        HSSFRegionUtil.setBorderRight(border, cellRangeAddress, sheet, workbook);
        HSSFRegionUtil.setBorderBottom(border, cellRangeAddress, sheet, workbook);
    }
    
    /**
     * 创建样式
     * @param billCell
     * @return
     */
    protected HSSFCellStyle createCellStyle(IvyCellDesc billCell) {
        HSSFCellStyle hssfCellStyle = workbook.createCellStyle();
        hssfCellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);//设置前景填充样式
        hssfCellStyle.setFillForegroundColor(HSSFColor.WHITE.index);//前景填充色
        
        HSSFFont hssfFont = workbook.createFont();
        if (billCell.isBold()) {
            hssfFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        }
        hssfFont.setFontHeightInPoints((short)billCell.getSize());
        hssfCellStyle.setFont(hssfFont);
        
        hssfCellStyle.setAlignment((short)(billCell.getAlign()+1));
        if (billCell.getValign() == Align.MIDDLE.getValue()) {
            hssfCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        } else if (billCell.getValign() == Align.TOP.getValue()) {
            hssfCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
        }
        
        String bgColor = billCell.getBgColor();
        if (!StringUtils.isEmpty(bgColor) && !"#ffffff".equals(bgColor)) {
            HSSFColor hssfColor = createColor(bgColor);
            hssfCellStyle.setFillForegroundColor(hssfColor.getIndex());
        }
        
        if (billCell.getDataType() == DataType.NUMBER &&
                !StringUtils.isEmpty(billCell.getPattern())) {
            hssfCellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat(billCell.getPattern()));
        }

        hssfCellStyle.setBorderLeft((short)billCell.getBorder());
        hssfCellStyle.setBorderRight((short)billCell.getBorder());
        hssfCellStyle.setBorderTop((short)billCell.getBorder());
        hssfCellStyle.setBorderBottom((short)billCell.getBorder());
        
        hssfCellStyle.setWrapText(Boolean.TRUE);
        
        return hssfCellStyle;
    }
    
    /**
     * 创建颜色
     * @param bgColor #ff0000
     * @return
     */
    protected HSSFColor createColor(String bgColor) {
        HSSFColor hssfColor = null;
        
        if (colors.containsKey(bgColor)) {
            hssfColor = colors.get(bgColor);                
        } else {
            HSSFPalette customPalette = workbook.getCustomPalette();  
            int r = Integer.valueOf(bgColor.substring( 1, 3 ), 16);
            int g = Integer.valueOf(bgColor.substring(3, 5), 16);
            int b = Integer.valueOf(bgColor.substring(5, 7), 16);
            
            try {
                hssfColor = customPalette.findColor((byte) r, (byte) g, (byte) b);
                if (hssfColor == null) {
                    customPalette.setColorAtIndex(
                            HSSFColor.LAVENDER.index,
                            (byte) r,
                            (byte) g,
                            (byte) b);
                    hssfColor = customPalette.getColor(HSSFColor.LAVENDER.index);
                    colors.put(bgColor, hssfColor);
                }
            } catch (Exception e) {
                logger.error("", e);
                e.printStackTrace();
            }
        }
        
        return hssfColor;
    }
    
    /*private static void test() {
//        BillHead billHead = DataCreate.getBillHead();
//        BillBottom billBottom = DataCreate.getBillBottom();
        
        List<File> xmlFiles = new ArrayList<File>();
        xmlFiles.add(new File("E:/tmp/8888999904_2015-07-01_2015-07-31_order_V1_1.xml"));
//        xmlFiles.add(new File("E:/tmp/order_ds_1.xml"));
//        xmlFiles.add(new File("E:/tmp/order_ds_2.xml"));
//        xmlFiles.add(new File("E:/tmp/order_ds_0.xml"));
        
        List<File> cxFiles = new ArrayList<File>();
        cxFiles.add(new File("E:/tmp/EXPRESS_CP_REBATE_8888999904_2015-07-01_2015-07-31_1.xml"));
        
        BillSubTotal billSubTotal = new BillSubTotal();
        billSubTotal.addSpecialRebate(120.1);
        billSubTotal.setTotalDueAmt(11111111111111.11d);
        
        billSubTotal.setTotalRebateAmt(10d);
        
        BillParams billParams = new BillParams();
//        billParams.put("billHead", billHead);
//        billParams.put("billBottom", billBottom);
        billParams.put("order", xmlFiles);
        billParams.put("order_count", 310);
        billParams.put(BillParams.PARAM_DS_CX, cxFiles);
        billParams.put(BillParams.PARAM_DS_CX_COUNT, 2);
        
        
        billParams.put("cpt", sdFiles);
        billParams.put("cpt_count", 0);
        billParams.put("output", "E:/cx.xls");
        billParams.put(BillParams.PARAM_BILL_SUBTOTAL, billSubTotal);
        
        billParams.put(BillParams.PARAM_TMPL, "tmpl_xls_bill_standard");
        
        XlsBillWriter xlsBillWriter = new XlsBillWriter(billParams);
        xlsBillWriter.create();
    }*/
    
    public static void main(String[] args) {
//        test();   
    }

    /* (non-Javadoc)
     * @see com.ivy.freport.ds.IvyDSAccessListener#nextElement(java.lang.Object)
     */
    public abstract boolean nextElement(E t);

    protected List<HSSFCellStyle> getLoopRowStyles() {
        return loopRowStyles;
    }

    protected int getRowId() {
        return rowId;
    }
    
    protected void incItemIndex() {
        curItemIndex ++;
    }
}

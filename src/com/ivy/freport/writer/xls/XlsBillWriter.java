/* 
 * Copyright (c) 2015, S.F. Express Inc. All rights reserved.
 */

package com.ivy.freport.writer.xls;

import java.io.File;
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
import org.xml.sax.Attributes;

import com.ivy.freport.layout.Align;
import com.ivy.freport.layout.Cell;
import com.ivy.freport.layout.Layout;
import com.ivy.freport.layout.Row;
import com.ivy.freport.layout.Table;
import com.sun.xml.internal.ws.util.StringUtils;

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

public class XlsBillWriter implements XmlDSListener {
    
    private final Logger logger = Logger.getLogger(XlsBillWriter.class);
    
    public static final String PARAM_FILE_SEQ = "${file_seq}";
    
    private Layout layout = Layout.getInstance();  //布局类
    
    private Table table;
    private BillParams billParams;
    
    private List<Row> multiHeadRow;      //表格行头，保存，分文件使用
    private List<List<HSSFCellStyle>> multiHeadRowStyles;
    
    private Row loopRow;    //循环数据源，行定义
    private List<Cell> loopRowCells;
    private List<HSSFCellStyle> loopRowStyles;
    
    private int itemCount;        //订单记录数
    private int curOrder = 1;     //当前订单索引
    
    private HSSFWorkbook workbook;   //POI工作表
    private HSSFSheet sheet;         //POI工作表单
    
    private int rowId;               //POI行ID
    
    private String output;           //输出文件名称
    private int fileSeq = 1;         //输出文件序列
    
    private Map<String, HSSFColor> colors = new HashMap<String, HSSFColor>();   //表单颜色
    
    /**
     * constructor
     * @param billParams
     */
    public XlsBillWriter(BillParams billParams) {
        super();
        this.billParams = billParams;
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
        
        Row billRow = table.getRows().get(0);
        List<Cell> billCells = billRow.getCells();
        for (int i = 0; i < billCells.size(); i++) {
            Cell billCell = billCells.get(i);
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
     * 新增表格
     * @param tableId
     * @throws DocumentException
     */
    public boolean create() {
        boolean validate = validateParams(billParams);
        if (! validate) {
            return false;
        }
        
        String tmplName = (String) billParams.get(BillParams.PARAM_TMPL);
        Map<String, Table> tables = layout.layouts.get(tmplName);
        if (tables == null || tables.isEmpty()) {
            logger.error("not exist table layout["+ tmplName +"]");
            return false;
        } 
        
        output = (String) billParams.get("output");
        int lastIndex = output.lastIndexOf(".");
        if (lastIndex == -1) {
            output = output + "_" + PARAM_FILE_SEQ;
        }else {
            output = output.substring(0, lastIndex) + "_" 
                    + PARAM_FILE_SEQ + output.substring(lastIndex);
        }
        
        table = tables.values().iterator().next();
        
        createWorkbook();
        createSheet();
        
        List<Row> billRows = table.getRows();
        for (Row billRow : billRows) {
            createRow(billRow);            
        }
        flush();
        
        return true;
    }
    
    /**
     * 添加行
     * @param billRow
     * @throws DocumentException
     */
    private void createRow(Row billRow) {
        if (billRow == null) {
            return;
        }
        
        if (billRow.getShow() != null && 
           !PdfTmplParser.validate(billRow.getShow(), billParams)) {   //行不显示，返回
            return;
        }
        
        List<Cell> billCells = billRow.getCells();
        if (billCells == null || billCells.isEmpty()) {     //行无单元格定义，返回
            return;
        }
        
        int maxline = billRow.getMaxline();
        if (maxline > 0) {                //根据换行符\n多行显示
            createMutilRow(billRow);
        } else if(!StringUtils.isEmpty(billRow.getDs())){   //根据数据源循环多行显示
            String ds = billRow.getDs();
            if (billParams.get(ds) == null) {
                return;
            }
            createLoopRow(billRow);            
        } else {       //其他直接创建行显示
            String rowName = billRow.getName();
            List<HSSFCellStyle> headCellStyles = null;
            if (Row.HEAD.equals(rowName)) {
                saveHeadRow(billRow);
                headCellStyles = new ArrayList<HSSFCellStyle>();
                saveHeadRowCellStyles(headCellStyles);
            }
            
            HSSFRow hssfRow = null;
            if (rowId < sheet.getPhysicalNumberOfRows()) {
                hssfRow = sheet.getRow(rowId);
            } else {
                hssfRow = sheet.createRow(rowId);
            }
            hssfRow.setHeight((short)(billRow.getHeight()*20));
            for (int j = 0; j < billCells.size(); j++) {
                Cell billCell = billCells.get(j);
                HSSFCellStyle cellStyle = createCellStyle(billCell);
                if (Row.HEAD.equals(rowName)) {
                    headCellStyles.add(j, cellStyle);
                }
                
                createCell(hssfRow, billCell, cellStyle, null);
                if (billCell.getRowspan() > 1 || billCell.getColspan() > 1) {
                    addMegeredRegion(sheet, 
                                rowId, 
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
    private void createMutilRow(Row billRow) {
        List<Cell> billCells = billRow.getCells();
        int maxline = billRow.getMaxline();
        
        List<HSSFCellStyle> cellStyles = new ArrayList<HSSFCellStyle>(billCells.size());
        List<String[]> cellValues = new ArrayList<String[]>(billCells.size());
        for (int j = 0; j < billCells.size(); j++) {   //没列拆分多行值，创建样式
            Cell billCell = billCells.get(j);
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
                Cell billCell = billCells.get(j);
                
                int rowspan = billCell.getRowspan();
                int colspan = billCell.getColspan();
                if (rowspan > 1 || colspan > 1) {
                    addMegeredRegion(sheet, 
                                rowId, 
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
    private void saveHeadRow(Row billRow) {
        if (billRow == null) {
            return;
        }
        if (multiHeadRow == null) {
            multiHeadRow = new ArrayList<Row>();
        }
        multiHeadRow.add(billRow);
    }
    
    /**
     * 为分多文件行头 保存表格行样式
     * @param hssfCellStyles
     */
    private void saveHeadRowCellStyles(List<HSSFCellStyle> hssfCellStyles) {
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
    @SuppressWarnings("unchecked")
    private void createLoopRow(Row billRow) {
        this.loopRow = billRow;
        
        loopRowCells = billRow.getCells();
        
        loopRowStyles = new ArrayList<HSSFCellStyle>(loopRowCells.size());
        for (Cell billCell : loopRowCells) {
            loopRowStyles.add(createCellStyle(billCell));
        }
        
        String ds = billRow.getDs();
        Object datasource = billParams.get(ds);
        if (datasource == null) {
            return;
        }
        
        List<File> xmlFiles = null;
        if (datasource instanceof List) {
            xmlFiles = (List<File>) datasource;
        } else if (datasource instanceof File) {
            xmlFiles = new ArrayList<File>(1);
            xmlFiles.add((File) datasource);
        } else {
            throw new RuntimeException("data source type not supported!!!["+ datasource +"]");
        }
        
        itemCount = (int) billParams.get(ds + "_count");
        curOrder = 1;
        
        XmlDSParser xmlDSParser = new XmlDSParser(xmlFiles);
        xmlDSParser.addListener(this);
        xmlDSParser.parse();
        
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
    private void createCell(HSSFRow hssfRow, Cell billCell, HSSFCellStyle cellStyle, String value) {
        HSSFCell cell = hssfRow.createCell(billCell.getCellId());
        if (value == null) {
            value = billCell.getValue();
        }
        
        if (cellStyle == null) {
            cellStyle = createCellStyle(billCell);
        }
        cell.setCellStyle(cellStyle);
        
        if (billCell.isHasParam()) {
            Object obj = XlsTmplParser.fillParams(value, billParams);
            obj = obj == null ? "" : obj;
            
            if (billCell.getDataType() == EnumDataType.NUMBER 
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
    
    /**
     * 跨行跨列
     * @param sheet
     * @param firstRow
     * @param firstCol
     * @param colspan
     */
    private void addMegeredRegion(HSSFSheet sheet,
                            int firstRow,
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
    private HSSFCellStyle createCellStyle(Cell billCell) {
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
        /*if (billCell.getAlign() == Align.LEFT.getValue()) {
            hssfCellStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
        } else if (billCell.getAlign() == Align.CENTER.getValue()) {
            hssfCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        }else if (billCell.getAlign() == Align.RIGHT.getValue()) {
            hssfCellStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
        }*/
        
        if (billCell.getValign() == Align.MIDDLE.getValue()) {
            hssfCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        } else if (billCell.getValign() == Align.TOP.getValue()) {
            hssfCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
        }
        
        String bgColor = billCell.getBackgroundColor();
        if (!StringUtils.isEmpty(bgColor) && !"#ffffff".equals(bgColor)) {
            HSSFColor hssfColor = createColor(bgColor);
            hssfCellStyle.setFillForegroundColor(hssfColor.getIndex());
        }
        
        if (billCell.getDataType() == EnumDataType.NUMBER &&
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
    
    /* 
     * xml数据读取监听器
     * @see com.sf.ecbil.bill.xls.XmlDSListener#readNextElement(int, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public boolean readNextElement(int fileSeq,
                            String qName,
                            Attributes attributes) {

        HSSFRow hssfRow = sheet.createRow(rowId);
        hssfRow.setHeight((short)(loopRow.getHeight()*20));
        
        for (int i = 0; i < loopRowCells.size(); i++) {
            Cell billCell = loopRowCells.get(i);
            String value = attributes.getValue(billCell.getValue());
            if (value == null) value = "";
            
            HSSFCell cell = hssfRow.createCell(billCell.getCellId());
            cell.setCellStyle(loopRowStyles.get(i));
            if (billCell.getDataType() == EnumDataType.NUMBER) {
                if (StringUtils.isEmpty(value)) {
                    cell.setCellValue(value);
                } else {
                    cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                    cell.setCellValue(Double.parseDouble(value));
                }
            } else {
                cell.setCellValue(value.replaceAll("\\\\n", "\n"));
            }
            
            if (billCell.getColspan() > 0) {
                addMegeredRegion(sheet, 
                            rowId, 
                            billCell.getCellId(), 
                            0, 
                            billCell.getColspan(), 
                            billCell.getBorder());
            }
        }
        
        //大于10W记录分文件
        //大于5W分页
        if (curOrder % TmplConfig.getMaxXmlFileOrderSize() == 0 && curOrder < itemCount) {        
            flush();
            
            createWorkbook();
            createSheet();
//            addRow(headRow);
            
            rowId = 0;
            if (multiHeadRow != null) {
                for (int i=0; i<multiHeadRow.size(); i++) {
                    Row headRow = multiHeadRow.get(i);
                    List<HSSFCellStyle> rowCellStyles = multiHeadRowStyles.get(i);
                    
                    hssfRow = sheet.createRow(rowId);
                    hssfRow.setHeight((short)(headRow.getHeight()*20));
                    List<Cell> headCells = headRow.getCells();
                    if (headCells != null && !headCells.isEmpty()) {
                        for (int j = 0; j < headCells.size(); j++) {
                            Cell headCell = headCells.get(j);
                            createCell(hssfRow, headCell, rowCellStyles.get(j), null);
                        }
                        rowId ++;
                    }
                }
            }
        //恰好记录完结，则不重新创建页
        }else if (curOrder % TmplConfig.getMaxOrderSizePerSheet() == 0 
                && curOrder < itemCount) {        
            
            createSheet();
            rowId = 0;
        }else {
            rowId ++;
        }
        
        curOrder ++;
        
        return true;
    }
    
    /**
     * 创建颜色
     * @param bgColor #ff0000
     * @return
     */
    private HSSFColor createColor(String bgColor) {
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
    
    private static void test() {
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
        
        
        /*billParams.put("cpt", sdFiles);
        billParams.put("cpt_count", 0);*/
        billParams.put("output", "E:/cx.xls");
        billParams.put(BillParams.PARAM_BILL_SUBTOTAL, billSubTotal);
        
        billParams.put(BillParams.PARAM_TMPL, "tmpl_xls_bill_standard");
        
        XlsBillWriter xlsBillWriter = new XlsBillWriter(billParams);
        xlsBillWriter.create();
    }
    
    public static void main(String[] args) {
        test();   
    }
}

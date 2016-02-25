/* 
 * Copyright (c) 2015, S.F. Express Inc. All rights reserved.
 */

package com.ivy.freport.writer.xls;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import com.ivy.freport.layout.Config;
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
    
    private IvyDocDesc ivyDocDesc; 
    private Map<String, IvyDataSource<E>> dataSources;
    private Map<String, Object> otherAttrs;
    private String output;
    
    private int[] columnWidth;
    private List<XlsRowDesc> headRows;      //表格行头，保存，分文件使用
    private XlsRowDesc loopRow;
    
    private int itemCount;        //数据源记录数
    protected int curItemIndex = 0;     //当前数据源记录索引
    
    protected HSSFWorkbook workbook;   //POI工作表
    protected HSSFSheet sheet;         //POI工作表单
    protected int rowId;               //POI行ID
    
    private int fileSeq = 1;         //输出文件序列
    
    private Map<String, HSSFColor> xlsColorCache = new HashMap<String, HSSFColor>();   //表单颜色
    
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
        if (ivyDocDesc == null) {
            return true;
        }
        
        List<IvyTableDesc> ivyTableDescs = ivyDocDesc.getIvyTableDescs();
        if (ivyTableDescs == null || ivyTableDescs.size() == 0) {
            return true;
        }
        
        if (output == null || output.trim().equals("")) {
            output = UUID.randomUUID().toString() + ".xls";
        }
        
        if (Config.isXls_split_file()) {    //分文件
            int lastIndexDot = output.lastIndexOf(".");
            if (lastIndexDot == -1) {
                output = output + "_" + PARAM_FILE_SEQ;
            }else {
                output = output.substring(0, lastIndexDot) + "_" 
                        + PARAM_FILE_SEQ + output.substring(lastIndexDot);
            }
        }
        
        cacheColumnWidth(ivyDocDesc.getIvyTableDescs().get(0).getIvyRowDescs().get(0));
        
        createWorkbook();
        createSheet();
        
        for (int i = 0; i < ivyTableDescs.size(); i++) {
            IvyTableDesc ivyTableDesc = ivyTableDescs.get(i);
            List<IvyRowDesc> billRows = ivyTableDesc.getIvyRowDescs();
            for (IvyRowDesc billRow : billRows) {
                createRow(billRow);            
            }
        }
        
        flush();
        
        return true;
    }
    
    /**
     * 创建工作表
     */
    private void createWorkbook() {
        workbook = new HSSFWorkbook();
        
        if (headRows != null) {
            for (XlsRowDesc xlsRowDesc : headRows) {    //重新定义样式
                List<HSSFCellStyle> hssfCellStyles = xlsRowDesc.getCellStyles();
                if (hssfCellStyles != null && hssfCellStyles.size() > 0) {
                    for (int i = 0; i < hssfCellStyles.size(); i++) {
                        HSSFCellStyle cellStyle = workbook.createCellStyle();
                        cellStyle.cloneStyleFrom(hssfCellStyles.get(i));
                        hssfCellStyles.set(i, cellStyle);
                    }
                }
            }
        }
        
        if (loopRow != null) {
            List<HSSFCellStyle> hssfCellStyles = loopRow.getCellStyles();
            if (hssfCellStyles != null && hssfCellStyles.size() > 0) {
                for (int i = 0; i < hssfCellStyles.size(); i++) {
                    HSSFCellStyle cellStyle = workbook.createCellStyle();
                    cellStyle.cloneStyleFrom(hssfCellStyles.get(i));
                    hssfCellStyles.set(i, cellStyle);
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
        
        for (int i = 0; i < columnWidth.length; i++) {
            sheet.setColumnWidth(i, columnWidth[i]);
        }
    }
    
    private void cacheColumnWidth(IvyRowDesc ivyRowDesc) {
        List<IvyCellDesc> ivyCellDescs = ivyRowDesc.getIvyCellDescs();
        columnWidth = new int[ivyCellDescs.size()];
        Arrays.fill(columnWidth, 10*256);
        for (int j = 0; j < ivyCellDescs.size(); j++) {
            int k = ivyCellDescs.get(j).getWidth();
            if (k != 0) {
                columnWidth[j] = k*256;
            }
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
            HSSFRow hssfRow = null;
            if (rowId < sheet.getPhysicalNumberOfRows()) {
                hssfRow = sheet.getRow(rowId);    //跨行
            } else {
                hssfRow = sheet.createRow(rowId);
            }
            hssfRow.setHeight((short)(rowDesc.getHeight()*20));
            
            XlsRowDesc xlsRowDesc = null;
            if (IvyRowDesc.HEAD.equals(rowDesc.getName())) {   //表头
                xlsRowDesc = addHeadRow(rowDesc);
            }
            
            for (int j = 0; j < billCells.size(); j++) {
                IvyCellDesc billCell = billCells.get(j);
                
                HSSFCellStyle cellStyle = xlsRowDesc == null ? createCellStyle(billCell) : xlsRowDesc.getCellStyles().get(j);
                
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
    private XlsRowDesc addHeadRow(IvyRowDesc rowDesc) {
        return addHeadRow(rowDesc, null);
    }
    
    /**
     * 为分多文件行头 保存表格行样式
     * @param hssfCellStyles
     */
    private XlsRowDesc addHeadRow(IvyRowDesc rowDesc, List<HSSFCellStyle> cellStyles) {
        if (rowDesc != null) {
            if (headRows == null) {
                headRows = new ArrayList<XlsRowDesc>();
            }
            
            if (cellStyles == null) {
                cellStyles = createCellStyles(rowDesc.getIvyCellDescs());
            }
            
            XlsRowDesc xlsRowDesc = new XlsRowDesc(rowDesc, cellStyles);
            headRows.add(xlsRowDesc);
            return xlsRowDesc;
        }
        return null;
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
        
        this.loopRow = new XlsRowDesc(loopRowDesc, createCellStyles(loopRowDesc.getIvyCellDescs()));
        
        itemCount = datasource.size();
        curItemIndex = 0;
        
        datasource.addListener(this);
        datasource.next();
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
        if (Config.isXls_split_file() &&
                curItemIndex % (Config.getXls_wb_max_sheet() * Config.getXls_sheet_max_item()) == 0 && 
                curItemIndex < itemCount) {        
            flush();
            
            createWorkbook();
            createSheet();
//            addRow(headRow);
            
            rowId = 0;
            if (headRows != null) {
                for (int i=0; i<headRows.size(); i++) {
                    XlsRowDesc xlsRowDesc = headRows.get(i);
                    
                    HSSFRow hssfRow = sheet.createRow(rowId);
                    hssfRow.setHeight((short)(xlsRowDesc.getIvyRowDesc().getHeight()*20));
                    
                    List<IvyCellDesc> ivyCellDescs = xlsRowDesc.getIvyRowDesc().getIvyCellDescs();
                    List<HSSFCellStyle> hssfCellStyles = xlsRowDesc.getCellStyles();
                    if (ivyCellDescs != null && !ivyCellDescs.isEmpty()) {
                        for (int j = 0; j < ivyCellDescs.size(); j++) {
                            IvyCellDesc headCellDesc = ivyCellDescs.get(j);
                            createCell(hssfRow, headCellDesc, hssfCellStyles.get(j), null);
                        }
                        rowId ++;
                    }
                }
            }
        //恰好记录完结，则不重新创建页
        }else if (Config.getXls_wb_max_sheet() > 1 &&
                curItemIndex % Config.getXls_sheet_max_item() == 0 && 
                curItemIndex < itemCount) {        
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
     * 批量创建样式
     * @param ivyCellDescs
     * @return
     */
    protected List<HSSFCellStyle> createCellStyles(List<IvyCellDesc> ivyCellDescs) {
        if (ivyCellDescs == null || ivyCellDescs.size() == 0) {
            return null;
        }
        List<HSSFCellStyle> result = new ArrayList<HSSFCellStyle>(ivyCellDescs.size());
        for (IvyCellDesc ivyCellDesc : ivyCellDescs) {
            result.add(createCellStyle(ivyCellDesc));
        }
        return result;
    }
    
    /**
     * 创建样式
     * @param ivyCellDesc
     * @return
     */
    protected HSSFCellStyle createCellStyle(IvyCellDesc ivyCellDesc) {
        HSSFCellStyle hssfCellStyle = workbook.createCellStyle();
        hssfCellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);//设置前景填充样式
        hssfCellStyle.setFillForegroundColor(HSSFColor.WHITE.index);//前景填充色
        
        HSSFFont hssfFont = workbook.createFont();
        if (ivyCellDesc.isBold()) {
            hssfFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        }
        hssfFont.setFontHeightInPoints((short)ivyCellDesc.getSize());
        hssfCellStyle.setFont(hssfFont);
        
        hssfCellStyle.setAlignment((short)(ivyCellDesc.getAlign()+1));
        if (ivyCellDesc.getValign() == Align.MIDDLE.getValue()) {
            hssfCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        } else if (ivyCellDesc.getValign() == Align.TOP.getValue()) {
            hssfCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
        }
        
        String bgColor = ivyCellDesc.getBgColor();
        if (!StringUtils.isEmpty(bgColor) && !"#ffffff".equals(bgColor)) {
            HSSFColor hssfColor = createColor(bgColor);
            hssfCellStyle.setFillForegroundColor(hssfColor.getIndex());
        }
        
        if (ivyCellDesc.getDataType() == DataType.NUMBER &&
                !StringUtils.isEmpty(ivyCellDesc.getPattern())) {
            hssfCellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat(ivyCellDesc.getPattern()));
        }

        hssfCellStyle.setBorderLeft((short)ivyCellDesc.getBorder());
        hssfCellStyle.setBorderRight((short)ivyCellDesc.getBorder());
        hssfCellStyle.setBorderTop((short)ivyCellDesc.getBorder());
        hssfCellStyle.setBorderBottom((short)ivyCellDesc.getBorder());
        
        hssfCellStyle.setWrapText(Boolean.TRUE);
        
        return hssfCellStyle;
    }
    
    /**
     * 创建颜色
     * @param bgColor #ff0000
     * @return
     */
    private HSSFColor createColor(String bgColor) {
        HSSFColor hssfColor = null;
        
        if (xlsColorCache.containsKey(bgColor)) {
            hssfColor = xlsColorCache.get(bgColor);                
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
                    xlsColorCache.put(bgColor, hssfColor);
                }
            } catch (Exception e) {
                logger.error("", e);
                e.printStackTrace();
            }
        }
        
        return hssfColor;
    }
    
    /* (non-Javadoc)
     * @see com.ivy.freport.ds.IvyDSAccessListener#nextElement(java.lang.Object)
     */
    public abstract boolean nextElement(E t);

    
    public XlsRowDesc getLoopRow() {
        return loopRow;
    }

    protected int getRowId() {
        return rowId;
    }
    
    protected void incItemIndex() {
        curItemIndex ++;
    }
}

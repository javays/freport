/* 
 * Copyright (c) 2015, S.F. Express Inc. All rights reserved.
 */

package com.ivy.freport.writer.pdf;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import com.ivy.freport.ds.IvyDSAccessListener;
import com.ivy.freport.ds.IvyDataSource;
import com.ivy.freport.layout.DataType;
import com.ivy.freport.layout.IvyCellDesc;
import com.ivy.freport.layout.IvyDocDesc;
import com.ivy.freport.layout.IvyRowDesc;
import com.ivy.freport.layout.IvyTableDesc;
import com.ivy.freport.utils.StringUtils;
import com.ivy.freport.utils.TmplParser;


/**
 * 描述：
 * 
 * <pre>HISTORY
 * ****************************************************************************
 *  ID   DATE           PERSON          REASON
 *  1    2015年4月15日      Steven.Zhu         Create
 * ****************************************************************************
 * </pre>
 * @author Steven.Zhu
 * @since 
 */

public abstract class IvyPdfWriter<E> extends PdfPageEventHelper implements IvyDSAccessListener<E> {
    
    private static final Logger logger = Logger.getLogger(IvyPdfWriter.class);
    
    protected static final int MAX_FLUSH_ROW_NUM = 10000;
    
    private IvyDocDesc ivyDocDesc; 
    private Map<String, IvyDataSource<E>> dataSources;
    private Map<String, Object> otherAttrs;
    private String output;
    
    protected Document document;
    protected PdfWriter pdfWriter;
    protected PdfPTable curTable;
    
    protected IvyRowDesc ivyRowDesc;
    
    /**
     * @param ivyDocDesc
     * @param dataSources
     * @param otherAttrs
     * @param output
     */
    public IvyPdfWriter(IvyDocDesc ivyDocDesc,
            Map<String, IvyDataSource<E>> dataSources,
            Map<String, Object> otherAttrs, String output) {
        super();
        this.ivyDocDesc = ivyDocDesc;
        this.dataSources = dataSources;
        this.otherAttrs = otherAttrs;
        this.output = output;
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        Rectangle rect = writer.getBoxSize("rect");
        
        Phrase phrase = PdfParagraph.changeChinese("第 "+ writer.getPageNumber() +" 页.", 9, false);
        ColumnText.showTextAligned(writer.getDirectContent(),
                                Element.ALIGN_CENTER,
                                phrase,
                                (rect.getLeft() + rect.getRight()) / 2,
                                rect.getBottom() - 45,
                                0);
    }
    
    /**
     * 创建PDF账单
     */
    public boolean create() {
//        Rectangle rectPageSize = new Rectangle(PageSize.A4);   // 定义A4页面大小
        document = new Document(PageSize.A4, 5, 5, 15, 25);      // 其余4个参数，设置了页面的4个边距
        Rectangle rect = new Rectangle(36, 54, 559, 788);
        try {
            FileOutputStream fos = new FileOutputStream(output);
            pdfWriter = PdfWriter.getInstance(document, fos);   // 将PDF文档写出到out所关联IO设备上的书写对象
            document.addTitle("月结客户账单");    // 添加文档元数据信息
            document.addSubject("电商结算-月结客户账单");
            document.addAuthor("S.F. Express Inc.");
            document.addCreator("S.F. Express Inc.");
            document.addKeywords("账单");
            
            pdfWriter.setBoxSize("rect", rect);   
            pdfWriter.setPageEvent(this);  
//            Phrase beforePhrase = PdfParagraph.changeChinese("第 ", 10, false);
//            Phrase afterPhrase = PdfParagraph.changeChinese(" 页.", 10, false);
//            HeaderFooter footer = new HeaderFooter(beforePhrase, afterPhrase);
//            footer.setAlignment(Element.ALIGN_CENTER);
            
//            Header header = new Header("第 ", " xxx");
            
//            document..setFooter(header);
            
            // 定义页头和页尾
            /*PdfParagraph paragraph = new PdfParagraph("This is title", 20, true);
            HeaderFooter header = new HeaderFooter(paragraph, false);
            header.setAlignment(Element.ALIGN_CENTER);
            document.setHeader(header);*/
            
            /*HeaderFooter footer = new HeaderFooter(new Phrase(
                    "This   is   page   "), new Phrase("."));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.setFooter(footer);*/
            
            document.open();
            createTables();
            return true;
        } catch (DocumentException e) {
            logger.error("", e);
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            logger.error("", e);
            e.printStackTrace();
        } catch (IOException e) {
            logger.error("", e);
            e.printStackTrace();
        } finally {
            close();
        }
        
        return false;
    }
    
    /**
     * 根据布局文件生成表格
     * @throws IOException 
     * @throws MalformedURLException 
     * @throws DocumentException 
     */
    private void createTables() throws MalformedURLException, 
                                    IOException, 
                                    DocumentException {
        
        List<IvyTableDesc> ivyTableDescs = ivyDocDesc.getIvyTableDescs();
        Iterator<IvyTableDesc> iterator = ivyTableDescs.iterator();
        while (iterator.hasNext()) {
            IvyTableDesc table = iterator.next();
            
            List<IvyRowDesc> rowDescs = table.getIvyRowDescs();
            if (rowDescs == null || rowDescs.isEmpty()) {
                continue;
            }
            
            if (table.getShow() != null 
                    && !TmplParser.validate(table.getShow(), otherAttrs)) {
                continue;
            }
            
            int columnNum = 0;    //列数
            int maxCellNumRowIndex = 0;    //最大列数行
            for (int i = 0; i < rowDescs.size(); i++) {
                IvyRowDesc ivyRowDesc = rowDescs.get(i);
                int rowCellSize = ivyRowDesc.getIvyCellDescs().size();
                if (rowCellSize > columnNum) {
                    columnNum = rowCellSize;
                    maxCellNumRowIndex = i;
                }
            }
            
            int[] widths = new int[columnNum];
            curTable = new PdfPTable(columnNum);
            curTable.setComplete(false);
            curTable.setWidthPercentage(table.getWidth());
            curTable.setHorizontalAlignment(table.getAlign());
            
            for (int i = 0; i < rowDescs.size(); i++) {
                IvyRowDesc row = rowDescs.get(i);
                if (row.getShow() != null 
                        && !TmplParser.validate(row.getShow(), otherAttrs)) {
                    continue;
                }
                
                List<IvyCellDesc> ivyCellDescs = row.getIvyCellDescs();
                if (i == maxCellNumRowIndex) {
                    for (int j = 0; j < ivyCellDescs.size(); j++) {
                        IvyCellDesc billCell = ivyCellDescs.get(j);
                        widths[j] = billCell.getWidth();
                    }
                }
                
                if (!StringUtils.isEmpty(row.getDs())) {
                    this.ivyRowDesc = row;
                    IvyDataSource<E> ivyDataSource = dataSources.get(row.getDs());
                    ivyDataSource.addListener(this);
                    ivyDataSource.next();
                    continue;
                }
                
                for (IvyCellDesc ivyCellDesc : ivyCellDescs) {
                    PdfPCell cell = createCell(ivyCellDesc);
                    if (ivyCellDesc.getColspan() != 0) {
                        cell.setColspan(ivyCellDesc.getColspan());
                    }
                    curTable.addCell(cell);
                }
            }
            
            curTable.setWidths(widths);
            
            curTable.setComplete(true);
            document.add(curTable);
            
            if (table.isBr()) {
                createBlankLine();
            }
        }
    }
    
    /**
     * 创建表格列
     * @param billCell
     * @return
     * @throws BadElementException
     * @throws MalformedURLException
     * @throws IOException
     */
    private PdfPCell createCell(IvyCellDesc cellDesc) 
            throws BadElementException,
                MalformedURLException,
                IOException {
        
        boolean img = cellDesc.isImg();
        String cellValue = cellDesc.getValue();
        
        PdfPCell cell = null;
        if (img) {
            Image image = Image.getInstance(cellDesc.getPicBuffer());
            image.scaleAbsolute(cellDesc.getImgWidth(), cellDesc.getImgHeight());
            cell = new PdfPCell(image);
        } else {
            Object value = null;
            if (cellDesc.isHasParam()) {   //替换参数
                value = TmplParser.fillParams(cellValue, otherAttrs);
                value = value == null ? "" : value;
            } 
            
            if (value != null) {
                if (DataType.NUMBER == cellDesc.getDataType() 
                    && value instanceof Number) {
                    String pattern = cellDesc.getPattern();
                    DecimalFormat df = new DecimalFormat(pattern);  
                    cellValue = df.format(value);
                } else {
                    cellValue = (String) value;
                }
            } 
            
            int newLineIndex = cellValue.indexOf("\\n");
            if (newLineIndex != -1) {
                String[] tmp = cellValue.split("\\\\n");
                cellValue = String.join("\n", tmp);
            }
            
            PdfParagraph paragraph = new PdfParagraph(cellValue, cellDesc.getSize(), cellDesc.isBold());
            cell = new PdfPCell(paragraph);
        }
        
        cell.setHorizontalAlignment(cellDesc.getAlign());
        cell.setVerticalAlignment(cellDesc.getValign());
        
        String bgColor = cellDesc.getBgColor();
        BaseColor baseColor = new BaseColor(Integer.valueOf(bgColor.substring( 1, 3 ), 16 ),
        Integer.valueOf(bgColor.substring( 3, 5 ), 16 ),
        Integer.valueOf(bgColor.substring( 5, 7 ), 16 ));
        cell.setBackgroundColor(baseColor);
        
        String borderColor = cellDesc.getBorderColor();
        BaseColor bdColor = new BaseColor(Integer.valueOf(borderColor.substring( 1, 3 ), 16 ),
                Integer.valueOf(borderColor.substring( 3, 5 ), 16 ),
                Integer.valueOf(borderColor.substring( 5, 7 ), 16 ));
                cell.setBackgroundColor(baseColor);
        cell.setBorderColor(bdColor);
        cell.setFixedHeight(cellDesc.getHeight());
        cell.setPadding(0);
        
        return cell;
    }
    
    public void createBlankLine() throws DocumentException {
        PdfParagraph paragraph = new PdfParagraph("\n");
        document.add(paragraph);
    }
    
    /**
     * 关闭文件
     */
    private void close() {
        try {
            if (pdfWriter != null && !pdfWriter.isCloseStream()) {
                pdfWriter.close();
            }
            if (document != null) {
                document.close();   
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("", e);
        }
    }
    
    /*private static boolean testSDBill() {
        BillHead billHead = DataCreate.getBillHead();
        BillBottom billBottom = DataCreate.getBillBottom();
        
        List<File> xmlFiles = new ArrayList<File>();
        xmlFiles.add(new File("E:/order_datasource1.xml"));
        
        List<File> sdFiles = new ArrayList<File>();
        sdFiles.add(new File("E:/sd.xml"));
        
        List<File> cptFiles = new ArrayList<File>();
        cptFiles.add(new File("E:/cpts.xml"));
        
        BillSubTotal billSubTotal = new BillSubTotal();
        billSubTotal.addSpecialRebate(120.1);
        
        BillParams billParams = new BillParams();
        billParams.put(BillParams.PARAM_TMPL, "tmpl_pdf_bill_standard_sd");
        billParams.put(BillParams.PARAM_DS_ORDER, xmlFiles);
        billParams.put(BillParams.PARAM_DS_ORDER_COUNT, 5);
        billParams.put(BillParams.PARAM_OUTPUT, "E:/SDBill.pdf");
        billParams.put(BillParams.PARAM_BILL_HEAD, billHead);
        billParams.put(BillParams.PARAM_BILL_BOTTOM, billBottom);
        
        billParams.put(BillParams.PARAM_DS_SD, sdFiles);
        billParams.put(BillParams.PARAM_DS_SD_COUNT, 5);  
        
        billParams.put(BillParams.PARAM_DS_CPT, cptFiles);
        billParams.put(BillParams.PARAM_DS_CPT_COUNT, 5);   
        
        billParams.put(BillParams.PARAM_BILL_SUBTOTAL, billSubTotal);
        
        
        BillPdfWriter billPdfWriter = new BillPdfWriter(billParams);
        return billPdfWriter.create();
    }*/
    
    /*private static boolean testStBill() {
        BillHead billHead = DataCreate.getBillHead();
//        billHead.setCustomerEnName("111");
        BillBottom billBottom = DataCreate.getBillBottom();
        
        List<File> xmlFiles = new ArrayList<File>();
        xmlFiles.add(new File("E:/tmp/order_datasource1.xml"));
//        xmlFiles.add(new File("E:/tmp/0206116146_2015-05-01_2015-05-31_order_V1_1.xml"));
//        xmlFiles.add(new File("E:/tmp/0206116146_2015-05-01_2015-05-31_order_V1_2.xml"));
//        xmlFiles.add(new File("E:/tmp/0206116146_2015-05-01_2015-05-31_order_V1_3.xml"));
        
        List<File> sdFiles = new ArrayList<File>();
        sdFiles.add(new File("E:/tmp/sd.xml"));
        
        List<File> cptFiles = new ArrayList<File>();
        cptFiles.add(new File("E:/tmp/cpts.xml"));
        
        BillSubTotal billSubTotal = new BillSubTotal();
        billSubTotal.addSpecialRebate(120.1);
        billSubTotal.setTotalDueAmt(11111111111111.11d);
//        billSubTotal.setTotalRebateAmt(12d);
        
        BillParams billParams = new BillParams();
        billParams.put(BillParams.PARAM_TMPL, "tmpl_pdf_bill_standard_sd");
        billParams.put(BillParams.PARAM_DS_ORDER, xmlFiles);
        billParams.put(BillParams.PARAM_DS_ORDER_COUNT, 5);
        billParams.put(BillParams.PARAM_OUTPUT, "E:/StBill.pdf");
        billParams.put(BillParams.PARAM_BILL_HEAD, billHead);
        billParams.put(BillParams.PARAM_BILL_BOTTOM, billBottom);
        
//        billParams.put(BillParams.PARAM_DS_SD, sdFiles);
//        billParams.put(BillParams.PARAM_DS_SD_COUNT, 10);
        
        billParams.put(BillParams.PARAM_DS_CPT, cptFiles);
        billParams.put(BillParams.PARAM_DS_CPT_COUNT, 5);   
        
        billParams.put(BillParams.PARAM_BILL_SUBTOTAL, billSubTotal);
        
        BillPdfWriter billPdfWriter = new BillPdfWriter(billParams);
        return billPdfWriter.create();
    }
    
    public static void main(String[] args) throws DocumentException {
//        testSDBill();
        testStBill();
    }*/
}

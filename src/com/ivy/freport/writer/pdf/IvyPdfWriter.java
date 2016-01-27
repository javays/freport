/* 
 * Copyright (c) 2015, S.F. Express Inc. All rights reserved.
 */

package com.ivy.freport.writer.pdf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.ivy.freport.ds.IvyDataSource;
import com.ivy.freport.ds.IvyXmlDataSource;
import com.ivy.freport.layout.Row;
import com.ivy.freport.layout.Table;
import com.ivy.freport.writer.xls.BillParams;
import com.ivy.freport.writer.xls.XmlDSListener;
import com.ivy.freport.writer.xls.XmlDSParser;

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

public class IvyPdfWriter implements XmlDSListener {
    
    private static final Logger logger = Logger.getLogger(IvyPdfWriter.class);
    
    private static final int MAX_FLUSH_ROW_NUM = 10000;
    
    private Document document;
    private PdfWriter pdfWriter;
    private PdfPTable curTable;
    private Map<String, Table> layout;
    
    private Row row;
    
    private int count;
    
    /**
     * 创建PDF账单
     */
    public boolean create(String tmpl) {
        
        IvyXmlDataSource ivyXmlDataSource = new IvyXmlDataSource((List)null, t -> {
            t.getIndex("");
            document.get
        }, null);
        
        String tmpl = (String) billParams.get(BillParams.PARAM_TMPL);
        layout = BillLayout.getInstance().billLayouts.get(tmpl);
        String output = (String) billParams.get(BillParams.PARAM_OUTPUT);
        
//        Rectangle rectPageSize = new Rectangle(PageSize.A4);   // 定义A4页面大小
//        document = new Document(rectPageSize, 5, 5, 15, 15);// 其余4个参数，设置了页面的4个边距
        document = new Document(PageSize.A4, 5, 5, 15, 25);
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
            HeaderFooter header = new HeaderFooter();   
            pdfWriter.setPageEvent(header);  
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
    @SuppressWarnings("unchecked")
    private void createTables() throws MalformedURLException, 
                                    IOException, 
                                    DocumentException {
        Iterator<Table> iterator = layout.values().iterator();
        while (iterator.hasNext()) {
            Table table = iterator.next();
            if (table.getShow() != null && !PdfTmplParser.validate(table.getShow(), billParams)) {
                continue;
            }
            
            List<Row> rows = table.getRows();
            if (rows == null || rows.isEmpty()) {
                continue;
            }
            
            int column = 0;
            int maxCellRow = 0;
            for (int i = 0; i < rows.size(); i++) {
                Row row = rows.get(i);
                if (row.getCells().size() > column) {
                    column = row.getCells().size();
                    maxCellRow = i;
                }
            }
            
            int[] widths = new int[column];
            curTable = new PdfPTable(column);
            curTable.setComplete(false);
            curTable.setWidthPercentage(table.getWidth());
            curTable.setHorizontalAlignment(table.getAlign());
            
            for (int i = 0; i < rows.size(); i++) {
                Row row = rows.get(i);
                if (row.getShow() != null && !PdfTmplParser.validate(row.getShow(), billParams)) {
                    continue;
                }
                
                List<Cell> billCells = row.getCells();
                if (i == maxCellRow) {
                    for (int j = 0; j < billCells.size(); j++) {
                        Cell billCell = billCells.get(j);
                        widths[j] = billCell.getWidth();
                    }
                }
                
                if (row.getDs() != null && !row.getDs().equals("")) {
                    this.row = row;
                    count = 0;
                    List<File> xmlFiles = (List<File>) billParams.get(row.getDs());
                    XmlDSParser xmlDSParser = new XmlDSParser(xmlFiles);
                    xmlDSParser.addListener(this);
                    xmlDSParser.parse();
                    continue;
                }
                
                for (Cell billCell : billCells) {
                    PdfPCell cell = createCell(billCell);
                    if (billCell.getColspan() != 0) {
                        cell.setColspan(billCell.getColspan());
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
    private PdfPCell createCell(Cell billCell) throws BadElementException,
            MalformedURLException,
            IOException {
        
        boolean img = billCell.isImg();
        String cellValue = billCell.getValue();
        
        PdfPCell cell = null;
        if (img) {
            Image image = Image.getInstance(billCell.getPicBuffer());
            image.scaleAbsolute(billCell.getImgWidth(), billCell.getImgHeight());
            cell = new PdfPCell(image);
        } else {
            Object value = null;
            if (billCell.isHasParam()) {   //替换参数
                value = PdfTmplParser.fillParams(cellValue, billParams);
                value = value == null ? "" : value;
            } 
            
            if (value != null) {
                if (EnumDataType.NUMBER == billCell.getDataType() 
                    && value instanceof Number) {
                    String pattern = billCell.getPattern();
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
            
            PdfParagraph paragraph = new PdfParagraph(cellValue, billCell.getSize(), billCell.isBold());
            cell = new PdfPCell(paragraph);
        }
        
        cell.setHorizontalAlignment(billCell.getAlign());
        cell.setVerticalAlignment(billCell.getValign());
        
        String bgColor = billCell.getBackgroundColor();
        BaseColor baseColor = new BaseColor(Integer.valueOf(bgColor.substring( 1, 3 ), 16 ),
        Integer.valueOf(bgColor.substring( 3, 5 ), 16 ),
        Integer.valueOf(bgColor.substring( 5, 7 ), 16 ));
        cell.setBackgroundColor(baseColor);
        
        String borderColor = billCell.getBorderColor();
        BaseColor bdColor = new BaseColor(Integer.valueOf(borderColor.substring( 1, 3 ), 16 ),
                Integer.valueOf(borderColor.substring( 3, 5 ), 16 ),
                Integer.valueOf(borderColor.substring( 5, 7 ), 16 ));
                cell.setBackgroundColor(baseColor);
        cell.setBorderColor(bdColor);
        cell.setFixedHeight(billCell.getHeight());
        cell.setPadding(0);
        
        return cell;
    }
    
    public void createBlankLine() throws DocumentException {
        PdfParagraph paragraph = new PdfParagraph("\n");
        document.add(paragraph);
    }
    
    /* (non-Javadoc)
     * @see com.sf.ecbil.bill.xls.XmlDSListener#readNextElement(int, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public boolean readNextElement(int fileSeq,
                            String qName,
                            Attributes attributes) {
        
        for (Cell billCell : row.getCells()) {
            String value = attributes.getValue(billCell.getValue());
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
        
        return true;
    }

    public static String getChinese(String s) {
        try {
           return new String(s.getBytes("gb2312"), "iso-8859-1");
        } catch (UnsupportedEncodingException e) {
            logger.error("", e);
            return s;
        }
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

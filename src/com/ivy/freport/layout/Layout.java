/* 
 * Copyright (c) 2015, S.F. Express Inc. All rights reserved.
 */

package com.ivy.freport.layout;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.ivy.freport.utils.StringUtils;

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

public class Layout {
    
    private static final Logger logger = Logger.getLogger(Layout.class);
    
    public static final String BILL_LAYOUT_ROOT = "META-INF/config/ecbil.bill/tmpl/";
    
    private static Layout layout = new Layout();
    public Map<String, Map<String, Table>> layouts = new HashMap<String, Map<String,Table>>();
    
    private int[] rowspans = null;
    
    private Layout() {
        super();
        init();
    }
    
    public static void reload() {
        layout.init();
    }
    
    @SuppressWarnings("unchecked")
    private void init() {
        /*String[] configs = new String[] {
                "META-INF/config/ecbil.bill/tmpl/tmpl_pdf_bill_standard_layout.xml",
                "META-INF/config/ecbil.bill/tmpl/tmpl_pdf_bill_standard_sd_layout.xml",
                "META-INF/config/ecbil.bill/tmpl/tmpl_pdf_bill_sto_layout.xml",
                "META-INF/config/ecbil.bill/tmpl/tmpl_xls_bill_standard_layout.xml"
                };*/
        
        File tmplRoot = new File(FileUtils.getFilePath(BILL_LAYOUT_ROOT));
        if (!tmplRoot.exists()) {
            throw new RuntimeException("bill layout tmpl path "+ BILL_LAYOUT_ROOT +" may moved away!!!");
        }
        
        String[] configs = tmplRoot.list(new FilenameFilter() {
            
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith("layout.xml");
            }
        });

        Document document = null;
        InputStream is = null;
        try {
            SAXReader saxReader = new SAXReader();
            for (String config : configs) {
                /*is = Thread
                        .currentThread()
                        .getContextClassLoader()
                        .getResourceAsStream(config);*/
                is = new FileInputStream(new File(tmplRoot,config));

                document = saxReader.read(is);
                Element root = document.getRootElement();

                Map<String, Table> layouts = new LinkedHashMap<String, Table>();
                String name = root.attributeValue("name");
                layouts.put(name, layouts);

                Iterator<Element> rootIter = root.elementIterator();
                while (rootIter.hasNext()) {
                    Table table = new Table();

                    Element tableNode = rootIter.next();
                    table.setId(tableNode.attributeValue("id"));

                    String width = tableNode.attributeValue("width");
                    if (!StringUtils.isEmpty(width)) {
                        table.setWidth(Integer.parseInt(width.substring(
                                0,
                                width.length() - 1)));
                    }

                    String align = tableNode.attributeValue("align");
                    if (!StringUtils.isEmpty(align)) {
                        table.setAlign(Align.getValue(align));
                    }
                    
                    String br = tableNode.attributeValue("br");
                    if (!StringUtils.isEmpty(br)) {
                        table.setBr(Boolean.parseBoolean(br));
                    }
                    
                    String tableShow = tableNode.attributeValue("show");
                    if (!StringUtils.isEmpty(tableShow)) {
                        table.setShow(tableShow);
                    }

                    List<Element> rowNodes = tableNode.elements("row");
                    for (Element rowNode : rowNodes) {
                        Row billRow = new Row();
                        
                        String _name = rowNode.attributeValue("name");
                        if (!StringUtils.isEmpty(_name)) {
                            billRow.setName(_name);
                        }
                        
                        String height = rowNode.attributeValue("height");
                        if (!StringUtils.isEmpty(height)) {
                            billRow.setHeight(Short.parseShort(height));
                        }
                        
                        String maxline = rowNode.attributeValue("maxline");
                        if (!StringUtils.isEmpty(maxline)) {
                            billRow.setMaxline(Integer.parseInt(maxline));
                        }
                        
                        String show = rowNode.attributeValue("show");
                        if (!StringUtils.isEmpty(show)) {
                            billRow.setShow(show);
                        }
                        
                        String ds = rowNode.attributeValue("ds");
                        if (!StringUtils.isEmpty(ds)) {
                            billRow.setDs(ds);
                        }
                        
                        billRow.setCells(getCellLayouts(rowNode));
                        
                        table.addRow(billRow);
                    }
                    
                    layouts.put(table.getId(), table);
                }

                is.close();
                document.clearContent();
            }
        } catch (DocumentException e) {
            logger.error("", e);
            e.printStackTrace();
        } catch (IOException e) {
            logger.error("", e);
            e.printStackTrace();
        }
    }
    
    @SuppressWarnings("unchecked")
    private List<Cell> getCellLayouts(Element parentElement) throws IOException {
        if (parentElement == null) {
            return null;
        }
        
        List<Element> cellElements = parentElement.elements("cell");
        List<Cell> result = new ArrayList<Cell>(cellElements.size());
        
        boolean b_rowspan = false;
        for (int i = 0; i < cellElements.size(); i++) {
            Element element = cellElements.get(i);
            
            Cell billCell = new Cell();
            
            if (i > 0) {
                Cell _billCell = result.get(i-1);
                int lastColSpan = _billCell.getColspan();
                if (lastColSpan > 1) {
                    billCell.setCellId(_billCell.getCellId() + _billCell.getColspan());
                }else {
                    billCell.setCellId(_billCell.getCellId() + 1);
                }
            } else {
                billCell.setCellId(0);
            }
            
            if (rowspans != null && rowspans.length > 0) {
                int preCellRowspan = 0;
                if (i < rowspans.length) {
                    preCellRowspan = rowspans[i];
                }
                
                if (preCellRowspan > 1) {
                    int index = findFirstZeroInArray(i);
                    
                    if (index != -1) {
                        billCell.setCellId(billCell.getCellId() + index);
                        updateArrayValue(i, index, -1);
                    }
                } else {
                    if (i > 0) {
                        Cell preCell = result.get(i-1);
                        billCell.setCellId(preCell.getCellId() + 1);
                    } else {
                        billCell.setCellId(0);
                    }
                }
            }
            

            String value = element.attributeValue("value");
            if (StringUtils.isEmpty(value)) {
                value = element.getText().trim();
            }
            billCell.setValue(value);
            
            String dataType = element.attributeValue("dataType");
            if (!StringUtils.isEmpty(dataType)) {
                billCell.setDataType(DataType.parse(dataType));
                
                String pattern = element.attributeValue("pattern");
                billCell.setPattern(pattern);
            }
            
            
            String hasParam = element.attributeValue("hasParam");
            if (!StringUtils.isEmpty(hasParam)) {
                billCell.setHasParam(Boolean.parseBoolean(hasParam));
            }
            
            String width = element.attributeValue("width");
            if (!StringUtils.isEmpty(width)) {
                billCell.setWidth(Integer.parseInt(width));
            }
            
            String height = element.attributeValue("height");
            if (!StringUtils.isEmpty(height)) {
                billCell.setHeight(Integer.parseInt(height));
            }
            
            String rowspan = element.attributeValue("rowspan");
            if (!StringUtils.isEmpty(rowspan)) {
                int i_rowspan = Integer.parseInt(rowspan);
                if (i_rowspan > 1) {       //记录跨行记录
                    billCell.setRowspan(i_rowspan);
                    b_rowspan = true;
                }
            }
            
            String colspan = element.attributeValue("colspan");
            if (!StringUtils.isEmpty(colspan)) {
                billCell.setColspan(Integer.parseInt(colspan));
            }
            
            String bold = element.attributeValue("bold");
            if (!StringUtils.isEmpty(bold)) {
                billCell.setBold(Boolean.parseBoolean(bold));
            }
            
            String border = element.attributeValue("border");
            if (!StringUtils.isEmpty(border)) {
                billCell.setBorder(Integer.parseInt(border));
            }
            
            String borderColor = element.attributeValue("borderColor");
            if (!StringUtils.isEmpty(borderColor)) {
                billCell.setBorderColor(borderColor);
            }
            
            String backgroundColor = element.attributeValue("backgroundColor");
            if (!StringUtils.isEmpty(backgroundColor)) {
                billCell.setBgColor(backgroundColor);
            }
            
            String size = element.attributeValue("size");
            if (!StringUtils.isEmpty(size)) {
                billCell.setSize(Integer.parseInt(size));
            }
            
            String align = element.attributeValue("align");
            if (!StringUtils.isEmpty(align)) {
                billCell.setAlign(Align.getValue(align));
            }
            
            String valign = element.attributeValue("valign");
            if (!StringUtils.isEmpty(valign)) {
                billCell.setValign(Align.getValue(valign));;
            }
            
            String img = element.attributeValue("img");
            if (!StringUtils.isEmpty(img)) {
                billCell.setImg(Boolean.parseBoolean(img));
                
                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(value);
                BufferedInputStream bis = new BufferedInputStream(is);
                byte[] buf = new byte[bis.available()];
                bis.read(buf, 0, bis.available());
                billCell.setPicBuffer(buf);
                
                bis.close();
                is.close();
                
                String imgHeight = element.attributeValue("imgHeight");
                if (!StringUtils.isEmpty(imgHeight)) {
                    billCell.setImgHeight(Float.parseFloat(imgHeight));;
                }
                String imgWidth = element.attributeValue("imgWidth");
                if (!StringUtils.isEmpty(imgWidth)) {
                    billCell.setImgWidth(Float.parseFloat(imgWidth));;
                }
            }
            
            result.add(billCell);
        }
        
        if (b_rowspan) {
            fillRowSpans(result);
            b_rowspan = false;
        }
        
        if (rowspans != null && rowspans.length > 0) {
            checkArray(cellElements.size());            
        }
        
        return result;
    }
    
    private void fillRowSpans(List<Cell> billCells) {
        int size = 0;
        for (Cell billCell : billCells) {
            size += billCell.getColspan() == 0 ? 1 : billCell.getColspan();
        }
        
        if (rowspans == null) {
            rowspans = new int[size];
            Arrays.fill(rowspans, 0);
        } else if (rowspans.length < size) {
            int[] tmp = rowspans;
            rowspans = new int[size];
            Arrays.fill(rowspans, 0);
            System.arraycopy(tmp, 0, rowspans, 0, tmp.length);
        } 
        
        int rowspan_index = 0;
        for (int i = 0; i < billCells.size(); i++) {
            Cell billCell = billCells.get(i);
            
            int rowspan = billCell.getRowspan();
            int cospan = billCell.getColspan();
            if (cospan > 1) {
                for (int j = 0; j < cospan; j++) {
                    if (rowspan > 1) {
                        rowspans[rowspan_index] = rowspans[rowspan_index] + billCell.getRowspan();
                    }
                    rowspan_index ++;
                }
            } else {
                if (rowspan > 1) {
                    rowspans[rowspan_index] = rowspans[rowspan_index] + billCell.getRowspan();
                }
                
                rowspan_index ++;
            }
        }
    }
    
    private int findFirstZeroInArray(int start) {
        if (rowspans == null 
                || rowspans.length == 0 
                || start < 0 
                || start > rowspans.length-1) {
            return -1;
        }
        
        for (int i = start; i < rowspans.length; i++) {
            int rowspan = rowspans[i];
            if (rowspan == 0) {
                return i;
            }
        }
        
        return -1;
    }
    
    private void updateArrayValue(int start, int end, int plus) {
        if (rowspans == null 
                || rowspans.length == 0 
                || start < 0 
                || start > rowspans.length-1
                || plus == 0) {
            return;
        }
        end = end > rowspans.length ? rowspans.length : end;
        for (int i = start; i < end; i++) {
            int rowspan = rowspans[i];
            if (rowspan > 0) {
                rowspans[i] = rowspans[i] + plus;
            }
        }
    }
    
    private void checkArray(int ignoreFrom) {
        if (rowspans != null && rowspans.length > 0) {
            updateArrayValue(ignoreFrom, rowspans.length, -1);
            boolean delete = true;
            for (int rowspan : rowspans) {
                if(rowspan > 1) {
                    delete = false;
                }
            }
            if (delete) {
                rowspans = null;
            }
        }
    }
    
    public static Layout getInstance() {
        if (Config.isDevEnv()) {
            layout.layouts = new HashMap<String, Map<String,Table>>();
            Layout.reload();
        }

        return layout;
    }
    
    public static void main(String[] args) {
        getInstance();
    }
}

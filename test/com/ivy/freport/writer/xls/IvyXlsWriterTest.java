/* 
 * Copyright (c) 2016, S.F. Express Inc. All rights reserved.
 */

package com.ivy.freport.writer.xls;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;

import com.ivy.freport.ds.IvyDataSource;
import com.ivy.freport.ds.IvyXmlDataSource;
import com.ivy.freport.layout.IvyDocDesc;
import com.ivy.freport.layout.IvyLayout;
import com.ivy.freport.service.BillBottom;
import com.ivy.freport.service.BillHead;
import com.ivy.freport.service.BillSubTotal;
import com.ivy.freport.service.DataCreate;
import com.ivy.freport.writer.pdf.IvyXmlAttrDsPdfWriter;

/**
 * 描述：
 * 
 * <pre>
 * HISTORY
 * ****************************************************************************
 *  ID   DATE           PERSON          REASON
 *  1    2016年1月29日      Steven.Zhu         Create
 * ****************************************************************************
 * </pre>
 * 
 * @author Steven.Zhu
 * @since
 */

public class IvyXlsWriterTest {

    public static void test() {
        List<File> xmlFiles = new ArrayList<File>();
        xmlFiles.add(new File("E:/tmp/order_datasource1.xml"));
        
        List<String> skipElementsName = new ArrayList<String>();
        skipElementsName.add("items");
        IvyDataSource<Attributes> dataSources = new IvyXmlDataSource(xmlFiles, 10, skipElementsName);
        
        List<File> cptFiles = new ArrayList<File>();
        cptFiles.add(new File("E:/tmp/cpts.xml"));
        IvyDataSource<Attributes> cpDataSources = new IvyXmlDataSource(xmlFiles, 10, skipElementsName);
        
        Map<String, IvyDataSource<Attributes>> ds = new HashMap<String, IvyDataSource<Attributes>>();
        ds.put("order", dataSources);
        ds.put("cpt", cpDataSources);
        
        
        Map<String, IvyDocDesc> ivyDocDescs = IvyLayout.getInstance().docDescs;
        IvyDocDesc ivyDocDesc = ivyDocDescs.get("tmpl_xls_bill_standard");
        
        BillHead billHead = DataCreate.getBillHead();
        BillBottom billBottom = DataCreate.getBillBottom();
        
        
        BillSubTotal billSubTotal = new BillSubTotal();
        billSubTotal.addSpecialRebate(120.1);
        
        Map<String, Object> otherAttrs = new HashMap<String, Object>();
        
        otherAttrs.put("order_count", 5);
        otherAttrs.put("billHead", billHead);
        otherAttrs.put("billBottom", billBottom);
        otherAttrs.put("cpt_count", 5);   
        otherAttrs.put("billSubTotal", billSubTotal);
        
        String output = "E:/SDBill.xls";
        
        IvyXmlAttrDsXlsWriter ivyXmlAttrDsXlsWriter = new IvyXmlAttrDsXlsWriter(ivyDocDesc, ds, otherAttrs, output);
        ivyXmlAttrDsXlsWriter.create();
    }

    public static void main(String[] args) {
         test();
    }
}

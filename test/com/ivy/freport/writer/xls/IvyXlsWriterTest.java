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
        long now = System.currentTimeMillis();
        
        List<File> xmlFiles = new ArrayList<File>();
//        xmlFiles.add(new File("E:/tmp/order_datasource1.xml"));
        
//        xmlFiles.add(new File("E:/tmp/order_ds_0.xml"));
//        xmlFiles.add(new File("E:/tmp/order_ds_1.xml"));
        
        int itemNum = 200000;
        
        List<String> skipElementsName = new ArrayList<String>();
        skipElementsName.add("items");
        IvyDataSource<Attributes> dataSources = new IvyXmlDataSource(xmlFiles, itemNum, skipElementsName);
        
        List<File> cptFiles = new ArrayList<File>();
        cptFiles.add(new File("E:/tmp/cpts.xml"));
        IvyDataSource<Attributes> cpDataSources = new IvyXmlDataSource(cptFiles, 10, skipElementsName);
        
        Map<String, IvyDataSource<Attributes>> ds = new HashMap<String, IvyDataSource<Attributes>>();
        ds.put("order", dataSources);
        ds.put("cpt", cpDataSources);
        
        
        Map<String, IvyDocDesc> ivyDocDescs = IvyLayout.getInstance().docDescs;
        IvyDocDesc ivyDocDesc = ivyDocDescs.get("tmpl_xls_bill_bbil_cod_service");
        
        BillHead billHead = DataCreate.getBillHead();
        BillBottom billBottom = DataCreate.getBillBottom();
        
        
        BillSubTotal billSubTotal = new BillSubTotal();
        billSubTotal.addSpecialRebate(120.1);
        
        Map<String, Object> otherAttrs = new HashMap<String, Object>();
        
        otherAttrs.put("order_count", 100000);
        otherAttrs.put("billHead", billHead);
        otherAttrs.put("billBottom", billBottom);
        otherAttrs.put("cpt_count", 5);   
        otherAttrs.put("billSubTotal", billSubTotal);
        
        String output = "E:/SDBill.xls";
        
        IvyXmlAttrDsXlsWriter ivyXmlAttrDsXlsWriter = new IvyXmlAttrDsXlsWriter(ivyDocDesc, ds, otherAttrs, output);
        ivyXmlAttrDsXlsWriter.create();
        
        System.out.println(System.currentTimeMillis() - now);
    }

    public static void main(String[] args) {
         test();
    }
}

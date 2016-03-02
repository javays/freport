package com.ivy.freport.writer.xls;
/* 
 * Copyright (c) 2016, S.F. Express Inc. All rights reserved.
 */



import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;

import com.ivy.freport.ds.IvyDataSource;
import com.ivy.freport.ds.IvyDbDataSource;
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

public class IvyXlsWriterDbDsTest {
    
    public static Connection getConnection() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        return DriverManager.getConnection("jdbc:mysql://10.202.4.41:3310/ecbilec", "ecbilec", "ecbilec_Mari");
    }

    public static void test() throws Exception {
        long now = System.currentTimeMillis();
        
        Connection connection = getConnection();  //TODO
        
        String orderSql = "select o.order_dt monthDate," +
                                 " o.order_no orderNo," +
                                 " o.waybill_no waybillNo," +
                                 " o.quote_amt feeAmt" +
                     " from tt_express_order o" +
                    " where o.pay_dept_code = '571YE'" +
                      " and o.cust_code = '8888999904'" +
                      " and o.order_dt between '2015-06-01' and '2015-06-31'";
//        String cptSql = "";
        
        int orderNum = 200000;
        int cptNum = 10;
        
        IvyDataSource<Map<String, Object>> orderDs = new IvyDbDataSource(connection, orderSql, orderNum);
//        IvyDataSource<ResultSet> cptDs = new IvyDbDataSource(connection, cptSql, cptNum);
        
        Map<String, IvyDataSource<Map<String, Object>>> ds = new HashMap<String, IvyDataSource<Map<String, Object>>>();
        ds.put("order", orderDs);
//        ds.put("cpt", cptDs);
        
        
        Map<String, IvyDocDesc> ivyDocDescs = IvyLayout.getInstance().docDescs;
        IvyDocDesc ivyDocDesc = ivyDocDescs.get("tmpl_xls_bill_standard");
        
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
        
        IvyDbDsXlsWriter ivyDbDsXlsWriter = new IvyDbDsXlsWriter(ivyDocDesc, ds, otherAttrs, output);
        ivyDbDsXlsWriter.create();
        
        System.out.println(System.currentTimeMillis() - now);
    }

    public static void main(String[] args) throws Exception {
         test();
    }
}

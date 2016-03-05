package com.ivy.freport.writer.xls;
/* 
 * Copyright (c) 2016, S.F. Express Inc. All rights reserved.
 */



import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;

import com.ivy.freport.ds.IvyDataSource;
import com.ivy.freport.ds.IvyDbDataSource;
import com.ivy.freport.ds.IvyEntityDataSource;
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

public class IvyXlsWriterEntityDsTest {
    
    public static void test() throws Exception {
        long now = System.currentTimeMillis();
        
        List<ShopPeriod> shopPeriods = new ArrayList<ShopPeriod>();
        shopPeriods.add(createShopPeriod());
        
        IvyDataSource<ShopPeriod> orderDs = new IvyEntityDataSource<ShopPeriod>(shopPeriods);
        
        Map<String, IvyDataSource<ShopPeriod>> ds = new HashMap<String, IvyDataSource<ShopPeriod>>();
        ds.put("order", orderDs);
        
        Map<String, IvyDocDesc> ivyDocDescs = IvyLayout.getInstance().docDescs;
        IvyDocDesc ivyDocDesc = ivyDocDescs.get("tmpl_xls_bill_bbil_cod_period");
        
        Map<String, Object> otherAttrs = new HashMap<String, Object>();
        
        otherAttrs.put("order_count", 1);
        
        String output = "E:/SDBill.xls";
        
        IvyXlsWriter<ShopPeriod> ivyDbDsXlsWriter = new IvyEntityDsXlsWriter<ShopPeriod>(ivyDocDesc, ds, otherAttrs, output);
        ivyDbDsXlsWriter.create();
        
        System.out.println(System.currentTimeMillis() - now);
    }

    public static void main(String[] args) throws Exception {
         test();
    }
    
    private static ShopPeriod createShopPeriod() {
        ShopPeriod shopPeriod = new ShopPeriod();
        shopPeriod.id = 100;
        shopPeriod.shopCode = "10001";
        shopPeriod.shopName = "shop-1";
        shopPeriod.startDt = "2015-01-01";
        shopPeriod.endDt = "2016-01-01";
        shopPeriod.fee = 100.1;
        shopPeriod.status = "未结算";
        
        return shopPeriod;
    }
}

/* 
 * Copyright (c) 2015, S.F. Express Inc. All rights reserved.
 */

package com.ivy.freport.service;


/**
 * 描述：
 * 
 * <pre>HISTORY
 * ****************************************************************************
 *  ID   DATE           PERSON          REASON
 *  1    2015年5月1日      Steven.Zhu         Create
 * ****************************************************************************
 * </pre>
 * @author Steven.Zhu
 * @since 
 */

public class DataCreate {
    
    public static BillHead getBillHead() {
        BillHead billHead = new BillHead();
        billHead.setBillStart("2015-03-01");
        billHead.setBillEnd("2015-04-31");
        billHead.setBillingMembers("61303380");
        billHead.setContact("ZJ");
        billHead.setCurrency("CNY");
        billHead.setCurrentAmount(100011.1);
        billHead.setCustCode("7550001234");
        billHead.setCustomerName("Lily");
        billHead.setDeptCode("755A");
        billHead.setDueDate("2015-02-06");
        billHead.setTheAmountOfUnpaid(80000);
        billHead.setTotalAmount(180011.1);
        
        return billHead;
    }
    
    public static BillBottom getBillBottom(){
        BillBottom billBottom = new BillBottom();
        billBottom.setCompanyName("顺丰速运深圳有限公司");
        billBottom.setBankName("招商银行深圳福田区支行");
        billBottom.setBankAccount("400562322252362");
        billBottom.setLinkMan("王小姐");
        billBottom.setTelphone("0755-86235895");
        billBottom.setEmail("Mr.Wang@sf-express.com");
        billBottom.setFaxNo("0755-86235895");
        
        return billBottom;
    }

    public static void createInvoiceItem() {
        String invoiceCode = "ECBILNCE0001CJ2G";
        
        String sql = "insert into tt_express_invoice_items(EXPRESS_INVOICE_CODE, FEE_TYPE_CODE, FEE_ITEM_ID) "
                + "values('%s', '%s', %d);\n";
        
        for (int i = 2833; i < 5436; i++) {
            System.out.printf(sql, invoiceCode, '1', i);
            
        }
    }
    
    public static void main(String[] args) {
        createInvoiceItem();
    }
}

/**
 * Copyright (c) 2014, S.F.EXPRESS CO.LTD. All rights reserved.
 * 
 */
package com.ivy.freport.service;



/**
 * 月结客户账单头部信息
 * @author Steven.Zhu
 * @email stevenzhu@sf-express.com
 * 2015-1-29
 */
public class BillHead {

    private String customerName;      //客户名称 
    private String pdfCustName;       //用户PDF显示客户名称
    private String customerEnName;    //英文名称
    private String custCode;          //月结账号： 
    private String billingacc;        //结算卡号
    private String contact;           //联系人： 
    private String deptCode;          //地       区 
    private String deptName;          //网点名称
    private String billingMembers;     //收账员： 
    private String currency;          //币       别： 
    private int paymentCycleQty;      //付款周期
    private String dueDate;           //到期日： 
    
    private String warehouseCode;  
    private String warehouseName;

    private String billStart;      //结算期间：  20140701-20140731
    private String billEnd;        //结算期间：  20140701-20140731
    private double currentAmount;       //本期金额：    保留一位小数 
    private double theAmountOfUnpaid;     //上期未付金额：        保留一位小数 
    private double totalAmount;        //截止本月您累计应付金额：     保留一位小数 
    
    public String getCustomerName() {
        return customerName;
    }
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    public String getCustCode() {
        return custCode;
    }
    public void setCustCode(String custCode) {
        this.custCode = custCode;
    }
    public String getContact() {
        return contact;
    }
    public void setContact(String contact) {
        this.contact = contact;
    }
    public String getDeptCode() {
        return deptCode;
    }
    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }
    public String getBillingMembers() {
        return billingMembers;
    }
    public void setBillingMembers(String billingMembers) {
        this.billingMembers = billingMembers;
    }
    public String getCurrency() {
        return currency;
    }
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    public String getDueDate() {
        return dueDate;
    }
    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }
    public String getBillStart() {
        return billStart;
    }
    public void setBillStart(String billStart) {
        this.billStart = billStart;
    }
    public String getBillEnd() {
        return billEnd;
    }
    public void setBillEnd(String billEnd) {
        this.billEnd = billEnd;
    }
    public double getCurrentAmount() {
        return currentAmount;
    }
    public void setCurrentAmount(double currentAmount) {
        this.currentAmount = currentAmount;
    }
    public double getTheAmountOfUnpaid() {
        return theAmountOfUnpaid;
    }
    public void setTheAmountOfUnpaid(double theAmountOfUnpaid) {
        this.theAmountOfUnpaid = theAmountOfUnpaid;
    }
    public double getTotalAmount() {
        return totalAmount;
    }
    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
    public String getCustomerEnName() {
        return customerEnName;
    }
    public void setCustomerEnName(String customerEnName) {
        this.customerEnName = customerEnName;
    }
    public int getPaymentCycleQty() {
        return paymentCycleQty;
    }
    public void setPaymentCycleQty(int paymentCycleQty) {
        this.paymentCycleQty = paymentCycleQty;
    }
    
    public String getWarehouseCode() {
        return warehouseCode;
    }
    public void setWarehouseCode(String warehouseCode) {
        this.warehouseCode = warehouseCode;
    }
    public String getWarehouseName() {
        return warehouseName;
    }
    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }
    public String getPdfCustName() {
        return pdfCustName;
    }
    public void setPdfCustName(String pdfCustName) {
        this.pdfCustName = pdfCustName;
    }
    
    public String getBillingacc() {
        return billingacc;
    }
    public void setBillingacc(String billingacc) {
        this.billingacc = billingacc;
    }
    public String getDeptName() {
        return deptName;
    }
    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }
    @Override
    public String toString() {
        return "BillHead [customerName=" + customerName
                + ", pdfCustName="
                + pdfCustName
                + ", customerEnName="
                + customerEnName
                + ", custCode="
                + custCode
                + ", billingacc="
                + billingacc
                + ", contact="
                + contact
                + ", deptCode="
                + deptCode
                + ", deptName="
                + deptName
                + ", billingMembers="
                + billingMembers
                + ", currency="
                + currency
                + ", paymentCycleQty="
                + paymentCycleQty
                + ", dueDate="
                + dueDate
                + ", warehouseCode="
                + warehouseCode
                + ", warehouseName="
                + warehouseName
                + ", billStart="
                + billStart
                + ", billEnd="
                + billEnd
                + ", currentAmount="
                + currentAmount
                + ", theAmountOfUnpaid="
                + theAmountOfUnpaid
                + ", totalAmount="
                + totalAmount
                + "]";
    }
}

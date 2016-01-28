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
 *  1    2015年3月11日      Steven.Zhu         Create
 * ****************************************************************************
 * </pre>
 * @author Steven.Zhu
 * @since 
 */

public class BillBottom {

    private String companyName;    //公司名称：     
    private String bankName;       //开户银行：       
    private String bankAccount;    //银行账号：       
    private String linkMan;        //SF联系人：      
    private String telphone;       //联系电话：       
    private String email;          //联系邮箱：   
    private String faxNo;          //传真号码：       
    
    public String getCompanyName() {
        return companyName;
    }
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
    public String getBankName() {
        return bankName;
    }
    public void setBankName(String bankName) {
        this.bankName = bankName;
    }
    public String getBankAccount() {
        return bankAccount;
    }
    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }
    public String getLinkMan() {
        return linkMan;
    }
    public void setLinkMan(String linkMan) {
        this.linkMan = linkMan;
    }
    public String getTelphone() {
        return telphone;
    }
    public void setTelphone(String telphone) {
        this.telphone = telphone;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getFaxNo() {
        return faxNo;
    }
    public void setFaxNo(String faxNo) {
        this.faxNo = faxNo;
    }
    
    @Override
    public String toString() {
        return "BillButton [companyName=" + companyName
                + ", bankName="
                + bankName
                + ", bankAccount="
                + bankAccount
                + ", linkMan="
                + linkMan
                + ", telphone="
                + telphone
                + ", email="
                + email
                + ", faxNo="
                + faxNo
                + "]";
    }
}

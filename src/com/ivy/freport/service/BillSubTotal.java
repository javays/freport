/* 
 * Copyright (c) 2015, S.F. Express Inc. All rights reserved.
 */

package com.ivy.freport.service;

import java.io.Serializable;


/**
 * 描述：
 * 
 * <pre>
 * HISTORY
 * ****************************************************************************
 *  ID   DATE           PERSON          REASON
 *  1    2015年3月26日      Steven.Zhu         Create
 * ****************************************************************************
 * </pre>
 * 
 * @author Steven.Zhu
 * @since
 */

public class BillSubTotal implements Serializable {

    private static final long serialVersionUID = 1L;

    private double totalWeight;       //重量合计
    private double totalFeeAmt;       //折扣前费用合计
    private double totalRebateAmt;    //折扣金额合计
    private double subTotalDueAmt;    //折扣后金额合计

    private double specialRebate;     //特殊折扣
    private double totalRebate;       //折扣总和（折扣+特殊折扣）
    private double totalDueAmtAfterSpecialRebate;    //扣除特殊折扣后金额
    
    private double totalCxRebate;          //促销折扣
    
    private double totalDueAmtAfterCpt;    //扣除理赔后应收
    
    private double totalDueAmtCurPriod;     //当期应收
    
    private double unpaidFee;       //应收余额
    
    private double totalDueAmt;   //最终应收
    
    private String s_totalDueAmtCurPriod = "0.00";     //当期应收
    private String s_unpaidFee = "0.00";     //应收余额
    private String s_totalDueAmt = "0.00";    //最终应收
    
    //抵扣费用小计
    private double totalOffsetFreightAmt;    //抵扣运费小计
    private double totalSubtractMonthlyAmt;  //抵扣月结运费小计
    private double totalOffsetValueAddAmt;   //抵扣增值服务费小计
    private double totalFeeFreight;     //抵扣小计
    
    private double totalFeeAmtFreight;
    private double totalFeeRebateFreight;
    private double totalFeeDueFreight;        
    
    //增值服务费小计
    private double totalFeeSendOnTime;    //各种增值服务费合计
    private double totalFeeReturn;
    private double totalFeeExchange;
    private double totalFeeNotify;
    private double totalFeeOutOfScope;
    private double totalFeeFaraway;
    private double totalFeeSignReturn;
    private double totalFeeHoliday;
    private double totalFeeInsurance;
    private double totalFeeBuySellInsurance;
    private double totalFeeOnTimeInsurance;
    private double totalFeeUrgent;
    private double totalFeeEAccept;
    private double totalFeeLongHeavy;
    private double totalFeeRefreshment;
    private double totalOthers;
    
    private int orderNum;
    private int sdNum;
    private int cptNum;
    private int cxNum;
    
    public void addSpecialRebate(double specialRebate) {
        this.specialRebate += specialRebate;
    }
    
    public double getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(double totalWeight) {
        this.totalWeight = totalWeight;
    }

    public double getTotalFeeAmt() {
        return totalFeeAmt;
    }

    public void setTotalFeeAmt(double totalFeeAmt) {
        this.totalFeeAmt = totalFeeAmt;
    }

    public double getTotalRebateAmt() {
        return totalRebateAmt;
    }

    public void setTotalRebateAmt(double totalRebateAmt) {
        this.totalRebateAmt = totalRebateAmt;
    }

    public double getTotalDueAmt() {
        return totalDueAmt;
    }

    public void setTotalDueAmt(double totalDueAmt) {
        this.totalDueAmt = totalDueAmt;
    }

    public double getTotalOffsetFreightAmt() {
        return totalOffsetFreightAmt;
    }

    public void setTotalOffsetFreightAmt(double totalOffsetFreightAmt) {
        this.totalOffsetFreightAmt = totalOffsetFreightAmt;
    }

    public double getTotalSubtractMonthlyAmt() {
        return totalSubtractMonthlyAmt;
    }

    public void setTotalSubtractMonthlyAmt(double totalSubtractMonthlyAmt) {
        this.totalSubtractMonthlyAmt = totalSubtractMonthlyAmt;
    }

    public double getSpecialRebate() {
        return specialRebate;
    }

    public double getTotalFeeFreight() {
        return totalFeeFreight;
    }

    public void setTotalFeeFreight(double totalFeeFreight) {
        this.totalFeeFreight = totalFeeFreight;
    }

    public double getTotalFeeSendOnTime() {
        return totalFeeSendOnTime;
    }

    public void setTotalFeeSendOnTime(double totalFeeSendOnTime) {
        this.totalFeeSendOnTime = totalFeeSendOnTime;
    }

    public double getTotalFeeReturn() {
        return totalFeeReturn;
    }

    public void setTotalFeeReturn(double totalFeeReturn) {
        this.totalFeeReturn = totalFeeReturn;
    }

    public double getTotalFeeExchange() {
        return totalFeeExchange;
    }

    public void setTotalFeeExchange(double totalFeeExchange) {
        this.totalFeeExchange = totalFeeExchange;
    }

    public double getTotalFeeNotify() {
        return totalFeeNotify;
    }

    public void setTotalFeeNotify(double totalFeeNotify) {
        this.totalFeeNotify = totalFeeNotify;
    }

    public double getTotalFeeOutOfScope() {
        return totalFeeOutOfScope;
    }

    public void setTotalFeeOutOfScope(double totalFeeOutOfScope) {
        this.totalFeeOutOfScope = totalFeeOutOfScope;
    }

    public double getTotalFeeFaraway() {
        return totalFeeFaraway;
    }

    public void setTotalFeeFaraway(double totalFeeFaraway) {
        this.totalFeeFaraway = totalFeeFaraway;
    }

    public double getTotalFeeSignReturn() {
        return totalFeeSignReturn;
    }

    public void setTotalFeeSignReturn(double totalFeeSignReturn) {
        this.totalFeeSignReturn = totalFeeSignReturn;
    }

    public double getTotalFeeHoliday() {
        return totalFeeHoliday;
    }

    public void setTotalFeeHoliday(double totalFeeHoliday) {
        this.totalFeeHoliday = totalFeeHoliday;
    }

    public double getTotalFeeInsurance() {
        return totalFeeInsurance;
    }

    public void setTotalFeeInsurance(double totalFeeInsurance) {
        this.totalFeeInsurance = totalFeeInsurance;
    }

    public double getTotalFeeBuySellInsurance() {
        return totalFeeBuySellInsurance;
    }

    public void setTotalFeeBuySellInsurance(double totalFeeBuySellInsurance) {
        this.totalFeeBuySellInsurance = totalFeeBuySellInsurance;
    }

    public double getTotalFeeOnTimeInsurance() {
        return totalFeeOnTimeInsurance;
    }

    public void setTotalFeeOnTimeInsurance(double totalFeeOnTimeInsurance) {
        this.totalFeeOnTimeInsurance = totalFeeOnTimeInsurance;
    }

    public double getTotalOthers() {
        return totalOthers;
    }

    public void setTotalOthers(double totalOthers) {
        this.totalOthers = totalOthers;
    }
    
    public double getSubTotalDueAmt() {
        return subTotalDueAmt;
    }

    public void setSubTotalDueAmt(double subTotalDueAmt) {
        this.subTotalDueAmt = subTotalDueAmt;
    }
    
    public double getUnpaidFee() {
        return unpaidFee;
    }

    public double getTotalDueAmtAfterSpecialRebate() {
        return totalDueAmtAfterSpecialRebate;
    }

    public void
            setTotalDueAmtAfterSpecialRebate(double totalDueAmtAfterSpecialRebate) {
        this.totalDueAmtAfterSpecialRebate = totalDueAmtAfterSpecialRebate;
    }

    public double getTotalDueAmtAfterCpt() {
        return totalDueAmtAfterCpt;
    }

    public void setTotalDueAmtAfterCpt(double totalDueAmtAfterCpt) {
        this.totalDueAmtAfterCpt = totalDueAmtAfterCpt;
    }
    
    public double getTotalDueAmtCurPriod() {
        return totalDueAmtCurPriod;
    }

    public void setTotalDueAmtCurPriod(double totalDueAmtCurPriod) {
        this.totalDueAmtCurPriod = totalDueAmtCurPriod;
    }
    public double getTotalOffsetValueAddAmt() {
        return totalOffsetValueAddAmt;
    }

    public void setTotalOffsetValueAddAmt(double totalOffsetValueAddAmt) {
        this.totalOffsetValueAddAmt = totalOffsetValueAddAmt;
    }

    public double getTotalFeeAmtFreight() {
        return totalFeeAmtFreight;
    }

    public void setTotalFeeAmtFreight(double totalFeeAmtFreight) {
        this.totalFeeAmtFreight = totalFeeAmtFreight;
    }

    public double getTotalFeeRebateFreight() {
        return totalFeeRebateFreight;
    }

    public void setTotalFeeRebateFreight(double totalFeeRebateFreight) {
        this.totalFeeRebateFreight = totalFeeRebateFreight;
    }

    public double getTotalFeeDueFreight() {
        return totalFeeDueFreight;
    }

    public void setTotalFeeDueFreight(double totalFeeDueFreight) {
        this.totalFeeDueFreight = totalFeeDueFreight;
    }

    public double getTotalRebate() {
        return totalRebate;
    }

    public void setTotalRebate(double totalRebate) {
        this.totalRebate = totalRebate;
    }

    public int getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(int orderNum) {
        this.orderNum = orderNum;
    }

    public int getSdNum() {
        return sdNum;
    }

    public void setSdNum(int sdNum) {
        this.sdNum = sdNum;
    }

    public int getCptNum() {
        return cptNum;
    }

    public void setCptNum(int cptNum) {
        this.cptNum = cptNum;
    }

    public double getTotalFeeUrgent() {
        return totalFeeUrgent;
    }

    public void setTotalFeeUrgent(double totalFeeUrgent) {
        this.totalFeeUrgent = totalFeeUrgent;
    }

    public String getS_unpaidFee() {
        return s_unpaidFee;
    }

    public String getS_totalDueAmtCurPriod() {
        return s_totalDueAmtCurPriod;
    }

    public String getS_totalDueAmt() {
        return s_totalDueAmt;
    }

    public void setS_totalDueAmtCurPriod(String s_totalDueAmtCurPriod) {
        this.s_totalDueAmtCurPriod = s_totalDueAmtCurPriod;
    }

    public void setS_unpaidFee(String s_unpaidFee) {
        this.s_unpaidFee = s_unpaidFee;
    }

    public void setS_totalDueAmt(String s_totalDueAmt) {
        this.s_totalDueAmt = s_totalDueAmt;
    }

    public double getTotalFeeEAccept() {
        return totalFeeEAccept;
    }

    public void setTotalFeeEAccept(double totalFeeEAccept) {
        this.totalFeeEAccept = totalFeeEAccept;
    }
    
    public double getTotalFeeLongHeavy() {
        return totalFeeLongHeavy;
    }

    public void setTotalFeeLongHeavy(double totalFeeLongHeavy) {
        this.totalFeeLongHeavy = totalFeeLongHeavy;
    }

    public double getTotalFeeRefreshment() {
        return totalFeeRefreshment;
    }

    public void setTotalFeeRefreshment(double totalFeeRefreshment) {
        this.totalFeeRefreshment = totalFeeRefreshment;
    }

    public double getTotalCxRebate() {
        return totalCxRebate;
    }

    public void setTotalCxRebate(double totalCxRebate) {
        this.totalCxRebate = totalCxRebate;
    }

    public int getCxNum() {
        return cxNum;
    }

    public void setCxNum(int cxNum) {
        this.cxNum = cxNum;
    }

    @Override
    public String toString() {
        return "BillSubTotal [totalWeight=" + totalWeight
                + ", totalFeeAmt="
                + totalFeeAmt
                + ", totalRebateAmt="
                + totalRebateAmt
                + ", subTotalDueAmt="
                + subTotalDueAmt
                + ", specialRebate="
                + specialRebate
                + ", totalRebate="
                + totalRebate
                + ", totalDueAmtAfterSpecialRebate="
                + totalDueAmtAfterSpecialRebate
                + ", totalCxRebate="
                + totalCxRebate
                + ", totalDueAmtAfterCpt="
                + totalDueAmtAfterCpt
                + ", totalDueAmtCurPriod="
                + totalDueAmtCurPriod
                + ", unpaidFee="
                + unpaidFee
                + ", totalDueAmt="
                + totalDueAmt
                + ", s_totalDueAmtCurPriod="
                + s_totalDueAmtCurPriod
                + ", s_unpaidFee="
                + s_unpaidFee
                + ", s_totalDueAmt="
                + s_totalDueAmt
                + ", totalOffsetFreightAmt="
                + totalOffsetFreightAmt
                + ", totalSubtractMonthlyAmt="
                + totalSubtractMonthlyAmt
                + ", totalOffsetValueAddAmt="
                + totalOffsetValueAddAmt
                + ", totalFeeFreight="
                + totalFeeFreight
                + ", totalFeeAmtFreight="
                + totalFeeAmtFreight
                + ", totalFeeRebateFreight="
                + totalFeeRebateFreight
                + ", totalFeeDueFreight="
                + totalFeeDueFreight
                + ", totalFeeSendOnTime="
                + totalFeeSendOnTime
                + ", totalFeeReturn="
                + totalFeeReturn
                + ", totalFeeExchange="
                + totalFeeExchange
                + ", totalFeeNotify="
                + totalFeeNotify
                + ", totalFeeOutOfScope="
                + totalFeeOutOfScope
                + ", totalFeeFaraway="
                + totalFeeFaraway
                + ", totalFeeSignReturn="
                + totalFeeSignReturn
                + ", totalFeeHoliday="
                + totalFeeHoliday
                + ", totalFeeInsurance="
                + totalFeeInsurance
                + ", totalFeeBuySellInsurance="
                + totalFeeBuySellInsurance
                + ", totalFeeOnTimeInsurance="
                + totalFeeOnTimeInsurance
                + ", totalFeeUrgent="
                + totalFeeUrgent
                + ", totalFeeEAccept="
                + totalFeeEAccept
                + ", totalFeeLongHeavy="
                + totalFeeLongHeavy
                + ", totalFeeRefreshment="
                + totalFeeRefreshment
                + ", totalOthers="
                + totalOthers
                + ", orderNum="
                + orderNum
                + ", sdNum="
                + sdNum
                + ", cptNum="
                + cptNum
                + ", cxNum="
                + cxNum
                + "]";
    }
}

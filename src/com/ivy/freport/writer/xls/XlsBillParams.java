/* 
 * Copyright (c) 2015, S.F. Express Inc. All rights reserved.
 */

package com.ivy.freport.writer.xls;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sf.ecbil.bill.vo.BillBottom;
import com.sf.ecbil.bill.vo.BillHead;
import com.sf.ecbil.bill.vo.BillSubTotal;

/**
 * 描述：
 * 
 * <pre>HISTORY
 * ****************************************************************************
 *  ID   DATE           PERSON          REASON
 *  1    2015年4月7日      Steven.Zhu         Create
 * ****************************************************************************
 * </pre>
 * @author Steven.Zhu
 * @since 
 */

public class XlsBillParams {
    
    private InputStream tmplStream;
    private InputStream extraTmplStream;
    private String output;
    private Map<String, Object> param;
    
    /**
     * @param tmplStream
     * @param extraTmplStream
     * @param output
     * @param param
     */
    public XlsBillParams(InputStream tmplStream, 
                         InputStream extraTmplStream,
                         String output, 
                         BillHead billHead, 
                         BillBottom billBottom, 
                         int count,
                         List<File> waybills, 
                         int cptCount, 
                         List<File> cpts,
                         int sdCount,
                         List<File> sumDiscount,
                         BillSubTotal billSubTotal) {
        super();
        this.tmplStream = tmplStream;
        this.extraTmplStream = extraTmplStream;
        this.output = output;
        
        this.param = new HashMap<String, Object>();
        param.put("billHead", billHead);
        param.put("billBottom", billBottom);
        
        param.put("count", count);
        param.put("waybills", waybills);
        
        param.put("cpts", cpts);
        param.put("cpts_count", cptCount);
        
        param.put("sd", sumDiscount);
        param.put("sd_count", sdCount);
        
        param.put("billSubTotal", billSubTotal);
    }
    
    
    public InputStream getTmplStream() {
        return tmplStream;
    }
    public void setTmplStream(InputStream tmplStream) {
        this.tmplStream = tmplStream;
    }
    public InputStream getExtraTmplStream() {
        return extraTmplStream;
    }
    public void setExtraTmplStream(InputStream extraTmplStream) {
        this.extraTmplStream = extraTmplStream;
    }
    public String getOutput() {
        return output;
    }
    public void setOutput(String output) {
        this.output = output;
    }
    public Map<String, Object> getParam() {
        return param;
    }
    public void setParam(Map<String, Object> param) {
        this.param = param;
    }
    
    
    /**
     * @param tmpl
     * @param outputFile
     * @param orderCount
     * @param cptCount
     * @param orderDS
     * @param cptDS
     * @param billHead
     * @param billBottom
     * @param waybills
     * @param cpts
     * @param sumDiscount
     */
    /*public XlsBillParams(String tmpl, 
                String outputFile, 
                BillHead billHead, 
                BillBottom billBottom, 
                int count,
                List<File> waybills, 
                int cptCount, 
                List<File> cpts,
                int sdCount,
                List<File> sumDiscount,
                BillSubTotal billSubTotal) {
        super();
        
        this.put("tmpl", tmpl);
        this.put("outputFile", outputFile);
        this.put("billHead", billHead);
        this.put("billBottom", billBottom);
        
        this.put("count", count);
        this.put("waybills", waybills);
        
        this.put("cpts", cpts);
        this.put("cpts_count", cptCount);
        
        this.put("sd", sumDiscount);
        this.put("sd_count", sdCount);
        
        this.put("billSubTotal", billSubTotal);
    }*/
    
    /*private String tmpl;
    private String outputFile;

    private int orderCount;
    private int cptCount;
    private String orderDS;
    private String cptDS;
    
    private BillHead billHead;
    private BillBottom billBottom;
    
    private List<File> waybills;
    private File cpts;
    private File sumDiscount;
    
    public int getOrderCount() {
        return orderCount;
    }
    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }
    public int getCptCount() {
        return cptCount;
    }
    public void setCptCount(int cptCount) {
        this.cptCount = cptCount;
    }
    public String getOrderDS() {
        return orderDS;
    }
    public void setOrderDS(String orderDS) {
        this.orderDS = orderDS;
    }
    public String getCptDS() {
        return cptDS;
    }
    public void setCptDS(String cptDS) {
        this.cptDS = cptDS;
    }
    public BillHead getBillHead() {
        return billHead;
    }
    public void setBillHead(BillHead billHead) {
        this.billHead = billHead;
    }
    public BillBottom getBillBottom() {
        return billBottom;
    }
    public void setBillBottom(BillBottom billBottom) {
        this.billBottom = billBottom;
    }
    public String getTmpl() {
        return tmpl;
    }
    public void setTmpl(String tmpl) {
        this.tmpl = tmpl;
    }
    public String getOutputFile() {
        return outputFile;
    }
    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }
    public List<File> getWaybills() {
        return waybills;
    }
    public void setWaybills(List<File> waybills) {
        this.waybills = waybills;
    }
    public File getCpts() {
        return cpts;
    }
    public void setCpts(File cpts) {
        this.cpts = cpts;
    }
    public File getSumDiscount() {
        return sumDiscount;
    }
    public void setSumDiscount(File sumDiscount) {
        this.sumDiscount = sumDiscount;
    }*/
}

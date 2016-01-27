/* 
 * Copyright (c) 2015, S.F. Express Inc. All rights reserved.
 */

package com.ivy.freport.writer.xls;

import java.util.HashMap;
import java.util.Map;

import com.sf.ecbil.bill.util.FileUtils;
import com.sf.ecbil.bill.util.TmplConfig;
import com.sf.ecbil.bill.vo.EnumFileType;

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

public class BillParams extends HashMap<String, Object> {
    
    private static final long serialVersionUID = 1L;
    
    public static final String PARAM_TMPL = "tmpl";
    public static final String PARAM_EXTRA_TMPL = "extraTmpl";
    
    public static final String PARAM_BILL_HEAD = "billHead";
    public static final String PARAM_BILL_BOTTOM = "billBottom";
    public static final String PARAM_BILL_SUBTOTAL = "billSubTotal";
    
    public static final String PARAM_DS_ORDER = "order";
    public static final String PARAM_DS_CPT = "cpt";
    public static final String PARAM_DS_SD = "sd";
    public static final String PARAM_DS_CX = "cx";
    
    public static final String PARAM_DS_ORDER_COUNT = "order_count";
    public static final String PARAM_DS_CPT_COUNT = "cpt_count";
    public static final String PARAM_DS_SD_COUNT = "sd_count";
    public static final String PARAM_DS_CX_COUNT = "cx_count";
    
    public static final String PARAM_OUTPUT = "output";
    
    public BillParams() {
        super();
    }
    
    public BillParams(Map<String, Object> map) {
        this.putAll(map);
    }
    
    /**
     * @param tmplStream
     * @param extraTmplStream
     * @param output
     * @param param
     */
    public BillParams(EnumFileType enumFileType,
                     String output, 
                     Map<String, Object> param) {
        super();
        String tmpl = TmplConfig.getBillTmpl(enumFileType);
        this.put(PARAM_TMPL, FileUtils.getFilePath(tmpl));
        this.put(PARAM_OUTPUT, output);
        this.putAll(param);
        /*if (param == null || param.isEmpty()) {
            throw new RuntimeException("param is null or empty!!!");
        }else if (!param.containsKey(PARAM_BILL_HEAD) ||
                !param.containsKey(PARAM_BILL_BOTTOM) ||
                !param.containsKey(PARAM_BILL_SUBTOTAL) ||
                !param.containsKey(PARAM_DS_ORDER) ||
                !param.containsKey(PARAM_DS_CPT) ||
                !param.containsKey(PARAM_DS_SD)) {
            throw new RuntimeException("map param should contain all param!!!");
        }
        */
    }

    @Override
    public String toString() {
        String str = "BillParams [";
        for (Entry<String, Object> entry : entrySet()) {
            str += entry.getKey() + "=" + entry.getValue() + ",";
        }
        str += "]";
        return str;
    }
}

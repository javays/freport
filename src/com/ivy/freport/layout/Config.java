/* 
 * Copyright (c) 2016, S.F. Express Inc. All rights reserved.
 */

package com.ivy.freport.layout;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.ivy.freport.utils.StringUtils;

/**
 * 描述：
 * 
 * <pre>HISTORY
 * ****************************************************************************
 *  ID   DATE           PERSON          REASON
 *  1    2016年1月29日      Steven.Zhu         Create
 * ****************************************************************************
 * </pre>
 * @author Steven.Zhu
 * @since 
 */

public class Config {
    
    private static boolean xls_split_file = true;
    private static int xls_wb_max_sheet = 1;
    private static int xls_sheet_max_item = 0;
    private static boolean xls_sheet_show_head = true;
    
    static {
        load();
    }
    
    
    public static void load() {
        InputStream is = null;
        try {
            Properties p = new Properties();
            
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream("freport.properties");
            p.load(is);
            
            String s_xls_split_file = p.getProperty("xls.split.file");    //#是否切分文件，数据量大时设置
            if (!StringUtils.isEmpty(s_xls_split_file)) {
                xls_split_file = Boolean.parseBoolean(s_xls_split_file);
            }
            
            String s_xls_wb_max_sheet = p.getProperty("xls.wb.max.sheet");    //#xls最大sheet数
            if (!StringUtils.isEmpty(s_xls_wb_max_sheet)) {
                xls_wb_max_sheet = Integer.parseInt(s_xls_wb_max_sheet);
            }
            
            String s_xls_sheet_max_item = p.getProperty("xls.sheet.max.item");    //#每个sheet最大记录数
            if (!StringUtils.isEmpty(s_xls_sheet_max_item)) {
                xls_sheet_max_item = Integer.parseInt(s_xls_sheet_max_item);
            }
            
            String s_xls_sheet_show_head = p.getProperty("xls.sheet.show.head");    //#xls显示表格头
            if (!StringUtils.isEmpty(s_xls_sheet_show_head)) {
                xls_sheet_show_head = Boolean.parseBoolean(s_xls_sheet_show_head);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        
    }


    public static boolean isXls_split_file() {
        return xls_split_file;
    }


    public static int getXls_wb_max_sheet() {
        return xls_wb_max_sheet;
    }


    public static int getXls_sheet_max_item() {
        return xls_sheet_max_item;
    }


    public static boolean isXls_sheet_show_head() {
        return xls_sheet_show_head;
    }
}

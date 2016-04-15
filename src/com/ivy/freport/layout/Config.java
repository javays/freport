/* 
 * Copyright (c) 2016, S.F. Express Inc. All rights reserved.
 */

package com.ivy.freport.layout;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.log4j.Logger;

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
    
    private final static Logger logger = Logger.getLogger(Config.class);
    
    private static boolean xls_split_file = true;
    private static int xls_wb_max_sheet = 2;
    private static int xls_sheet_max_item = 50000;
    private static boolean xls_sheet_show_head = true;
    
    private static String tmpl_path = ".";
    
    static {
        load();
    }
    
    
    public static void load() {
        InputStream is = null;
        try {
            Properties p = new Properties();
            
            URL propUrl = Thread.currentThread().getContextClassLoader().getResource("resources/freport.properties");
            if (propUrl == null) {
                propUrl = Thread.currentThread().getContextClassLoader().getResource("com/resources/freport.properties");
            }
            
            if (propUrl != null) {
                logger.info("freport path=" + propUrl.getPath());
                
                is = propUrl.openStream();
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
                
                String s_tmpl_path = p.getProperty("tmpl.path");    //#tmpl路径
                if (!StringUtils.isEmpty(s_tmpl_path)) {
                    tmpl_path = s_tmpl_path;
                }
                
                logger.info("freport prop xls.split.file=" + xls_split_file);
                logger.info("freport prop xls.wb.max.sheet=" + xls_wb_max_sheet);
                logger.info("freport prop xls.sheet.max.item=" + xls_sheet_max_item);
                logger.info("freport prop xls.sheet.show.head=" + xls_sheet_show_head);
                logger.info("freport prop tmpl_path=" + tmpl_path);
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

    public static String getTmpl_path() {
        return tmpl_path;
    }

    public static void setXls_split_file(boolean xls_split_file) {
        Config.xls_split_file = xls_split_file;
    }

    public static void setXls_wb_max_sheet(int xls_wb_max_sheet) {
        Config.xls_wb_max_sheet = xls_wb_max_sheet;
    }

    public static void setXls_sheet_max_item(int xls_sheet_max_item) {
        Config.xls_sheet_max_item = xls_sheet_max_item;
    }

    public static void setXls_sheet_show_head(boolean xls_sheet_show_head) {
        Config.xls_sheet_show_head = xls_sheet_show_head;
    }

    public static void setTmpl_path(String tmpl_path) {
        Config.tmpl_path = tmpl_path;
    }
}

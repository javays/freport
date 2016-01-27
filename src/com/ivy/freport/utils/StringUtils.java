/**
 * Copyright (c) 2014, S.F.EXPRESS CO.LTD. All rights reserved.
 * 
 */
package com.ivy.freport.utils;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * @author Steven.Zhu
 * @email stevenzhu@sf-express.com
 * 2014-9-16
 */
public class StringUtils {
    
    public final static Logger logger = Logger.getLogger(StringUtils.class);
    
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    
    public static char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                                    'A', 'B', 'C', 'D', 'E', 'F' };
    
    /**
     * 日期格式化
     * @param date
     * @param pattern
     * @return
     */
    public static String dateFormat(Date date, String pattern) {
        if (pattern == null || "".equals(pattern.trim())) {
            pattern = DATE_PATTERN;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }
    
    /**
     * 字符串转日期
     * @param s_date
     * @return
     */
    public static Date parseDate(String s_date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
            return sdf.parse(s_date);
        } catch (ParseException e) {
            logger.error("", e);
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 字符串是否为空
     * @param str
     * @return
     */
    public static boolean isEmpty(String str) {
        if (str == null || str.trim().equals("")) {
            return true;
        }
        return false;
    }

    /**
     * 正则查找
     * @param str
     * @param regexp
     * @return
     */
    public static String find(String str, String regexp) {
        Pattern pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(str);
        boolean match = matcher.find();
        if (match) {
            return matcher.group();
        }
        return null;
    }

    /**
     * 右填充
     * @param str
     * @param length
     * @param padding
     * @return
     */
    public static String rightPad(String str, int length, char padding) {
        if (str == null) {
            str = "";
        }
        if (length == 0 || str.length() > length) {
            return str;
        }
        for (int i = str.length(); i < length; i++) {
            str += padding;
        }
        return str;
    }

    /**
     * 字符串重复
     * @param str
     * @param repeat
     * @return
     */
    public static String repeat(String str, int repeat) {
        if (str == null) {
            return "";
        }

        String tmp = str;
        for (int i = 0; i < repeat - 1; i++) {
            tmp += str;
        }
        return tmp;
    }

    /**
     * 获取文件名称
     * @param columnName
     * @param linkChar
     * @return
     */
    public static String getFieldName(String columnName, char linkChar) {
        if (StringUtils.isEmpty(columnName)) {
            return null;
        }

        char[] chars = columnName.toCharArray();
        int idx = columnName.indexOf(linkChar);
        while (idx != -1) {
            chars[idx + 1] = Character.toUpperCase(chars[idx + 1]);
            idx = columnName.indexOf(linkChar, idx + 1);
        }

        return new String(chars).replaceAll(linkChar + "", "");
    }
    
    /**
     * RGB颜色创建
     * @param colorStr e.g. "#FFFFFF"
     */
    public static Color hex2Rgb(String colorStr) {
        return new Color(
                Integer.valueOf( colorStr.substring( 1, 3 ), 16 ),
                Integer.valueOf( colorStr.substring( 3, 5 ), 16 ),
                Integer.valueOf( colorStr.substring( 5, 7 ), 16 ) 
               );
    }
    
    /**
     * byte数组转16进制
     * @param byteArray
     * @return
     */
    public static String byteToHex(byte[] byteArray) {
        // 一个byte是八位二进制，也就是2位十六进制字符（2的8次方等于16的2次方）
        char[] resultCharArray = new char[byteArray.length * 2];

        // 遍历字节数组，通过位运算（位运算效率高），转换成字符放到字符数组中去
        int index = 0;
        for (byte b : byteArray) {
            resultCharArray[index++] = hexDigits[b >>> 4 & 0xf];
            resultCharArray[index++] = hexDigits[b & 0xf];
        }

        return new String(resultCharArray);
    }

    /**
     * 获取字符串MD5
     * @param str
     * @return
     */
    public static String md5(String str) {
        if (!isEmpty(str)) {
            try {
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                byte[] bytes = md5.digest(str.getBytes());
                return byteToHex(bytes);
            } catch (NoSuchAlgorithmException e) {
                logger.error("", e);
                e.printStackTrace();
            }
        }

        return "";
    }
    
    /**
     * 获取文件MD5
     * @param file
     * @return
     */
    public static String md5(File file){
        if(file==null){
            return null;
        }
        
        FileInputStream fis = null;
        FileChannel channel = null;
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            
            fis = new FileInputStream(file);
            channel = fis.getChannel();
            ByteBuffer buff = ByteBuffer.allocate(2048);
            while(channel.read(buff) != -1) {
                buff.flip();
                md.update(buff);
                buff.clear();
            }
            byte[] hashValue = md.digest();
            return byteToHex(hashValue);
        }
        catch (NoSuchAlgorithmException | IOException e) {
            logger.error("", e);
            e.printStackTrace();
        } finally {
            try {
                if(channel != null) channel.close();
                if(fis!=null) fis.close();
            } catch (IOException e) {
                logger.error("", e);
                e.printStackTrace();
            }
        } 
        
        return null;
    }
    
    public static void main(String[] args) throws FileNotFoundException {
        Pattern pattern = Pattern.compile("(A[b-e])+");
        
        //, , , 。
        System.out.println(pattern.matcher("Abc").matches());
    }
}























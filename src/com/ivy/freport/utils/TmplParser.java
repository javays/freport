/* 
 * Copyright (c) 2015, S.F. Express Inc. All rights reserved.
 */

package com.ivy.freport.utils;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.ivy.freport.layout.DataType;
import com.ivy.freport.layout.IvyParam;

/**
 * 描述：
 * 
 * <pre>HISTORY
 * ****************************************************************************
 *  ID   DATE           PERSON          REASON
 *  1    2015年5月20日      Steven.Zhu         Create
 * ****************************************************************************
 * </pre>
 * @author Steven.Zhu
 * @since 
 */

public abstract class TmplParser {
    
    private final static Logger logger = Logger.getLogger(TmplParser.class);
    
    public static final String PARAM_REGEX = "\\$F\\{\\s*(\\w+)\\s*(,\\s*dataType\\s*=\\s*(\\w+))?\\s*(,\\s*pattern\\s*=\\s*(\\w+))?\\}";
    public static final Pattern PARAM_PATTERN = Pattern.compile(PARAM_REGEX);
    
    /**
     * 获取字符串中所有参数
     * @param str
     * @return
     */
    public static Set<IvyParam> findParams(String str) {
        Matcher matcher = PARAM_PATTERN.matcher(str);
        
        Set<IvyParam> params = new HashSet<IvyParam>();
        while (matcher.find()) {
            String name = matcher.group(1);
            String fullName = matcher.group();
            String s_dataType = matcher.group(3);
            String pattern = matcher.group(5);
            
            DataType dataType = StringUtils.isEmpty(s_dataType) ? null : DataType.parse(s_dataType);
            
            IvyParam ivyParam = new IvyParam(name, fullName, dataType, pattern);
            
            params.add(ivyParam);
        }
        return params;
    }
    
    /**
     * 字符串是否参数
     * @param str
     * @return
     */
    public static boolean isParam(String str) {
        Matcher matcher = PARAM_PATTERN.matcher(str);
        boolean isMatch = matcher.matches();
        return isMatch;
    }
    
    
    /**
     * 对模板中变量填充值
     * @param line
     * @param params
     * @return
     */
    public static Object fillParams(String line, Map<String, Object> attrs) {
        if (line == null || line.trim().equals("")) {
            return line;
        }
        
        Set<IvyParam> params = findParams(line);
        for (IvyParam param : params) {
            Object value = getParamValue(param.getName(), attrs);
            if (value == null) {
                value = "";
            }else {
                if (param.getDataType() != null 
                        && param.getDataType() == param.getDataType()) {
                    String pattern = param.getPattern();
                    if (!StringUtils.isEmpty(pattern)) {
                        Double d_value = (Double) value;
                        DecimalFormat df = new DecimalFormat(pattern);
                        value = df.format(d_value);
                    }
                }
            }
            
            line = line.replaceAll(Pattern.quote(param.getFullName()), value.toString());
        }
        
        return line;
    }
    
    /**
     * 根据变量名在Map中获取值
     * @param paramName
     * @param params
     * @return
     */
    public static Object getParamValue(String paramName, Map<String, Object> params) {
        if (StringUtils.isEmpty(paramName) || params == null || params.isEmpty()) {
            return null;
        }
        
        try {
            if (paramName.indexOf(".") == -1) {    //obj
                return params.get(paramName);
            }
            String[] param = paramName.split("\\.");    //obj.field
            Object object = params.get(param[0]);
            if (object == null) {
                return null;                   //obj is not assigned
            }else {
                Class<?> clazz = object.getClass();
                Field field = clazz.getDeclaredField(param[1]);
                field.setAccessible(true);
                
                return field.get(object);
            }
        } catch (IllegalArgumentException e) {
            logger.error("", e);
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            logger.error("", e);
            e.printStackTrace();
        } catch (SecurityException e) {
            logger.error("", e);
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            logger.error("", e);
            e.printStackTrace();
        }
        return null;
    }

    public static boolean validate(String exp, Map<String, Object> params) {
        if (params == null || StringUtils.isEmpty(exp)) {
            return false;
        }
        
        String operator = StringUtils.find(exp, "[!<>=]{1,2}");
        String[] expressions = exp.split("\\s*[!<>=]{1,2}\\s*");
        
        Object p_value = getParamValue(expressions[0], params);
        double d_p_value = 0;
        if (p_value != null) {
            d_p_value = Double.parseDouble(p_value.toString());
        }
        
        double d_c_value = Double.parseDouble(expressions[1]);
        
        if (operator.equals(">=")) {
            return d_p_value >= d_c_value;
        }else if (operator.equals("<=")) {
            return d_p_value <= d_c_value;
        }else if (operator.equals(">")) {
            return d_p_value > d_c_value;
        }else if (operator.equals("<")) {
            return d_p_value < d_c_value;
        }else if (operator.equals("==")) {
            return d_p_value == d_c_value;
        }else if (operator.equals("!=")) {
            return d_p_value != d_c_value;
        }else {
            throw new RuntimeException("UNKOWN Expression[" + exp + "]");
        }
    }
    
    public static void main(String[] args) {
        String input = "fdsa$F{ pater, dataType =   number , pattern=12}fff";
        Matcher matcher = PARAM_PATTERN.matcher(input);
        System.out.println(matcher.find());
        System.out.println(matcher.group());
        System.out.println(matcher.group(1));
        System.out.println(matcher.group(2));
        System.out.println(matcher.group(3));
        System.out.println(matcher.group(4));
        System.out.println(matcher.group(5));
        
    }
}

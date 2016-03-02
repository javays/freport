/* 
 * Copyright (c) 2016, S.F. Express Inc. All rights reserved.
 */

package com.ivy.freport.ds;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 描述：
 * 
 * <pre>HISTORY
 * ****************************************************************************
 *  ID   DATE           PERSON          REASON
 *  1    2016年3月1日      Steven.Zhu         Create
 * ****************************************************************************
 * </pre>
 * @author Steven.Zhu
 * @since 
 */

public class IvyDbDataSource implements IvyDataSource<Map<String, Object>> {
    
    private String sql;
    private Object[] args;
    private Connection connection;
    private int size;
    
    private PreparedStatement pstmt;
    private ResultSet rs;
    
    private int counter;
    
    private List<IvyDSAccessListener<Map<String, Object>>> listeners;
    
    /**
     * @param connection
     * @param sql
     */
    public IvyDbDataSource(Connection connection, String sql, int itemNum) {
        super();
        this.connection = connection;
        this.sql = sql;
        this.size = itemNum;
    }
    
    /**
     * @param sql
     * @param args
     * @param connection
     */
    public IvyDbDataSource(Connection connection, String sql, Object[] args, int itemNum) {
        super();
        this.connection = connection;
        this.sql = sql;
        this.args = args;
        this.size = itemNum;
    }

    /* (non-Javadoc)
     * @see java.io.Closeable#close()
     */
    @Override
    public void close() throws IOException {
        try {
            if (rs != null) {
                rs.close();
            }
            if (pstmt != null) {
                pstmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see com.ivy.freport.ds.IvyDataSource#size()
     */
    @Override
    public int size() {
        return size;
    }

    /* (non-Javadoc)
     * @see com.ivy.freport.ds.IvyDataSource#hasNext()
     */
    @Override
    public boolean hasNext() {
        throw new RuntimeException("Not supported");
    }

    /* (non-Javadoc)
     * @see com.ivy.freport.ds.IvyDataSource#next()
     */
    @Override
    public void next() {
        if (connection == null) {
            throw new RuntimeException("connection is null");
        }
        
        try {
            pstmt = connection.prepareStatement(sql);
            if (args != null && args.length > 0) {
                for (int i = 0; i < args.length; i++) {
                    Object object = args[i];
                    pstmt.setObject((i+1), object);
                }
            }
            
            rs = pstmt.executeQuery();
            
            ResultSetMetaData rsmd = rs.getMetaData();
            int count = rsmd.getColumnCount();
            String[] columns = new String[count];
            
            for (int i = 1; i <= count; i++) {
                columns[i-1] = rsmd.getColumnLabel(i);
            }

            Map<String, Object> row = new HashMap<String, Object>();
            while (rs.next()) {
                for (String columnName : columns) {
                    row.put(columnName, rs.getObject(columnName));
                }
                
                counter ++;
                onNextItem(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see com.ivy.freport.ds.IvyDataSource#addListener(com.ivy.freport.ds.IvyDSAccessListener)
     */
    @Override
    public void addListener(IvyDSAccessListener<Map<String, Object>> listener) {
        if (listeners == null) {
            listeners = new ArrayList<IvyDSAccessListener<Map<String, Object>>>();
        }
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /* (non-Javadoc)
     * @see com.ivy.freport.ds.IvyDataSource#onNextItem(java.lang.Object)
     */
    @Override
    public void onNextItem(Map<String, Object> row) {
        if (listeners != null) {
            for (IvyDSAccessListener<Map<String, Object>> ivyDSAccessListener : listeners) {
                ivyDSAccessListener.nextElement(row, counter);
            }
        }
    }

}

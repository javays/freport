/* 
 * Copyright (c) 2016, S.F. Express Inc. All rights reserved.
 */

package com.ivy.freport.ds;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 描述：
 * 
 * <pre>HISTORY
 * ****************************************************************************
 *  ID   DATE           PERSON          REASON
 *  1    2016年3月5日      Steven.Zhu         Create
 * ****************************************************************************
 * </pre>
 * @author Steven.Zhu
 * @since 
 */

public class IvyEntityDataSource<T> implements IvyDataSource<T> {
    
    private List<T> entitys;
    private int size;
    
    private int counter;
    
    private List<IvyDSAccessListener<T>> listeners; 
    
    /**
     * @param entitys
     * @param size
     */
    public IvyEntityDataSource(List<T> entitys) {
        super();
        this.entitys = entitys;
        if (entitys != null) {
            this.size = entitys.size();
        }
    }

    /* (non-Javadoc)
     * @see java.io.Closeable#close()
     */
    @Override
    public void close() throws IOException {
        
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
        if (entitys != null && entitys.size() > 0) {
            for (T t : entitys) {
                counter ++;
                onNextItem(t);
            }
        }
    }

    /* (non-Javadoc)
     * @see com.ivy.freport.ds.IvyDataSource#addListener(com.ivy.freport.ds.IvyDSAccessListener)
     */
    @Override
    public void addListener(IvyDSAccessListener<T> listener) {
        if (listeners == null) {
            listeners = new ArrayList<IvyDSAccessListener<T>>();
        }
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /* (non-Javadoc)
     * @see com.ivy.freport.ds.IvyDataSource#onNextItem(java.lang.Object)
     */
    @Override
    public void onNextItem(T t) {
        if (listeners != null) {
            for (IvyDSAccessListener<T> ivyDSAccessListener : listeners) {
                ivyDSAccessListener.nextElement(t, counter);
            }
        }
    }

}

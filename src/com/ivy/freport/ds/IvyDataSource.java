/* 
 * Copyright (c) 2016, S.F. Express Inc. All rights reserved.
 */

package com.ivy.freport.ds;

import java.io.Closeable;

import com.ivy.freport.writer.xls.IvyDSAccessListener;

/**
 * 描述：
 * 
 * <pre>HISTORY
 * ****************************************************************************
 *  ID   DATE           PERSON          REASON
 *  1    2016年1月25日      Steven.Zhu         Create
 * ****************************************************************************
 * </pre>
 * @author Steven.Zhu
 * @since 
 */

public interface IvyDataSource<T> extends Closeable {

    public boolean hasNext();
    public void next();
    
    public void addListener(IvyDSAccessListener<T> listener);
    public void onNextItem(T t);
}

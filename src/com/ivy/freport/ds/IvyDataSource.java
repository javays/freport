/* 
 * Copyright (c) 2016, S.F. Express Inc. All rights reserved.
 */

package com.ivy.freport.ds;

import java.io.Closeable;
import java.util.function.Consumer;

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

    public int curRow();
    public boolean hasNext();
    public void next();
}

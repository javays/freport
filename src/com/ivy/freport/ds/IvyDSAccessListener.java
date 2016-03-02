/* 
 * Copyright (c) 2015, S.F. Express Inc. All rights reserved.
 */

package com.ivy.freport.ds;


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

public interface IvyDSAccessListener<T> {

    public boolean nextElement(T t, int seq);
}

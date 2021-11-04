package com.xk.cache.support;

import java.io.Serializable;

/**
 * @author xiongkai
 * @version 1.0
 * @date 2021-03-26 10:37
 * 防止缓存穿透的空对象,存入缓存来充当null作用
 */
public class Null implements Serializable {

    private static final long serialVersionUID = 2756779687467644058L;

    public static final Null NULL = new Null();

    protected Object readResolve() {
        return NULL;
    }
}

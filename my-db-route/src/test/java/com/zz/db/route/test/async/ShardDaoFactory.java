package com.zz.db.route.test.async;

import com.zz.db.route.test.async.dao.MokeDaoImpl;
import com.zz.db.route.test.async.proxy.MapperProxyFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhangzuizui
 * @date 2018/3/29 16:09
 */
public class ShardDaoFactory<T> {

    public ShardDaoFactory() {
    }

    private static final Map<Class<?>, MapperProxyFactory<?>> knownClass = new HashMap<Class<?>, MapperProxyFactory<?>>();

    static {
        Class<?> mokeDaoImpl = MokeDaoImpl.class;
        knownClass.put(mokeDaoImpl, new MapperProxyFactory(mokeDaoImpl));
    }

    public T getInstance(Class<T> classType) {
        return (T) knownClass.get(classType).newInstance();
    }
}

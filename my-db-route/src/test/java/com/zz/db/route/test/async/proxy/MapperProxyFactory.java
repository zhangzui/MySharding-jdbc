package com.zz.db.route.test.async.proxy;

import org.apache.ibatis.binding.MapperMethod;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhangzuizui
 * @date 2018/3/29 15:00
 */
public class MapperProxyFactory<T> {

    private final Class<T> mapperInterface;
    private Map<Method, MapperMethod> methodCache = new ConcurrentHashMap<Method, MapperMethod>();

    public MapperProxyFactory(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    public Class<T> getMapperInterface() {
        return mapperInterface;
    }

    public Map<Method, MapperMethod> getMethodCache() {
        return methodCache;
    }

    @SuppressWarnings("unchecked")
    protected T newInstance(MyDynamicProxy<T> mapperProxy) {
        return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), mapperInterface.getInterfaces(), mapperProxy);
    }

    public T newInstance() {
        final MyDynamicProxy<T> myDynamicProxy = new MyDynamicProxy<T>();
        try {
            myDynamicProxy.setTarget(mapperInterface.newInstance());
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return newInstance(myDynamicProxy);
    }
}

package com.zz.db.route.test.async.proxy;

import com.alibaba.fastjson.JSON;
import com.zz.db.route.test.async.annotation.MyAsync;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author zhangzuizui
 * @date 2018/3/29 11:56
 */
public class MyDynamicProxy<T> implements InvocationHandler{
    // 2、创建被代理对象
    private Object target;
    // 3、创建代理对象，参数是要被代理的对象，返回值是代理对象
    public static Object getInstance(Object o) {
        MyDynamicProxy proxy = new MyDynamicProxy();
        proxy.target = o;
        Object result = Proxy.newProxyInstance(o.getClass().getClassLoader(),o.getClass().getInterfaces(), proxy);
        return result;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.isAnnotationPresent(MyAsync.class)) { // 检查该方法上是否有LogInf注解
            MyAsync myAsync = method.getAnnotation(MyAsync.class); // 取得注解
            System.out.println("代理参数"+JSON.toJSONString(args));
        }
        if (Object.class.equals(method.getDeclaringClass())) {
            return method.invoke(this, args);
        }
        Object o = method.invoke(target, args);
        return o;
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }
}

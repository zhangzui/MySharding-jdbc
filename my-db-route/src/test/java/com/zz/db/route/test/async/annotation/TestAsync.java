package com.zz.db.route.test.async.annotation;

import com.zz.db.route.test.async.dao.MokeDao;
import com.zz.db.route.test.async.dao.MokeDaoImpl;
import com.zz.db.route.test.async.proxy.MyDynamicProxy;

/**
 * @author zhangzuizui
 * @date 2018/3/29 11:49
 */
public class TestAsync {

    public static void main(String[] args) {

        MokeDao mokeDao = new MokeDaoImpl();

        MokeDao myDynamicProxy = (MokeDao) MyDynamicProxy.getInstance(mokeDao);

        myDynamicProxy.select("000001");
    }
}

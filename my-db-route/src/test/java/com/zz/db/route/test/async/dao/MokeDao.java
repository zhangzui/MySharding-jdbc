package com.zz.db.route.test.async.dao;

import com.zz.db.route.test.async.annotation.MyAsync;

/**
 * 模拟分库分表分片字段，通过MyAsync注解进行异步处理参数分片结果
 * @author zhangzuizui
 * @date 2018/3/29 12:03
 */
public interface MokeDao {

    @MyAsync
    public void select(String routeFiled);

}

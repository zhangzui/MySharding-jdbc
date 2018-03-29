package com.zz.db.route.test.async;

import com.zz.db.route.test.async.dao.MokeDao;
import com.zz.db.route.test.async.dao.MokeDaoImpl;

/**
 * @author zhangzuizui
 * @date 2018/3/29 15:06
 */
public class TestMapper {

    public static void main(String[] args) {
        MokeDao mokeDao = (MokeDao) new ShardDaoFactory().getInstance(MokeDaoImpl.class);
        mokeDao.select("000001");
    }
}

package com.zz.sharding.test.dao;

import com.zz.sharding.jdbc.bean.Order;
import com.zz.sharding.jdbc.dao.OrderMapper;
import com.zz.sharding.test.common.BuildOrderUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * Created by zhangzuizui on 2018/1/9.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:/shard-datasources.xml"})
public class TestOrderMapper {

    @Autowired
    OrderMapper orderMapper;

    @Test
    public void insertOrder(){
        List<Order> orderList = BuildOrderUtils.getOrderList();
        for (int i = 0; i < orderList.size(); i++) {
            orderMapper.insert(orderList.get(i));
        }
    }
}

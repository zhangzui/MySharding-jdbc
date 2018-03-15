package com.zz.sharding.test;

import com.zz.sharding.jdbc.bean.Order;
import com.zz.sharding.jdbc.dao.OrderMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.List;

/**
 * @author zhangzuizui
 * @date 2018/3/15 15:09
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:/shard-datasources-2.xml"})
public class TestShardingJDBC {

    @Autowired
    private OrderMapper orderMapper;

    @Test
    public void test(){
        List<Order> orderList = BuildOrderUtils.getOrderList();
        for (Order order : orderList){
            orderMapper.insert(order);
        }
    }
}

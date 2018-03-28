package com.zz.sharding.test.shardJDBC;

import com.alibaba.fastjson.JSON;
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
 * @author zhangzuizui
 * @date 2018/3/15 15:09
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:/shard-datasources-mybatis.xml"})
public class TestShardingJDBCMybatis {

    @Autowired
    private OrderMapper orderMapper;

    @Test
    public void testInsert(){
        List<Order> orderList = BuildOrderUtils.getOrderList();
        for (Order order : orderList){
            orderMapper.insert(order);
        }
    }
    @Test
    public void testSelect(){
       List<Order> orderList = orderMapper.selectByPrimaryKey(1L);
        System.out.println(JSON.toJSONString(orderList));
    }
}

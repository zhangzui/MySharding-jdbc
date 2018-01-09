package com.zz.sharding.jdbc.main;

import com.zz.sharding.jdbc.bean.Order;
import com.zz.sharding.jdbc.dao.OrderMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangzuizui on 2018/1/9.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:/shard-datasources.xml"})
public class InitMain {

    @Autowired
    OrderMapper orderMapper;

    @Test
    public void insertOrder(){
        List<Order> orderList = new ArrayList<Order>();
        orderList.add(buildOrder("001"));
        orderList.add(buildOrder("002"));
        orderList.add(buildOrder("003"));
        orderList.add(buildOrder("004"));
        orderList.add(buildOrder("005"));
        for (int i = 0; i < orderList.size(); i++) {
            orderMapper.insert(orderList.get(i));
        }
    }

    private Order buildOrder(String id) {
        Order order = new Order();
        order.setInfo("order_信息");
        order.setOrderId(id);
        return order;
    }
}

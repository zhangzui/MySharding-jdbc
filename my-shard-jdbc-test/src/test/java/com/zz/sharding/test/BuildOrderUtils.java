package com.zz.sharding.test;

import com.zz.sharding.jdbc.bean.Order;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangzuizui
 * @date 2018/3/15 16:12
 */
public class BuildOrderUtils {

    public static List<Order> getOrderList(){
        List<Order> orderList = new ArrayList<Order>();
        orderList.add(buildOrder("001"));
        orderList.add(buildOrder("002"));
        orderList.add(buildOrder("003"));
        orderList.add(buildOrder("004"));
        orderList.add(buildOrder("005"));
        return orderList;
    }
    public static Order buildOrder(String id) {
        Order order = new Order();
        order.setInfo("order_信息");
        order.setOrderId(id);
        return order;
    }
}

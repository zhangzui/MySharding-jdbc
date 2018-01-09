package com.zz.sharding.jdbc.bean;

public class Order {
    /**
     * 
     */
    private Long id;

    /**
     * 
     */
    private String orderId;

    /**
     * 
     */
    private String info;

    /**
     * 
     * @return id 
     */
    public Long getId() {
        return id;
    }

    /**
     * 
     * @param id 
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 
     * @return order_id 
     */
    public String getOrderId() {
        return orderId;
    }

    /**
     * 
     * @param orderId 
     */
    public void setOrderId(String orderId) {
        this.orderId = orderId == null ? null : orderId.trim();
    }

    /**
     * 
     * @return info 
     */
    public String getInfo() {
        return info;
    }

    /**
     * 
     * @param info 
     */
    public void setInfo(String info) {
        this.info = info == null ? null : info.trim();
    }
}
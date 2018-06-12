package com.lstu.kovalchuk.taxiservice;

public class RespCreateOrder {
    private String status;
    private String orderID;

    public RespCreateOrder(String status, String orderID) {
        this.status = status;
        this.orderID = orderID;
    }

    public RespCreateOrder(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }
}

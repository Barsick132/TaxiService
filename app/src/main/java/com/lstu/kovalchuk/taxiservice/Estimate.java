package com.lstu.kovalchuk.taxiservice;

public class Estimate {
    private String OrderUID;
    private Integer EstimateValue;

    public Estimate(String orderUID, Integer estimateValue) {
        OrderUID = orderUID;
        EstimateValue = estimateValue;
    }

    public String getOrderUID() {
        return OrderUID;
    }

    public void setOrderUID(String orderUID) {
        OrderUID = orderUID;
    }

    public Integer getEstimateValue() {
        return EstimateValue;
    }

    public void setEstimateValue(Integer estimateValue) {
        EstimateValue = estimateValue;
    }
}

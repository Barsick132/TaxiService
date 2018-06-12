package com.lstu.kovalchuk.taxiservice;

public class RespCounterCost {
    private String status;
    private Integer cost;
    private Integer time;
    private Integer distance;
    private String points;

    public RespCounterCost(String status, Integer cost, Integer time, Integer distance) {
        this.status = status;
        this.cost = cost;
        this.time = time;
        this.distance = distance;
    }

    public RespCounterCost(String status, String points) {
        this.status = status;
        this.points = points;
    }

    public RespCounterCost(String status) {
        this.status = status;
    }

    public RespCounterCost(String status, Integer cost) {
        this.status = status;
        this.cost = cost;
    }

    public String getStatus() {
        return status;
    }

    public Integer getCost() {
        return cost;
    }

    public void setCost(Integer cost) {
        this.cost = cost;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public Integer getDistance() {
        return distance;
    }

    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    public String getPoints() {
        return points;
    }

    public void setPoints(String points) {
        this.points = points;
    }
}

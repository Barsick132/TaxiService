package com.lstu.kovalchuk.taxiservice;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

public class Order {
    private String ID;
    private String driverUID;
    private String clientUID;
    private String estimateUID;
    private GeoPoint whenceGeoPoint;
    private String whenceAddress;
    private GeoPoint whereGeoPoint;
    private String whereAddress;
    private boolean cashlessPay;
    private String comment;
    private Integer approxCost;
    private Integer approxTimeToDest; // приблизительное время в секундах
    private Integer approxDistanceToDest; // приблизительное расстояние в метрах
    private boolean cancel;
    private boolean driverArrived;
    private boolean clientCameOut;
    private Timestamp DTbegin;
    private Timestamp DTend;
    private Integer totalCost;
    private Integer timeWaiting; // время ожидания в секундах

    public Order() {
    }

    public void assembleOrder(String ID, String clientUID, GeoPoint whenceGeoPoint, String whenceAddress,
                              GeoPoint whereGeoPoint, String whereAddress,
                              boolean cashlessPay, String comment) {
        this.ID = ID;
        this.clientUID = clientUID;
        this.whenceGeoPoint = whenceGeoPoint;
        this.whenceAddress = whenceAddress;
        this.whereGeoPoint = whereGeoPoint;
        this.whereAddress = whereAddress;
        this.cashlessPay = cashlessPay;
        this.comment = comment;
    }

    public String getDriverUID() {
        return driverUID;
    }

    public void setDriverUID(String driverUID) {
        this.driverUID = driverUID;
    }

    public String getClientUID() {
        return clientUID;
    }

    public void setClientUID(String clientUID) {
        this.clientUID = clientUID;
    }

    public String getEstimateUID() {
        return estimateUID;
    }

    public void setEstimateUID(String estimateUID) {
        this.estimateUID = estimateUID;
    }

    public GeoPoint getWhenceGeoPoint() {
        return whenceGeoPoint;
    }

    public void setWhenceGeoPoint(GeoPoint whenceGeoPoint) {
        this.whenceGeoPoint = whenceGeoPoint;
    }

    public String getWhenceAddress() {
        return whenceAddress;
    }

    public void setWhenceAddress(String whenceAddress) {
        this.whenceAddress = whenceAddress;
    }

    public GeoPoint getWhereGeoPoint() {
        return whereGeoPoint;
    }

    public void setWhereGeoPoint(GeoPoint whereGeoPoint) {
        this.whereGeoPoint = whereGeoPoint;
    }

    public String getWhereAddress() {
        return whereAddress;
    }

    public void setWhereAddress(String whereAddress) {
        this.whereAddress = whereAddress;
    }

    public boolean isCashlessPay() {
        return cashlessPay;
    }

    public void setCashlessPay(boolean cashlessPay) {
        this.cashlessPay = cashlessPay;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Integer getApproxCost() {
        return approxCost;
    }

    public void setApproxCost(Integer approxCost) {
        this.approxCost = approxCost;
    }

    public Integer getApproxTimeToDest() {
        return approxTimeToDest;
    }

    public void setApproxTimeToDest(Integer approxTimeToDest) {
        this.approxTimeToDest = approxTimeToDest;
    }

    public Integer getApproxDistanceToDest() {
        return approxDistanceToDest;
    }

    public void setApproxDistanceToDest(Integer approxDistanceToDest) {
        this.approxDistanceToDest = approxDistanceToDest;
    }

    public boolean isCancel() {
        return cancel;
    }

    public void setCancel(boolean cancel) {
        this.cancel = cancel;
    }

    public boolean isDriverArrived() {
        return driverArrived;
    }

    public void setDriverArrived(boolean driverArrived) {
        this.driverArrived = driverArrived;
    }

    public boolean isClientCameOut() {
        return clientCameOut;
    }

    public void setClientCameOut(boolean clientCameOut) {
        this.clientCameOut = clientCameOut;
    }

    public Timestamp getDTbegin() {
        return DTbegin;
    }

    public void setDTbegin(Timestamp DTbegin) {
        this.DTbegin = DTbegin;
    }

    public Timestamp getDTend() {
        return DTend;
    }

    public void setDTend(Timestamp DTend) {
        this.DTend = DTend;
    }

    public Integer getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(Integer totalCost) {
        this.totalCost = totalCost;
    }

    public Integer getTimeWaiting() {
        return timeWaiting;
    }

    public void setTimeWaiting(Integer timeWaiting) {
        this.timeWaiting = timeWaiting;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }
}

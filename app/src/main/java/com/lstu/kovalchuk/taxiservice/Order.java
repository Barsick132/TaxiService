package com.lstu.kovalchuk.taxiservice;

import java.sql.Time;
import java.util.Date;

public class Order{
    public int ID;
    public int driverID;
    public int clientID;
    public int estimateID;
    public String whence;
    public String where;
    public boolean cashlessPay;
    public String comment;
    public int approxCost;
    public Time approxTimeToDest;
    public Time approxDistanceToDest;
    public boolean cancel;
    public boolean driverArrived;
    public boolean clientCameOut;
    public Date DTbegin;
    public Date DTend;
    public int totalCost;
    public Time timeWaiting;
}

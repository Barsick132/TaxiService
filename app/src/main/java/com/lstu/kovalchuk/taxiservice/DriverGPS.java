package com.lstu.kovalchuk.taxiservice;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

public class DriverGPS {
    private Timestamp DT;
    private GeoPoint geoPoint;

    public Timestamp getDT() {
        return DT;
    }

    public void setDT(Timestamp DT) {
        this.DT = DT;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }
}

package com.lstu.kovalchuk.taxiservice;

public class Client {
    private String FullName;
    private String Phone;

    public Client() {
    }

    public Client(String fullName, String phone) {
        FullName = fullName;
        Phone = phone;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getFullName() {
        return FullName;
    }

    public void setFullName(String fullName) {
        FullName = fullName;
    }
}

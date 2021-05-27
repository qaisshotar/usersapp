package com.example.handygit;

import com.google.firebase.firestore.GeoPoint;

public class User {

    private String fullname,email,password,phone;
    private String type;
    private GeoPoint latLng;
    public User() {
    }

    public User(String fullname, String email,
                String password,
                String phone , String type , GeoPoint latLng) {
        this.fullname = fullname;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.type=type;
        this.latLng=latLng;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public GeoPoint getLatLng() {
        return latLng;
    }

    public void setLatLng(GeoPoint latLng) {
        this.latLng = latLng;
    }
}

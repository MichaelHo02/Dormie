package com.michael.dormie.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class User implements Serializable {
    private String uid;
    private String name;
    private String email;
    private String avatar;
    private String role;
    private String dob;
    private boolean isPromoted;
    private Date expiryDate;

    public User() {
        isPromoted = false;
        expiryDate = null;
    }

    public User(String uid, String name, String email, String avatar, String role, String dob, boolean isPromoted, Date expiryDate) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.avatar = avatar;
        this.role = role;
        this.dob = dob;
        this.isPromoted = isPromoted;
        this.expiryDate = expiryDate;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public boolean isPromoted() {
        return isPromoted;
    }

    public void setPromoted(boolean promoted) {
        isPromoted = promoted;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean afterExpiryDate() {
        return isPromoted && Calendar.getInstance().getTime().after(expiryDate);
    }
}

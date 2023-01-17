package com.michael.dormie.model;

import java.util.Calendar;
import java.util.Date;

public class User {
    private String role;
    private String dob;
    private boolean isPromoted;
    private Date expiryDate;

    public User() {
        isPromoted = false;
        expiryDate = null;
    }

    public User(String role, String dob, boolean isPromoted, Date expiryDate) {
        this.role = role;
        this.dob = dob;
        this.isPromoted = isPromoted;
        this.expiryDate = expiryDate;
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

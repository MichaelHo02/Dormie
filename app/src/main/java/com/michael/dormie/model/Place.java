package com.michael.dormie.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Place implements Serializable {
    private String uid;
    private String authorId;
    private String authorRef;
    private String name;
    private Location location;
    //    public Integer rating;
    private String description;
    private List<String> images;
    private String houseType;
    private List<String> amenities;
    private boolean isPromoted;
    private Date expiryDate;

    public static class Location implements Serializable {
        public String name;
        public String address;
        public Double lat;
        public Double lng;
        public String geoHash;

        public Location() {}

        public Location(String name, String address, Double lat, Double lng, String geoHash) {
            this.name = name;
            this.address = address;
            this.lat = lat;
            this.lng = lng;
            this.geoHash = geoHash;
        }
    }

    public Place() {
        images = new ArrayList<>();
        amenities = new ArrayList<>();
        isPromoted = false;
        expiryDate = null;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getAuthorRef() {
        return authorRef;
    }

    public void setAuthorRef(String authorRef) {
        this.authorRef = authorRef;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getImages() {
        return images;
    }

    public void addImage(String url) {
        images.add(url);
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public String getHouseType() {
        return houseType;
    }

    public void setHouseType(String houseType) {
        this.houseType = houseType;
    }

    public List<String> getAmenities() {
        return amenities;
    }

    public void setAmenities(List<String> amenities) {
        this.amenities = amenities;
    }

    public void addAmenity(String amenity) {
        amenities.add(amenity);
    }

    public void removeAmenity(String amenity) {
        amenities.remove(amenity);
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
}

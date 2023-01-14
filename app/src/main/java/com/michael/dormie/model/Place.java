package com.michael.dormie.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.DocumentReference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Place implements Serializable {
    private String authorId;
    private String authorRef;
    private String name;
    private Location location;
    //    public Integer rating;
    private String description;
    private List<String> images;
    //    public PairValue[] bathroom;
//    public PairValue[] bedroom;
    private String houseType;
    private List<String> amenities;
//    public PairValue[] furniture;

    public static class PairValue implements Serializable {
        public Integer qty;
        public String type;

        public PairValue() {}

        public PairValue(Integer qty, String type) {
            this.qty = qty;
            this.type = type;
        }
    }

    public static class Location implements Serializable {
        public String name;
        public String address;
        public Double lat;
        public Double lng;

        public Location() {}

        public Location(String name, String address, Double lat, Double lng) {
            this.name = name;
            this.address = address;
            this.lat = lat;
            this.lng = lng;
        }
    }

    public Place() {
        images = new ArrayList<>();
        amenities = new ArrayList<>();
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
}

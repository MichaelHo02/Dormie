package com.michael.dormie.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.DocumentReference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Place implements Serializable {
    public String authorId;
    public String authorRef;
    public String name;
    public Location location;
//    public Integer rating;
    public String description;
    public List<String> images = new ArrayList<>();
//    public PairValue[] bathroom;
//    public PairValue[] bedroom;
//    public String houseType;
//    public String[] amenities;
//    public PairValue[] furniture;

    public static class PairValue implements Serializable {
        public Integer qty;
        public String type;

        public PairValue(Integer qty, String type) {
            this.qty = qty;
            this.type = type;
        }
    }

    public static class Location implements Serializable{
        public String name;
        public String address;
        public Double lat;
        public Double lng;

        public Location(String name, String address, Double lat, Double lng) {
            this.name = name;
            this.address = address;
            this.lat = lat;
            this.lng = lng;
        }
    }
}

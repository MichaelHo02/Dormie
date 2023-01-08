package com.michael.dormie.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.DocumentReference;

import java.io.Serializable;

public class Place implements Serializable {
    public String authorId;
    public DocumentReference authorRef;
    public String name;
    public Location location;
    public Integer rating;
    public String description;
    public String[] images;
    public Float[] coordinate;
    public PairValue[] bathroom;
    public PairValue[] bedroom;
    public String houseType;
    public String[] amenities;
    public PairValue[] furniture;

    private static class PairValue {
        Integer qty;
        String type;
    }

    public static class Location {
        String name;
        String address;
        LatLng latlng;

        public Location(String name, String address, LatLng latlng) {
            this.name = name;
            this.address = address;
            this.latlng = latlng;
        }
    }


}

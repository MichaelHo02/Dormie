package com.michael.dormie.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Tenant implements Serializable {
    private List<String> houseTypes;
    private List<String> amenities;
    private Location school;
    private int minDistance;
    private int maxDistance;

    public Tenant() {
        houseTypes = new ArrayList<>();
        amenities = new ArrayList<>();
    }

    public Tenant(List<String> houseTypes, List<String> amenities, Location school, int minDistance, int maxDistance) {
        this.houseTypes = houseTypes;
        this.amenities = amenities;
        this.school = school;
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
    }

    public List<String> getHouseTypes() {
        return houseTypes;
    }

    public void addHouseType(String type) {
        if (!houseTypes.contains(type))
            houseTypes.add(type);
    }

    public void removeHouseType(String type) {
        houseTypes.remove(type);
    }

    public void setHouseTypes(List<String> houseTypes) {
        this.houseTypes = houseTypes;
    }

    public List<String> getAmenities() {
        return amenities;
    }

    public void addAmenity(String amenity) {
        if (!amenities.contains(amenity))
            amenities.add(amenity);
    }

    public void removeAmenity(String amenity) {
        amenities.remove(amenity);
    }

    public void setAmenities(List<String> amenities) {
        this.amenities = amenities;
    }

    public Location getSchool() {
        return school;
    }

    public void setSchool(Location school) {
        this.school = school;
    }

    public int getMinDistance() {
        return minDistance;
    }

    public void setMinDistance(int minDistance) {
        this.minDistance = minDistance;
    }

    public int getMaxDistance() {
        return maxDistance;
    }

    public void setMaxDistance(int maxDistance) {
        this.maxDistance = maxDistance;
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

        public Location() {}
    }
}
package com.michael.dormie.model;

public class Place {
    String name;
    String address;
    Integer rating;
    String description;
    String[] images;
    Float[] coordinate;
    PairValue[] bathroom;
    PairValue[] bedroom;
    String houseType;
    String[] amenities;
    PairValue[] furniture;

    private static class PairValue {
        Integer qty;
        String type;
    }
}

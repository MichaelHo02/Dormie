package com.michael.dormie.recyclerview;

public class RentalRegistrationCard {

    private String cardTitle;
    private String cardContent;
    private boolean isOn;

    public RentalRegistrationCard(String cardTitle, String cardContent, boolean isOn) {
        this.cardTitle = cardTitle;
        this.cardContent = cardContent;
        this.isOn = isOn;
    }

    public String getCardTitle() {
        return cardTitle;
    }

    public void setCardTitle(String cardTitle) {
        this.cardTitle = cardTitle;
    }

    public String getCardContent() {
        return cardContent;
    }

    public void setCardContent(String cardContent) {
        this.cardContent = cardContent;
    }

    public boolean isOn() {
        return isOn;
    }

    public void setOn(boolean on) {
        isOn = on;
    }

}

package com.michael.dormie.recyclerview;

public class ProfileCard {
    private String cardTitle;
    private String cardContent;

    public ProfileCard(String cardTitle, String cardContent) {
        this.cardTitle = cardTitle;
        this.cardContent = cardContent;
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
}

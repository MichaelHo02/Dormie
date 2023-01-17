package com.michael.dormie.model;

import java.io.Serializable;

public class Message implements Serializable {
    private String chatId;
    private String content;
    private String senderId;
    private String timestamp;

    public Message(String chatId, String content, String senderId) {
        this.chatId = chatId;
        this.content = content;
        this.senderId = senderId;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

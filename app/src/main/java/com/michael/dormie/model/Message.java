package com.michael.dormie.model;

import java.io.Serializable;
import java.util.Date;

public class Message implements Serializable {
    private String content;
    private String senderId;
    private long timestamp;

    public Message() {
    }

    public Message(String content, String senderId) {
        this.content = content;
        this.senderId = senderId;
        this.timestamp = new Date().getTime();
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

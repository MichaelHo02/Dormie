package com.michael.dormie.model;

import java.io.Serializable;

public class Message implements Serializable {
    private String content;
    private String userId;
    private String timestamp;

    public Message(String content, String userId, String timestamp) {
        this.content = content;
        this.userId = userId;
        this.timestamp = timestamp;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

package com.michael.dormie.model;

import java.io.Serializable;
import java.util.List;

public class Chat implements Serializable {
    private String chatId;
    private List<Message> content;
    private String sentId;

    public Chat(String chatId, List<Message> content, String sentId) {
        this.chatId = chatId;
        this.content = content;
        this.sentId = sentId;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public List<Message> getContent() {
        return content;
    }

    public void setContent(List<Message> content) {
        this.content = content;
    }

    public String getSentId() {
        return sentId;
    }

    public void setSentId(String sentId) {
        this.sentId = sentId;
    }
}

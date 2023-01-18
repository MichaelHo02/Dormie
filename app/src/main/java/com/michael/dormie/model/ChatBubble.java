package com.michael.dormie.model;

import java.util.Date;

public class ChatBubble {
    private String chatBubbleId;
    private String content;
    private String chatRoomId;
    private String personId;
    private Date timestamp;

    public ChatBubble() {
    }

    public ChatBubble(String chatBubbleId, String content, String chatRoomId, String personId, Date timestamp) {
        this.chatBubbleId = chatBubbleId;
        this.content = content;
        this.chatRoomId = chatRoomId;
        this.personId = personId;
        this.timestamp = timestamp;
    }

    public String getChatBubbleId() {
        return chatBubbleId;
    }

    public void setChatBubbleId(String chatBubbleId) {
        this.chatBubbleId = chatBubbleId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(String chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}

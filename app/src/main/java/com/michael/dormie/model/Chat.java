package com.michael.dormie.model;

import java.util.List;

public class Chat {
    private List<String> userIds;
    private List<Message> messages;

    public Chat(List<String> userIds, List<Message> messages) {
        this.userIds = userIds;
        this.messages = messages;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}

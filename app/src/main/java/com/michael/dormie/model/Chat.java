package com.michael.dormie.model;

import java.io.Serializable;
import java.util.List;

public class Chat implements Serializable {
    private List<String> userList;
    private List<Message> messages;

    public Chat() {
    }

    public Chat(List<String> userList, List<Message> messages) {
        this.userList = userList;
        this.messages = messages;
    }

    public List<String> getUserList() {
        return userList;
    }

    public void setUserList(List<String> userList) {
        this.userList = userList;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}

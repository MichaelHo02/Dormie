package com.michael.dormie.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class ChatRoom implements Serializable {
    private String uid;
    private boolean didNotify;
    private String latestMessage;
    private String latestMessageSender;
    private Date timeStamp;
    private boolean isUnread;
    private List<String> userIds;

    public ChatRoom() {
    }

    public ChatRoom(String uid, List<String> userIds) {
        this.uid = uid;
        this.userIds = userIds;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public boolean isDidNotify() {
        return didNotify;
    }

    public void setDidNotify(boolean didNotify) {
        this.didNotify = didNotify;
    }

    public String getLatestMessage() {
        return latestMessage;
    }

    public void setLatestMessage(String latestMessage) {
        this.latestMessage = latestMessage;
    }

    public String getLatestMessageSender() {
        return latestMessageSender;
    }

    public void setLatestMessageSender(String latestMessageSender) {
        this.latestMessageSender = latestMessageSender;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public boolean isUnread() {
        return isUnread;
    }

    public void setUnread(boolean unread) {
        isUnread = unread;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }
}

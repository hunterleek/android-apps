package com.app.model;

public class Message {
    private String text;
    private boolean isSent;
    private long timestamp;

    public Message(String text, boolean isSent, long timestamp) {
        this.text = text;
        this.isSent = isSent;
        this.timestamp = timestamp;
    }

    public String getText() {
        return text;
    }

    public boolean isSent() {
        return isSent;
    }

    public long getTimestamp() {
        return timestamp;
    }
}

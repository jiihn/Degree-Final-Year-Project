package com.example.fyp.Model;

public class Chatlist {
    public String id;
    public String timestamp;

    public Chatlist(String id, String timestamp) {
        this.id = id;
        this.timestamp = timestamp;
    }

    public Chatlist() {
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

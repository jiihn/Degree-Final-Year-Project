package com.example.fyp.Model;

public class Purchaselist {
    String id;
    String timestamp;

    public Purchaselist() {
    }

    public Purchaselist(String id, String timestamp) {
        this.id = id;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

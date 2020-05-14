package com.example.fyp.Model;

public class Rating {
    String orderId;
    String ratingId;
    String comment;
    String commenter;
    String timestamp;
    Float rating;

    public Rating() {
    }

    public Rating(String orderId, String ratingId, String comment, String commenter, String timestamp, Float rating) {
        this.orderId = orderId;
        this.ratingId = ratingId;
        this.comment = comment;
        this.commenter = commenter;
        this.timestamp = timestamp;
        this.rating = rating;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getCommenter() {
        return commenter;
    }

    public void setCommenter(String commenter) {
        this.commenter = commenter;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getRatingId() {
        return ratingId;
    }

    public void setRatingId(String ratingId) {
        this.ratingId = ratingId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Float getRating() {
        return rating;
    }

    public void setRating(Float rating) {
        this.rating = rating;
    }
}

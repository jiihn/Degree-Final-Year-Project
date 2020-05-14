package com.example.fyp.Model;

import android.content.Context;

import java.util.List;

public class Order {
    String buyer;
    String orderId;
    String seller;
    String serviceId;
    String timestamp;
    String paidTimestamp;
    String completeTimestamp;
    String acceptTimestamp;
    String dueTimestamp;
    String ratingId;
    boolean complete;
    boolean verified;
    boolean paid;
    boolean cancel;
    boolean rated;
    boolean transfer;

    public Order() {
    }

    public Order(String buyer, String orderId, String seller, String serviceId, String timestamp, String acceptTimestamp, String paidTimestamp, String completeTimestamp, String dueTimestamp, String ratingId, boolean complete, boolean paid, boolean cancel, boolean verified, boolean rated, boolean transfer) {
        this.buyer = buyer;
        this.orderId = orderId;
        this.seller = seller;
        this.serviceId = serviceId;
        this.timestamp = timestamp;
        this.acceptTimestamp = acceptTimestamp;
        this.paidTimestamp = paidTimestamp;
        this.completeTimestamp = completeTimestamp;
        this.dueTimestamp = dueTimestamp;
        this.ratingId = ratingId;
        this.complete = complete;
        this.paid = paid;
        this.cancel = cancel;
        this.verified = verified;
        this.rated = rated;
        this.transfer = transfer;
    }

    public boolean isTransfer() {
        return transfer;
    }

    public void setTransfer(boolean transfer) {
        this.transfer = transfer;
    }

    public String getAcceptTimestamp() {
        return acceptTimestamp;
    }

    public void setAcceptTimestamp(String acceptTimestamp) {
        this.acceptTimestamp = acceptTimestamp;
    }

    public String getDueTimestamp() {
        return dueTimestamp;
    }

    public void setDueTimestamp(String dueTimestamp) {
        this.dueTimestamp = dueTimestamp;
    }

    public String getRatingId() {
        return ratingId;
    }

    public void setRatingId(String ratingId) {
        this.ratingId = ratingId;
    }

    public boolean isRated() {
        return rated;
    }

    public void setRated(boolean rated) {
        this.rated = rated;
    }

    public String getCompleteTimestamp() {
        return completeTimestamp;
    }

    public void setCompleteTimestamp(String completeTimestamp) {
        this.completeTimestamp = completeTimestamp;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getBuyer() {
        return buyer;
    }

    public void setBuyer(String buyer) {
        this.buyer = buyer;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getPaidTimestamp() {
        return paidTimestamp;
    }

    public void setPaidTimestamp(String paidTimestamp) {
        this.paidTimestamp = paidTimestamp;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public boolean isCancel() {
        return cancel;
    }

    public void setCancel(boolean cancel) {
        this.cancel = cancel;
    }
}

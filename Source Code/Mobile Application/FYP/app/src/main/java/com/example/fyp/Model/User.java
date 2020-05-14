package com.example.fyp.Model;

public class User {

    private String id;
    private String first_name, last_name;
    private String imageURL;
    private String status;
    private String address;
    private String city;
    private String state;
    private String postcode;
    private String phone_number;
    private String email;
    private String icURL;
    private String icNum;
    private boolean verified;
    private boolean rejected;
    private Float ewallet;

    public User(Float ewallet, String id, String email, String first_name, String last_name, String imageURL, String status, String icURL, String icNum, boolean verified, boolean rejected, String address, String city, String state, String postcode, String phone_number) {
        this.id = id;
        this.ewallet = ewallet;
        this.email = email;
        this.first_name = first_name;
        this.last_name = last_name;
        this.imageURL = imageURL;
        this.status = status;
        this.address = address;
        this.city = city;
        this.icURL = icURL;
        this.icNum = icNum;
        this.verified = verified;
        this.rejected = rejected;
        this.state = state;
        this.postcode = postcode;
        this.phone_number = phone_number;
    }

    public User() {
    }

    public boolean isRejected() {
        return rejected;
    }

    public void setRejected(boolean rejected) {
        this.rejected = rejected;
    }

    public String getIcURL() {
        return icURL;
    }

    public void setIcURL(String icURL) {
        this.icURL = icURL;
    }

    public String getIcNum() {
        return icNum;
    }

    public void setIcNum(String icNum) {
        this.icNum = icNum;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public Float getEwallet() {
        return ewallet;
    }

    public void setEwallet(Float ewallet) {
        this.ewallet = ewallet;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

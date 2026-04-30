package com.example.milkteamanagement.models;

public class User {
    private String uid;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String address;
    private String role;
    private String gender; // "Nam" hoặc "Nữ"
    private String bannerUrl;
    private String avatarUrl;

    public User() {
    }

    public User(String uid, String fullName, String email, String phoneNumber, String address, String role, String gender, String bannerUrl, String avatarUrl) {
        this.uid = uid;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.role = role;
        this.gender = gender;
        this.bannerUrl = bannerUrl;
        this.avatarUrl = avatarUrl;
    }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getBannerUrl() { return bannerUrl; }
    public void setBannerUrl(String bannerUrl) { this.bannerUrl = bannerUrl; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
}

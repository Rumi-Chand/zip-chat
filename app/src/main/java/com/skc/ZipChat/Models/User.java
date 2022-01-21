package com.skc.ZipChat.Models;

public class User {

    private String uid, name, phoneNumber, profileImage, aboutInfo, token;

    public User() {

    }

    public User(String uid, String name, String phoneNumber, String profileImage, String aboutInfo) {
        this.uid = uid;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.profileImage = profileImage;
        this.aboutInfo = aboutInfo;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public String getAboutInfo() {
        return aboutInfo;
    }

    public String getToken() {
        return token;
    }
}

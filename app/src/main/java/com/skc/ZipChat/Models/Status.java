package com.skc.ZipChat.Models;

public class Status {
    private String imageUrl;
    private long timeStamp;

    public Status() {
    }

    public Status(String imageUrl, long timeStamp) {
        this.imageUrl = imageUrl;
        this.timeStamp = timeStamp;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public long getTimeStamp() {
        return timeStamp;
    }
}

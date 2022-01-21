package com.skc.ZipChat.Models;

import java.util.ArrayList;

public class UserStatus {
    private String name, uid;
    private long lastUpdated;
    private ArrayList<Status> statuses;

    public UserStatus() {
    }

    public UserStatus(String name, String uid, long lastUpdated, ArrayList<Status> statuses) {
        this.name = name;
        this.uid = uid;
        this.lastUpdated = lastUpdated;
        this.statuses = statuses;
    }

    public ArrayList<Status> getStatuses() {
        return statuses;
    }

    public void setStatuses(ArrayList<Status> statuses) {
        this.statuses = statuses;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }
}

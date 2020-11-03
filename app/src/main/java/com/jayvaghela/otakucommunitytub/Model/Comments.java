package com.jayvaghela.otakucommunitytub.Model;

public class Comments {

    private String comment;
    private String username;
    private String userimage;
    private String time;
    private String UID;
    private long timestamp;

    public Comments() {
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getComment() {
        return comment;
    }

    public String getUsername() {
        return username;
    }

    public String getUserimage() {
        return userimage;
    }

    public String getTime() {
        return time;
    }

    public String getUID() {
        return UID;
    }
}

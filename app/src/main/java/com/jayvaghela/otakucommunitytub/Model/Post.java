package com.jayvaghela.otakucommunitytub.Model;

public class Post {
    private String image,text,username,userimage,postkey,UID,time;
    private long timestamp;

    public Post() {
    }

    public String getImage() {
        return image;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getText() {
        return text;
    }

    public String getUsername() {
        return username;
    }

    public String getUserimage() {
        return userimage;
    }

    public String getPostkey() {
        return postkey;
    }

    public String getUID() {
        return UID;
    }

    public String getTime() {
        return time;
    }
}
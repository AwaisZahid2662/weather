package com.example.weather;

public class User {

    private String name, email, uid, Imageurl;

    public User() {
    }

    public User(String name, String email, String uid, String imageurl) {
        this.name = name;
        this.email = email;
        this.uid = uid;
        Imageurl = imageurl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getImageurl() {
        return Imageurl;
    }

    public void setImageurl(String imageurl) {
        Imageurl = imageurl;
    }
}

package com.learningandroid.journal.model;

import com.google.firebase.Timestamp;

public class Journal {
    private String title;
    private String thought;
    private String imageUri;
    private String userId;
    private String userName;
    private Timestamp timeAdded;

    public Journal() {

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThought() {
        return thought;
    }

    public void setThought(String thought) {
        this.thought = thought;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Timestamp getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(Timestamp timeAdded) {
        this.timeAdded = timeAdded;
    }

    public Journal(String title, String thought, String imageUri, String userId, String userName, Timestamp timeAdded) {
        this.title = title;
        this.thought = thought;
        this.imageUri = imageUri;
        this.userId = userId;
        this.userName = userName;
        this.timeAdded = timeAdded;
    }
}
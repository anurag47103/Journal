package com.learningandroid.journal.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.GeoPoint;

public class Journal implements Parcelable {

    @Exclude private String journalId;
    private String title;
    private String thought;
    private String imageUri;
    private String userId;
    private String userName;
    private Timestamp timeAdded;
    private GeoPoint geoPoint;

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

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

    public Journal(String title, String thought, String imageUri, String userId, String userName, Timestamp timeAdded, GeoPoint geoPoint) {
        this.title = title;
        this.thought = thought;
        this.imageUri = imageUri;
        this.userId = userId;
        this.userName = userName;
        this.timeAdded = timeAdded;
        this.geoPoint = geoPoint;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(journalId);
        parcel.writeString(title);
        parcel.writeString(thought);
        parcel.writeString(imageUri);
        parcel.writeString(userId);
        parcel.writeParcelable(timeAdded, i);
        parcel.writeString(userName);
        if(geoPoint!=null) {
            parcel.writeDouble(geoPoint.getLatitude());
            parcel.writeDouble(geoPoint.getLongitude());
        }
    }

    public static final Parcelable.Creator<Journal> CREATOR = new Parcelable.Creator<Journal>() {
        public Journal createFromParcel(Parcel in) {
            return new Journal(in);
        }
        public Journal[] newArray(int size) {
            return new Journal[size];
        }
    };

    public String getJournalId() {
        return journalId;
    }

    public void setJournalId(String journalId) {
        this.journalId = journalId;
    }

    private Journal(Parcel in) {
        journalId = in.readString();
        title = in.readString();
        thought = in.readString();
        imageUri = in.readString();
        userId = in.readString();
        timeAdded = in.readParcelable(Timestamp.class.getClassLoader());
        userName = in.readString();
        if(geoPoint!=null) {
            Double lat = in.readDouble();
            Double lng = in.readDouble();
            geoPoint = new GeoPoint(lat, lng);
        }
    }
}
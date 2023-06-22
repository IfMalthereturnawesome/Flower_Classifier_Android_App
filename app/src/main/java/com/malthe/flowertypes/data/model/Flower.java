package com.malthe.flowertypes.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.sql.Timestamp;

public class Flower  {
    private String botanicalName;
    private String plantType;
    private String plantHeight;
    private String description;
    private String flowerName;

    private String imageUrl;

    private String documentId;

    private double longitude;

    private double latitude;

    private com.google.firebase.Timestamp classificationDate;

    private boolean isFavorite;

    private String userId;


    public Flower() {

    }

    public Flower(String flowerName) {
        this.flowerName = flowerName;
    }


    public Flower(String flowerName, String botanicalName, String plantType, String plantHeight, String description, String imageUrl, double longitude, double latitude, com.google.firebase.Timestamp classificationDate, boolean isFavorite, String userId) {
        this.flowerName = flowerName;
        this.botanicalName = botanicalName;
        this.plantType = plantType;
        this.plantHeight = plantHeight;
        this.description = description;
        this.imageUrl = imageUrl;
        this.longitude = longitude;
        this.latitude = latitude;
        this.classificationDate = classificationDate;
        this.isFavorite = isFavorite;
        this.userId = userId;


    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public com.google.firebase.Timestamp getClassificationDate() {
        return classificationDate;
    }


    public void setClassificationDate(com.google.firebase.Timestamp now) {
        this.classificationDate = now;

    }



    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }


    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getBotanicalName() {
        return botanicalName;
    }

    public String getPlantType() {
        return plantType;
    }

    public String getPlantHeight() {
        return plantHeight;
    }

    public String getDescription() {
        return description;
    }

    public String getFlowerName() {
        return flowerName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setBotanicalName(String botanicalName) {
        this.botanicalName = botanicalName;
    }

    public void setPlantType(String plantType) {
        this.plantType = plantType;
    }

    public void setPlantHeight(String plantHeight) {
        this.plantHeight = plantHeight;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setFlowerName(String flowerName) {
        this.flowerName = flowerName;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }





}

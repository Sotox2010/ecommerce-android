package com.smartsecurity.android.piudonnacouture.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Category implements Parcelable {

    @SerializedName("id")
    private String serverId;

    @SerializedName("nameEn")
    private String name;

    @SerializedName("textEn")
    private String description;

    @SerializedName("filename")
    private String imageUrl;

    public Long getServerId() {
        return Long.parseLong(serverId);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Category() {

    }

    public Category(Long serverId, String name) {
        this.serverId = Long.toString(serverId);
        this.name = name;
    }

    protected Category(Parcel in) {
        serverId = in.readString();
        name = in.readString();
        description = in.readString();
    }

    public static final Creator<Category> CREATOR = new Creator<Category>() {
        @Override
        public Category createFromParcel(Parcel in) {
            return new Category(in);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(serverId);
        dest.writeString(name);
        dest.writeString(description);
    }
}

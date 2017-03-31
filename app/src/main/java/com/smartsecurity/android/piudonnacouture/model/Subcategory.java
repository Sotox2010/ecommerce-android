package com.smartsecurity.android.piudonnacouture.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Subcategory implements Parcelable {

    @SerializedName("id")
    private String serverId;

    @SerializedName("nameEn")
    private String name;

    @SerializedName("filename")
    private String imageUrl;

    @SerializedName("categoryId")
    private String categoryId;

    @SerializedName("category")
    private String categoryName;

    public Subcategory() {

    }

    public Subcategory(Long serverId, String name, String imageUrl, Long categoryId/*, String categoryName*/) {
        this.serverId = Long.toString(serverId);
        this.name = name;
        this.imageUrl = imageUrl;
        this.categoryId = Long.toString(categoryId);
        //this.categoryName = categoryName;
    }

    protected Subcategory(Parcel in) {
        serverId = in.readString();
        name = in.readString();
        categoryId = in.readString();
        categoryName = in.readString();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public Long getServerId() {
        return Long.parseLong(serverId);
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Long getCategoryId() {
        return Long.parseLong(categoryId);
    }

    public String getCategoryName() {
        return categoryName;
    }

    public static final Creator<Subcategory> CREATOR = new Creator<Subcategory>() {
        @Override
        public Subcategory createFromParcel(Parcel in) {
            return new Subcategory(in);
        }

        @Override
        public Subcategory[] newArray(int size) {
            return new Subcategory[size];
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
        dest.writeString(categoryId);
        dest.writeString(categoryName);
    }
}

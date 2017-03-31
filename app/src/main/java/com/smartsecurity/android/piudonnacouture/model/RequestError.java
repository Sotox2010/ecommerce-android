package com.smartsecurity.android.piudonnacouture.model;

import com.google.gson.annotations.SerializedName;

public class RequestError {

    @SerializedName("id")
    private String id;

    @SerializedName("description")
    private String description;

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }
}

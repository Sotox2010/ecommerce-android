package com.smartsecurity.android.piudonnacouture.model;

import com.google.gson.annotations.SerializedName;

public class UserInfo {

    @SerializedName("id")
    private String id;

    @SerializedName("firstName")
    private String firstName;

    @SerializedName("lastName")
    private String lastName;

    @SerializedName("email")
    private String email;

    @SerializedName("companyName")
    private String company;

    @SerializedName("status")
    private String status;

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getCompany() {
        return company;
    }

    public String getStatus() {
        return status;
    }
}

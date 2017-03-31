package com.smartsecurity.android.piudonnacouture.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

public class Address implements Parcelable {

    @SerializedName("id")
    private String serverId;

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("lineOne")
    private String addressLine1;

    @SerializedName("lineTwo")
    private String addressLine2;

    @SerializedName("city")
    private String city;

    @SerializedName("state")
    private String state;

    @SerializedName("country")
    private String country;

    @SerializedName("zip")
    private String zipCode;

    @SerializedName("phoneNumber")
    private String phoneNumber;

    public Address() {

    }

    public Address(long serverId, String fullName, String line1, String line2, String city, String state, String country, String zipCode, String phoneNumber) {
        this.serverId = Long.toString(serverId);
        this.fullName = fullName;
        this.addressLine1 = line1;
        this.addressLine2 = line2;
        this.city = city;
        this.state = state;
        this.country = country;
        this.zipCode = zipCode;
        this.phoneNumber = phoneNumber;
    }

    protected Address(Parcel in) {
        serverId = in.readString();
        fullName = in.readString();
        addressLine1 = in.readString();
        addressLine2 = in.readString();
        city = in.readString();
        state = in.readString();
        country = in.readString();
        zipCode = in.readString();
        phoneNumber = in.readString();
    }

    public static final Creator<Address> CREATOR = new Creator<Address>() {
        @Override
        public Address createFromParcel(Parcel in) {
            return new Address(in);
        }

        @Override
        public Address[] newArray(int size) {
            return new Address[size];
        }
    };

    public Long getServerId() {
        return Long.parseLong(serverId);
    }

    public String getFullName() {
        return fullName;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public String getPrintableAddressLine2() {
        return TextUtils.isEmpty(addressLine2) ? "" : " " + addressLine2;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getCountry() {
        return country;
    }

    public String getZipCode() {
        return zipCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(serverId);
        dest.writeString(fullName);
        dest.writeString(addressLine1);
        dest.writeString(addressLine2);
        dest.writeString(city);
        dest.writeString(state);
        dest.writeString(country);
        dest.writeString(zipCode);
        dest.writeString(phoneNumber);
    }

    @Override
    public String toString() {
        return "Address{" +
                "serverId='" + serverId + '\'' +
                ", fullName='" + fullName + '\'' +
                ", addressLine1='" + addressLine1 + '\'' +
                ", addressLine2='" + addressLine2 + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", country='" + country + '\'' +
                ", zipCode='" + zipCode + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Address address = (Address) o;

        return serverId.equals(address.serverId);

    }
}

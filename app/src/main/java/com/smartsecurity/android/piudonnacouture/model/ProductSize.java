package com.smartsecurity.android.piudonnacouture.model;

import com.google.gson.annotations.SerializedName;

public class ProductSize {

    @SerializedName("id")
    private String serverId;

    @SerializedName("name")
    private String name;

    public ProductSize(Long serverId, String name) {
        this.serverId = Long.toString(serverId);
        this.name = name;
    }

    public Long getServerId() {
        return Long.parseLong(serverId);
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "ProductSize{" +
                "serverId=" + serverId +
                ", name='" + name + '\'' +
                '}';
    }
}

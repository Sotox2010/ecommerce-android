package com.smartsecurity.android.piudonnacouture.model;

import com.google.gson.annotations.SerializedName;

public class Brand {

    @SerializedName("id")
    private String serverId;

    @SerializedName("name")
    private String name;

    public Long getServerId() {
        return Long.parseLong(serverId);
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("Brand[serverId = %s, name = %s]", serverId, name);
    }
}

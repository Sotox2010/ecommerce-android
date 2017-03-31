package com.smartsecurity.android.piudonnacouture.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ProductColor {

    @SerializedName("id")
    private String serverId;

    @SerializedName("name")
    private String name;

    @SerializedName("argb")
    private String hexa;

    private List<String> imageUrls;

    public ProductColor(Long serverId, String name, String hexa) {
        this.serverId = Long.toString(serverId);
        this.name = name;
        this.hexa = hexa;
    }

    public Long getServerId() {
        return Long.parseLong(serverId);
    }

    public String getName() {
        return name;
    }

    public String getHexa() {
        return hexa;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    @Override
    public String toString() {
        return "ProductColor{" +
                "serverId='" + serverId + '\'' +
                ", name='" + name + '\'' +
                ", hexa='" + hexa + '\'' +
                ", imageUrls=" + imageUrls +
                '}';
    }
}

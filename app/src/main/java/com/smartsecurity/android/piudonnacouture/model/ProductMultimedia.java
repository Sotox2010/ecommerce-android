package com.smartsecurity.android.piudonnacouture.model;

import com.google.gson.annotations.SerializedName;

public class ProductMultimedia {

    @SerializedName("id")
    private String serverId;

    @SerializedName("imageURL")
    private String imageUrl;

    @SerializedName("displayOrder")
    private Integer displayOrder;

    @SerializedName("productId")
    private String productId;

    public ProductMultimedia() {

    }

    public ProductMultimedia(Long serverId, String imageUrl, Integer displayOrder, Long productId) {
        this.serverId = Long.toString(serverId);
        this.imageUrl = imageUrl;
        this.displayOrder = displayOrder;
        this.productId = Long.toString(productId);
    }

    public Long getServerId() {
        return Long.parseLong(serverId);
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public Long getProductId() {
        return Long.parseLong(productId);
    }
}

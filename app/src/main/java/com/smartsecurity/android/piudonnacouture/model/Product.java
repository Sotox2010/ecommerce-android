package com.smartsecurity.android.piudonnacouture.model;

import com.google.gson.annotations.SerializedName;

public class Product {

    @SerializedName("id")
    private String serverId;

    @SerializedName("code")
    private String code;

    @SerializedName("nameEn")
    private String name;

    @SerializedName("descriptionEn")
    private String description;

    @SerializedName("sellPrice")
    private String price;

    @SerializedName("leadImageURL")
    private String leadImageUrl;

    @SerializedName("brandId")
    private String brandId;

    @SerializedName("subcategoryId")
    private String subcategoryId;

    @SerializedName("brandName")
    private String brandName;

    @SerializedName("subcategoryNameEn")
    private String subcategoryName;

    @SerializedName("categoryNameEn")
    private String categoryName;

    public Product() {

    }

    public Product(Long serverId, String code, String name, Float price, String leadImageUrl) {
        this.serverId = Long.toString(serverId);
        this.code = code;
        this.name = name;
        this.price = Float.toString(price);
        this.leadImageUrl = leadImageUrl;
    }

    public Long getServerId() {
        return Long.parseLong(serverId);
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Float getPrice() {
        return Float.parseFloat(price);
    }

    public String getBrandId() {
        return brandId;
    }

    public String getSubcategoryId() {
        return subcategoryId;
    }

    public String getBrandName() {
        return brandName;
    }

    public String getSubcategoryName() {
        return subcategoryName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getLeadImageUrl() {
        return leadImageUrl;
    }
}

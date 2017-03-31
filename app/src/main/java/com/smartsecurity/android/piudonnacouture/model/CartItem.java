package com.smartsecurity.android.piudonnacouture.model;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.smartsecurity.android.piudonnacouture.util.DateUtils;

public class CartItem {
    private static final String TAG = "CartItem";

    @SerializedName("cartId")
    private String serverId;

    @SerializedName("productId")
    private String productServerId;

    @SerializedName("quantity")
    private int quantity;

    @SerializedName("date")
    private JsonObject addedOnDate;

    @SerializedName("colorId")
    private String colorServerId;

    @SerializedName("color")
    private String color;

    @SerializedName("sizeId")
    private String sizeServerId;

    @SerializedName("size")
    private String size;

    @SerializedName("leadImageURL")
    private String leadImageUrl;

    public long getServerId() {
        return Long.parseLong(serverId);
    }

    public long getProductServerId() {
        return Long.parseLong(productServerId);
    }

    public int getQuantity() {
        return quantity;
    }

    public String getRfc3339Date() {
        return DateUtils.getRfc3339DateFromMySqlObject(addedOnDate);
    }

    public long getColorServerId() {
        return Long.parseLong(colorServerId);
    }

    public String getColor() {
        return color;
    }

    public long getSizeServerId() {
        return Long.parseLong(sizeServerId);
    }

    public String getSize() {
        return size;
    }

    public String getLeadImageUrl() {
        return leadImageUrl;
    }
}

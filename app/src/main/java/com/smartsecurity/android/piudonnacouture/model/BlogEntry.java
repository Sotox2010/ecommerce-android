package com.smartsecurity.android.piudonnacouture.model;

import com.google.gson.annotations.SerializedName;
import com.smartsecurity.android.piudonnacouture.Config;

public class BlogEntry {

    @SerializedName("id")
    private String serverId;

    @SerializedName("nameEn")
    private String title;

    @SerializedName("textEn")
    private String content;

    @SerializedName("imageURL")
    private String imageUrl;

    public BlogEntry() {

    }

    public BlogEntry(Long serverId, String title, String content, String imageUrl) {
        this.serverId = Long.toString(serverId);
        this.title= title;
        this.content = content;
        this.imageUrl = imageUrl;
    }

    public Long getServerId() {
        return Long.parseLong(serverId);
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getImageUrl() {
        return Config.buildBlogEntryImageUrl(imageUrl);
    }
}

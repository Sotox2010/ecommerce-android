package com.smartsecurity.android.piudonnacouture.model;

import com.google.gson.annotations.SerializedName;

public class AuthTokenResponse {

    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("expires_in")
    private Long expiresIn;

    @SerializedName("token_type")
    private String tokenType;

    @SerializedName("scope")
    private String scope;

    @SerializedName("refresh_token")
    private String refreshToken;

    public String getAccessToken() {
        return accessToken;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getScope() {
        return scope;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AuthTokenResponse[");
        builder.append("accessToken = ");
        builder.append(accessToken);
        builder.append(", expiresIn = ");
        builder.append(expiresIn);
        builder.append(", tokenType = ");
        builder.append(tokenType);
        builder.append(", scope = ");
        builder.append(scope);
        builder.append(", refreshToken = ");
        builder.append(refreshToken);
        builder.append("]");

        return builder.toString();
    }
}

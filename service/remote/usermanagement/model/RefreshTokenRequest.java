package org.tpl.chat.service.remote.usermanagement.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RefreshTokenRequest {
    @JsonProperty("grant_type")
    private String grantType = "refresh_token";
    @JsonProperty("refresh_token")
    private String refreshToken;

    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

}

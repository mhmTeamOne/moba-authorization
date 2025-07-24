package com.mhm.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TokenModel {

    private String access_token;
    private Long expires_in;
    @JsonProperty("refresh_expires_in")
    private int refreshExpiresIn;
    @JsonProperty("token_type")
    private String tokenType;
    @JsonProperty("not-before-policy")
    private int notBeforePolicy;
    private String scope;

}

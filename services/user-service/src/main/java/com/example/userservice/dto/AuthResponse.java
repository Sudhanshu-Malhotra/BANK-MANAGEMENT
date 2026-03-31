package com.example.userservice.dto;

public class AuthResponse {
    private final String accessToken;
    private final String tokenType = "Bearer";
    private final Long userId;
    private final String email;

    public AuthResponse(String accessToken, Long userId, String email) {
        this.accessToken = accessToken;
        this.userId = userId;
        this.email = email;
    }

    public String getAccessToken() { return accessToken; }
    public String getTokenType() { return tokenType; }
    public Long getUserId() { return userId; }
    public String getEmail() { return email; }
}

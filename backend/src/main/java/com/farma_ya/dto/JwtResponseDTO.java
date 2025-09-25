package com.farma_ya.dto;

public class JwtResponseDTO {
    private String accessToken;
    private String refreshToken;
    private String type = "Bearer";
    private String username;
    private String role;
    private long expiresIn;

    public JwtResponseDTO(String accessToken, String refreshToken, String username, String role, long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.username = username;
        this.role = role;
        this.expiresIn = expiresIn;
    }

    // Legacy constructor for backward compatibility
    public JwtResponseDTO(String token, String username, String role) {
        this.accessToken = token;
        this.username = username;
        this.role = role;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getToken() {
        return accessToken; // For backward compatibility
    }

    public void setToken(String token) {
        this.accessToken = token; // For backward compatibility
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }
}
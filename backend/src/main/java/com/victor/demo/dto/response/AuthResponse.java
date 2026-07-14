package com.victor.demo.dto.response;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        UserSummaryResponse user
) {}

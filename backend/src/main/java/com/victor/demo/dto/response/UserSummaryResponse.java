package com.victor.demo.dto.response;

import com.victor.demo.entity.UserRole;

public record UserSummaryResponse(
        Long id,
        String email,
        String fullName,
        UserRole role
) {}

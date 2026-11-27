package com.victor.demo.dto.response;

public record AddressResponse(
        Long id,
        String street,
        String city,
        String state,
        String zipCode,
        String country,
        boolean isDefault
) {}

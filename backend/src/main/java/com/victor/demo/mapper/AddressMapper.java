package com.victor.demo.mapper;

import com.victor.demo.dto.response.AddressResponse;
import com.victor.demo.entity.Address;

public class AddressMapper {

    private AddressMapper() {}

    public static AddressResponse toResponse(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getStreet(),
                address.getCity(),
                address.getState(),
                address.getZipCode(),
                address.getCountry(),
                Boolean.TRUE.equals(address.getIsDefault())
        );
    }
}

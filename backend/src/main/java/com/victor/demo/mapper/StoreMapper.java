package com.victor.demo.mapper;

import com.victor.demo.dto.response.StoreResponse;
import com.victor.demo.entity.Store;

public class StoreMapper {

    private StoreMapper() {}

    public static StoreResponse toResponse(Store store) {
        if (store == null) {
            return null;
        }
        String ownerName = store.getOwner() != null ? store.getOwner().getFullName() : null;
        return new StoreResponse(
                store.getId(),
                store.getName(),
                store.getDescription(),
                ownerName,
                store.getCreatedAt()
        );
    }
}

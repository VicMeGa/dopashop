package com.victor.demo.repository;

import com.victor.demo.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {
    Optional<Store> findByOwnerId(Long ownerId);
}

package com.victor.demo.service;

import com.victor.demo.dto.request.StoreRequest;
import com.victor.demo.dto.response.StoreResponse;
import com.victor.demo.entity.Store;
import com.victor.demo.entity.User;
import com.victor.demo.mapper.StoreMapper;
import com.victor.demo.repository.StoreRepository;
import com.victor.demo.security.CurrentUserProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StoreService {

    private final StoreRepository storeRepository;
    private final CurrentUserProvider currentUserProvider;

    public StoreService(StoreRepository storeRepository, CurrentUserProvider currentUserProvider) {
        this.storeRepository = storeRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public StoreResponse createStore(StoreRequest request) {
        User user = currentUserProvider.getCurrentUser();
        if (storeRepository.findByOwnerId(user.getId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya tienes una tienda registrada");
        }
        Store store = new Store();
        store.setName(request.name().trim());
        store.setDescription(request.description() == null ? null : request.description().trim());
        store.setOwner(user);
        storeRepository.save(store);
        return StoreMapper.toResponse(store);
    }

    public StoreResponse findById(Long id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tienda no encontrada"));
        return StoreMapper.toResponse(store);
    }

    public StoreResponse findMine() {
        User user = currentUserProvider.getCurrentUser();
        Store store = storeRepository.findByOwnerId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aún no tienes una tienda"));
        return StoreMapper.toResponse(store);
    }
}

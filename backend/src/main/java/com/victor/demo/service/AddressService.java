package com.victor.demo.service;

import com.victor.demo.dto.request.AddressRequest;
import com.victor.demo.dto.response.AddressResponse;
import com.victor.demo.entity.Address;
import com.victor.demo.entity.User;
import com.victor.demo.mapper.AddressMapper;
import com.victor.demo.repository.AddressRepository;
import com.victor.demo.security.CurrentUserProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final CurrentUserProvider currentUserProvider;

    public AddressService(AddressRepository addressRepository, CurrentUserProvider currentUserProvider) {
        this.addressRepository = addressRepository;
        this.currentUserProvider = currentUserProvider;
    }

    public List<AddressResponse> findAllForCurrentUser() {
        User user = currentUserProvider.getCurrentUser();
        return addressRepository.findByUserId(user.getId()).stream()
                .map(AddressMapper::toResponse)
                .toList();
    }

    @Transactional
    public AddressResponse create(AddressRequest request) {
        User user = currentUserProvider.getCurrentUser();
        if (request.isDefault()) {
            clearExistingDefault(user.getId());
        }
        Address address = new Address();
        address.setUser(user);
        address.setStreet(request.street().trim());
        address.setCity(request.city().trim());
        address.setState(request.state().trim());
        address.setZipCode(request.zipCode().trim());
        address.setCountry(request.country().trim());
        address.setIsDefault(request.isDefault());
        addressRepository.save(address);
        return AddressMapper.toResponse(address);
    }

    @Transactional
    public AddressResponse update(Long id, AddressRequest request) {
        User user = currentUserProvider.getCurrentUser();
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dirección no encontrada"));
        if (!address.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La dirección no pertenece al usuario actual");
        }
        if (request.isDefault()) {
            clearExistingDefault(user.getId());
        }
        address.setStreet(request.street().trim());
        address.setCity(request.city().trim());
        address.setState(request.state().trim());
        address.setZipCode(request.zipCode().trim());
        address.setCountry(request.country().trim());
        address.setIsDefault(request.isDefault());
        addressRepository.save(address);
        return AddressMapper.toResponse(address);
    }

    @Transactional
    public void delete(Long id) {
        User user = currentUserProvider.getCurrentUser();
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dirección no encontrada"));
        if (!address.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La dirección no pertenece al usuario actual");
        }
        addressRepository.delete(address);
    }

    private void clearExistingDefault(Long userId) {
        addressRepository.findByUserIdAndIsDefaultTrue(userId)
                .ifPresent(a -> {
                    a.setIsDefault(false);
                    addressRepository.save(a);
                });
    }
}

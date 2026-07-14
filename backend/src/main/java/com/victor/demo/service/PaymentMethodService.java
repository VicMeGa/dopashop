package com.victor.demo.service;

import com.victor.demo.dto.request.PaymentMethodRequest;
import com.victor.demo.dto.response.PaymentMethodResponse;
import com.victor.demo.entity.PaymentMethod;
import com.victor.demo.entity.User;
import com.victor.demo.mapper.PaymentMethodMapper;
import com.victor.demo.repository.PaymentMethodRepository;
import com.victor.demo.security.CurrentUserProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final CurrentUserProvider currentUserProvider;

    public PaymentMethodService(PaymentMethodRepository paymentMethodRepository, CurrentUserProvider currentUserProvider) {
        this.paymentMethodRepository = paymentMethodRepository;
        this.currentUserProvider = currentUserProvider;
    }

    public List<PaymentMethodResponse> findAllForCurrentUser() {
        User user = currentUserProvider.getCurrentUser();
        return paymentMethodRepository.findByUserId(user.getId()).stream()
                .map(PaymentMethodMapper::toResponse)
                .toList();
    }

    @Transactional
    public PaymentMethodResponse create(PaymentMethodRequest request) {
        User user = currentUserProvider.getCurrentUser();
        PaymentMethod pm = new PaymentMethod();
        pm.setUser(user);
        pm.setAlias(request.alias());
        pm.setCardLastFour(request.cardLastFour());
        pm.setCardBrand(request.cardBrand());
        paymentMethodRepository.save(pm);
        return PaymentMethodMapper.toResponse(pm);
    }

    @Transactional
    public void delete(Long id) {
        User user = currentUserProvider.getCurrentUser();
        PaymentMethod pm = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment method not found"));
        if (!pm.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Payment method does not belong to current user");
        }
        paymentMethodRepository.delete(pm);
    }
}

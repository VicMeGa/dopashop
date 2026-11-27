package com.victor.demo.mapper;

import com.victor.demo.dto.response.PaymentMethodResponse;
import com.victor.demo.entity.PaymentMethod;

public class PaymentMethodMapper {

    private PaymentMethodMapper() {}

    public static PaymentMethodResponse toResponse(PaymentMethod pm) {
        return new PaymentMethodResponse(
                pm.getId(),
                pm.getAlias(),
                pm.getCardLastFour(),
                pm.getCardBrand()
        );
    }
}

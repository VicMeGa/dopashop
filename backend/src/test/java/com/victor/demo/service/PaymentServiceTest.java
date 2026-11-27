package com.victor.demo.service;

import com.victor.demo.dto.response.PaymentAttemptResponse;
import com.victor.demo.entity.Address;
import com.victor.demo.entity.Order;
import com.victor.demo.entity.OrderItem;
import com.victor.demo.entity.OrderStatus;
import com.victor.demo.entity.PaymentMethod;
import com.victor.demo.entity.Product;
import com.victor.demo.entity.User;
import com.victor.demo.repository.OrderRepository;
import com.victor.demo.repository.PaymentTransactionRepository;
import com.victor.demo.repository.ProductRepository;
import com.victor.demo.repository.ShipmentRepository;
import com.victor.demo.security.CurrentUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private PaymentTransactionRepository paymentTransactionRepository;
    @Mock private ProductRepository productRepository;
    @Mock private ShipmentRepository shipmentRepository;
    @Mock private CurrentUserProvider currentUserProvider;
    @Mock private SecureRandom secureRandom;
    @InjectMocks private PaymentService paymentService;

    private User user;
    private Order order;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        Product product = new Product();
        product.setId(10L);
        product.setTitle("Mouse");
        product.setPrice(new BigDecimal("10.00"));
        product.setStock(5);

        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setProductTitle("Mouse");
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("10.00"));
        item.setSubtotal(new BigDecimal("20.00"));

        Address address = new Address();
        address.setId(1L);

        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setId(1L);

        order = new Order();
        order.setId(100L);
        order.setUser(user);
        order.setAddress(address);
        order.setPaymentMethod(paymentMethod);
        order.setStatus(OrderStatus.PENDING);
        order.setTotal(new BigDecimal("20.00"));
        order.getOrderItems().add(item);
        item.setOrder(order);

        lenient().when(currentUserProvider.getCurrentUser()).thenReturn(user);
        lenient().when(orderRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(order));
    }

    @Test
    void approvedPaymentDeductsStockAndCreatesShipment() {
        when(secureRandom.nextInt(100)).thenReturn(0);
        when(productRepository.decrementStock(10L, 2)).thenReturn(1);

        PaymentAttemptResponse response = paymentService.attemptPayment(100L);

        assertThat(response.status()).isEqualTo("APPROVED");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(order.getShipment()).isNotNull();
        verify(productRepository).decrementStock(10L, 2);
        verify(shipmentRepository).save(any());
        verify(paymentTransactionRepository).save(any());
    }

    @Test
    void rejectedPaymentDoesNotTouchStock() {
        when(secureRandom.nextInt(100)).thenReturn(99);

        PaymentAttemptResponse response = paymentService.attemptPayment(100L);

        assertThat(response.status()).isEqualTo("REJECTED");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        verifyNoInteractions(productRepository);
        verify(shipmentRepository, never()).save(any());
    }

    @Test
    void insufficientStockThrows422AndDoesNotPersistPayment() {
        when(secureRandom.nextInt(100)).thenReturn(0);
        when(productRepository.decrementStock(10L, 2)).thenReturn(0);

        assertThatThrownBy(() -> paymentService.attemptPayment(100L))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode().value()).isEqualTo(422));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        verify(shipmentRepository, never()).save(any());
        verify(paymentTransactionRepository, never()).save(any());
    }

    @Test
    void alreadyPaidOrderThrows409() {
        order.setStatus(OrderStatus.CONFIRMED);

        assertThatThrownBy(() -> paymentService.attemptPayment(100L))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode().value()).isEqualTo(409));

        verifyNoInteractions(productRepository);
    }

    @Test
    void otherUsersOrderThrows403() {
        User other = new User();
        other.setId(2L);
        when(currentUserProvider.getCurrentUser()).thenReturn(other);

        assertThatThrownBy(() -> paymentService.attemptPayment(100L))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode().value()).isEqualTo(403));
    }
}

package com.victor.demo.service;

import com.victor.demo.dto.request.CartItemRequest;
import com.victor.demo.dto.request.MergeCartRequest;
import com.victor.demo.dto.request.UpdateCartItemRequest;
import com.victor.demo.dto.response.CartResponse;
import com.victor.demo.entity.Cart;
import com.victor.demo.entity.CartItem;
import com.victor.demo.entity.Product;
import com.victor.demo.entity.User;
import com.victor.demo.repository.CartRepository;
import com.victor.demo.repository.ProductRepository;
import com.victor.demo.security.CurrentUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock private CartRepository cartRepository;
    @Mock private ProductRepository productRepository;
    @Mock private CurrentUserProvider currentUserProvider;
    @InjectMocks private CartService cartService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
    }

    @Test
    void addItemCreatesCartAndUsesServerPrice() {
        Product product = product(10L, "Mouse", "10.00", 5);
        Cart cart = cart();

        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(productRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        CartResponse response = cartService.addItem(new CartItemRequest(10L, 2));

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).quantity()).isEqualTo(2);
        assertThat(response.items().get(0).subtotal()).isEqualByComparingTo("20.00");
        assertThat(response.items().get(0).available()).isTrue();
        assertThat(response.itemCount()).isEqualTo(2);
        assertThat(response.total()).isEqualByComparingTo("20.00");
        verify(cartRepository).save(cart);
    }

    @Test
    void addItemCreatesCartWhenUserHasNone() {
        Product product = product(10L, "Mouse", "10.00", 5);

        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(productRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartResponse response = cartService.addItem(new CartItemRequest(10L, 1));

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).quantity()).isEqualTo(1);
        assertThat(response.total()).isEqualByComparingTo("10.00");
    }

    @Test
    void addItemSameProductIncrementsQuantity() {
        Product product = product(10L, "Mouse", "10.00", 5);
        Cart cart = cart(item(product, 2));

        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(productRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        CartResponse response = cartService.addItem(new CartItemRequest(10L, 1));

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).quantity()).isEqualTo(3);
        assertThat(response.itemCount()).isEqualTo(3);
    }

    @Test
    void addItemAboveStockThrows422AndDoesNotSave() {
        Product product = product(10L, "Mouse", "10.00", 2);
        Cart cart = cart(item(product, 2));

        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(productRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> cartService.addItem(new CartItemRequest(10L, 1)))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode().value()).isEqualTo(422));

        verify(cartRepository, never()).save(any());
    }

    @Test
    void addItemInactiveOrMissingThrows404() {
        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(productRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addItem(new CartItemRequest(99L, 1)))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode().value()).isEqualTo(404));

        verifyNoInteractions(cartRepository);
    }

    @Test
    void updateQuantityAboveStockThrows422() {
        Product product = product(10L, "Mouse", "10.00", 2);
        Cart cart = cart(item(product, 1));

        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> cartService.updateQuantity(10L, new UpdateCartItemRequest(3)))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode().value()).isEqualTo(422));

        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(1);
        verify(cartRepository, never()).save(any());
    }

    @Test
    void removeItemIsIdempotent() {
        Cart cart = cart();

        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        cartService.removeItem(99L);

        verify(cartRepository, never()).save(any());
    }

    @Test
    void clearRemovesAllItems() {
        Product product = product(10L, "Mouse", "10.00", 5);
        Cart cart = cart(item(product, 2));

        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        cartService.clear();

        assertThat(cart.getItems()).isEmpty();
        verify(cartRepository).save(cart);
    }

    @Test
    void mergeSumsCapsAtStockAndSkipsUnavailable() {
        Product available = product(10L, "Mouse", "10.00", 5);
        Cart cart = cart(item(available, 3));

        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(available));
        when(productRepository.findByIdAndActiveTrue(20L)).thenReturn(Optional.empty());

        MergeCartRequest request = new MergeCartRequest(List.of(
                new CartItemRequest(10L, 4),
                new CartItemRequest(20L, 1),
                new CartItemRequest(10L, 1)
        ));

        CartResponse response = cartService.merge(request);

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).quantity()).isEqualTo(5);
        assertThat(response.total()).isEqualByComparingTo("50.00");
        verify(cartRepository).save(cart);
    }

    @Test
    void getCartReturnsEmptyWhenNoCartExists() {
        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());

        CartResponse response = cartService.getCart();

        assertThat(response.id()).isNull();
        assertThat(response.items()).isEmpty();
        assertThat(response.itemCount()).isZero();
        assertThat(response.total()).isEqualByComparingTo("0.00");
        verifyNoInteractions(productRepository);
    }

    private Product product(Long id, String title, String price, int stock) {
        Product product = new Product();
        product.setId(id);
        product.setTitle(title);
        product.setPrice(new BigDecimal(price));
        product.setStock(stock);
        product.setActive(true);
        return product;
    }

    private Cart cart(CartItem... items) {
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);
        for (CartItem item : items) {
            item.setCart(cart);
            cart.getItems().add(item);
        }
        return cart;
    }

    private CartItem item(Product product, int quantity) {
        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(quantity);
        return item;
    }
}

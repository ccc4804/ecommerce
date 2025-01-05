package kr.hhplus.be.server.controller.order.application;

import kr.hhplus.be.server.domain.balance.entity.BalanceHistory;
import kr.hhplus.be.server.domain.cart.entity.CartItem;
import kr.hhplus.be.server.domain.coupon.code.CouponStatus;
import kr.hhplus.be.server.domain.coupon.entity.Coupon;
import kr.hhplus.be.server.domain.coupon.entity.UserCoupon;
import kr.hhplus.be.server.domain.order.entity.Order;
import kr.hhplus.be.server.domain.payment.entity.Payment;
import kr.hhplus.be.server.domain.product.code.ProductStatus;
import kr.hhplus.be.server.domain.product.entity.Product;
import kr.hhplus.be.server.domain.user.entity.User;
import kr.hhplus.be.server.service.balance.BalanceHistoryService;
import kr.hhplus.be.server.service.cart.CartItemService;
import kr.hhplus.be.server.service.coupon.CouponUsedHistoryService;
import kr.hhplus.be.server.service.coupon.UserCouponService;
import kr.hhplus.be.server.service.order.OrderItemService;
import kr.hhplus.be.server.service.order.OrderService;
import kr.hhplus.be.server.service.order.vo.OrderVO;
import kr.hhplus.be.server.service.payment.PaymentBalanceService;
import kr.hhplus.be.server.service.payment.PaymentCouponService;
import kr.hhplus.be.server.service.payment.PaymentService;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Transactional
@ExtendWith(MockitoExtension.class)
class OrderApplicationServiceTest {

    @Mock
    private CartItemService cartItemService;
    @Mock
    private OrderService orderService;
    @Mock
    private OrderItemService orderItemService;
    @Mock
    private UserCouponService userCouponService;
    @Mock
    private CouponUsedHistoryService couponUsedHistoryService;
    @Mock
    private BalanceHistoryService balanceHistoryService;
    @Mock
    private PaymentService paymentService;
    @Mock
    private PaymentBalanceService paymentBalanceService;
    @Mock
    private PaymentCouponService paymentCouponService;

    @InjectMocks
    private OrderApplicationServiceImpl orderApplicationServiceImpl;

    @Test
    @DisplayName("주문 결제 성공")
    void payOrder_Success() {
        // Given
        User user = User.of().build();
        Order order = Order.of().user(user).totalPrice(BigDecimal.valueOf(100)).build();
        BalanceHistory balanceHistory = BalanceHistory.of().build();
        Payment payment = Payment.of().build();
        Product product = Product.of().price(BigDecimal.valueOf(100)).stock(10).status(ProductStatus.SALE).build();
        CartItem cartItem = CartItem.of().user(user).product(product).quantity(1).build();

        List<CartItem> cartItems = List.of(cartItem);

        when(cartItemService.getCartItemsByIds(anyList())).thenReturn(cartItems);
        when(orderService.createOrder(user, BigDecimal.valueOf(100))).thenReturn(order);
        when(balanceHistoryService.calculate(user)).thenReturn(BigDecimal.valueOf(200));
        when(balanceHistoryService.use(user, BigDecimal.valueOf(100))).thenReturn(balanceHistory);
        when(paymentService.save(order, BigDecimal.valueOf(100))).thenReturn(payment);

        // When
        OrderVO result = orderApplicationServiceImpl.payOrder(List.of(1L), null);

        // Then
        assertNotNull(result);
        verify(cartItemService).deleteCartItems(cartItems);
    }

    @Test
    @DisplayName("주문 결제 실패 - 잔액 부족")
    void payOrder_InsufficientBalance() {
        // Given
        User user = User.of().balance(BigDecimal.valueOf(0)).build();
        Order order = Order.of().user(user).totalPrice(BigDecimal.valueOf(100)).build();
        BalanceHistory balanceHistory = BalanceHistory.of().build();
        Payment payment = Payment.of().build();
        Product product = Product.of().price(BigDecimal.valueOf(100)).stock(10).status(ProductStatus.SALE).build();
        CartItem cartItem = CartItem.of().user(user).product(product).quantity(1).build();

        List<CartItem> cartItems = List.of(cartItem);

        when(cartItemService.getCartItemsByIds(anyList())).thenReturn(cartItems);
        when(orderService.createOrder(any(), any())).thenReturn(order);
        when(balanceHistoryService.calculate(any())).thenReturn(BigDecimal.valueOf(0));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            orderApplicationServiceImpl.payOrder(List.of(1L), null);
        });

        assertEquals("Insufficient balance.", exception.getMessage());
    }

    @Test
    @DisplayName("주문 결제 실패 - 쿠폰 만료")
    void payOrder_CouponExpired() {
        // Given
        User user = User.of().build();
        Order order = Order.of().user(user).totalPrice(BigDecimal.valueOf(100)).build();
        BalanceHistory balanceHistory = BalanceHistory.of().build();
        Payment payment = Payment.of().build();
        Product product = Product.of().price(BigDecimal.valueOf(100)).stock(10).status(ProductStatus.SALE).build();
        CartItem cartItem = CartItem.of().user(user).product(product).quantity(1).build();
        Coupon coupon = Coupon.of().build();
        UserCoupon userCoupon = UserCoupon.of().coupon(coupon).status(CouponStatus.ACTIVE).expiredAt(LocalDateTime.now().minusDays(1)).build();

        List<CartItem> cartItems = List.of(cartItem);

        when(cartItemService.getCartItemsByIds(anyList())).thenReturn(cartItems);
        when(orderService.createOrder(user, BigDecimal.valueOf(100))).thenReturn(order);
        when(userCouponService.getUserCouponByCouponIdAndUserId(any(), any())).thenReturn(Optional.of(userCoupon));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            orderApplicationServiceImpl.payOrder(List.of(1L), 1L);
        });

        assertEquals("Invalid or expired coupon.", exception.getMessage());
    }
}
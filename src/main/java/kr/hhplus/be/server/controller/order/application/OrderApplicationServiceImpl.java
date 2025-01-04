package kr.hhplus.be.server.controller.order.application;

import kr.hhplus.be.server.domain.balance.entity.BalanceHistory;
import kr.hhplus.be.server.domain.cart.entity.CartItem;
import kr.hhplus.be.server.domain.coupon.code.CouponStatus;
import kr.hhplus.be.server.domain.coupon.entity.UserCoupon;
import kr.hhplus.be.server.domain.order.entity.Order;
import kr.hhplus.be.server.domain.order.entity.OrderItem;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderApplicationServiceImpl implements OrderApplicationService {

    private final CartItemService cartItemService;
    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final UserCouponService userCouponService;
    private final CouponUsedHistoryService couponUsedHistoryService;
    private final BalanceHistoryService balanceHistoryService;
    private final PaymentService paymentService;
    private final PaymentBalanceService paymentBalanceService;
    private final PaymentCouponService paymentCouponService;

    @Override
    @Transactional
    public OrderVO payOrder(List<Long> cartItemIds, Long userCouponId) {

        // 장바구니 가져오기
        List<CartItem> cartItems = getCartItems(cartItemIds);

        // 총 주문 금액 계산
        BigDecimal totalPrice = getTotalPrice(cartItems);

        // 주문서 생성
        User user = cartItems.get(0).getUser();
        Order order = createOrder(user, totalPrice, cartItems);

        // 쿠폰 확인
        UserCoupon userCoupon = getUserCoupon(userCouponId, user);

        // 나누기 시 1의 자리까지 버림으로 처리
        BigDecimal couponDiscountPrice = totalPrice.divide(BigDecimal.valueOf(userCoupon.getCoupon().getDiscount()), 0, RoundingMode.DOWN);
        totalPrice = totalPrice.subtract(couponDiscountPrice);

        // 잔액 확인하기
        BigDecimal availableBalance = balanceHistoryService.calculate(user);
        if (totalPrice.compareTo(availableBalance) > 0) {
            throw new IllegalArgumentException("Insufficient balance.");
        }

        // 쿠폰 사용 처리
        userCoupon = userCouponService.use(userCoupon);
        couponUsedHistoryService.save(user.getId(), userCoupon.getId());

        // 남은 금액 차감하기
        BalanceHistory useBalanceHistory = balanceHistoryService.use(user, totalPrice);

        // 결제하기
        Payment payment = paymentService.saveSuccess(order, totalPrice);
        paymentBalanceService.save(payment, useBalanceHistory, totalPrice);
        paymentCouponService.save(payment, userCoupon, couponDiscountPrice);

        // 카트 비우기
        cartItemService.deleteCartItems(cartItems);

        return OrderVO.from(order);
    }

    private UserCoupon getUserCoupon(Long userCouponId, User user) {
        UserCoupon userCoupon = userCouponService.getUserCouponByCouponIdAndUserId(userCouponId, user.getId())
                .orElseThrow(() -> new EntityNotFoundException("The coupon does not exist."));

        // 쿠폰 사용 가능 상태 확인
        if (!ObjectUtils.nullSafeEquals(userCoupon.getStatus(), CouponStatus.ACTIVE)) {
            throw new IllegalArgumentException("The coupon is not available.");
        }

        if (LocalDateTime.now().isAfter(userCoupon.getExpiredAt())) {
            throw new IllegalArgumentException("The coupon has expired.");
        }
        return userCoupon;
    }

    private List<CartItem> getCartItems(List<Long> cartItemIds) {
        List<CartItem> cartItems = cartItemService.getCartItemsByIds(cartItemIds);

        // 장바구니 검증
        validateCartItems(cartItemIds, cartItems);

        // 상품 주문 가능 상태 검증
        validateProducts(cartItems);
        return cartItems;
    }

    private Order createOrder(User user, BigDecimal totalPrice, List<CartItem> cartItems) {
        Order order = orderService.createOrder(user, totalPrice);

        // 주문서 상품 목록 생성
        List<OrderItem> orderItems
                = cartItems.stream().map(cartItem
                        -> orderItemService.createOrderItem(order, cartItem.getProduct(), cartItem.getQuantity(), cartItem.getProduct().getPrice()))
                .toList();

        order.setOrderItems(orderItems);
        return order;
    }

    private static BigDecimal getTotalPrice(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(cartItem -> cartItem.getProduct().getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validateCartItems(List<Long> cartItemIds, List<CartItem> cartItems) {
        if (ObjectUtils.nullSafeEquals(cartItemIds.size(), cartItems.size())) {
            throw new IllegalArgumentException("The number of products in the shopping cart is different.");
        }
    }

    private void validateProducts(List<CartItem> cartItems) {
        cartItems.forEach(cartItem -> {
            Product product = cartItem.getProduct();
            if (ObjectUtils.isEmpty(product)) {
                throw new IllegalArgumentException("The product does not exist.");
            }

            // 상품이 판매중인지 확인
            if (!ObjectUtils.nullSafeEquals(ProductStatus.SALE, product.getStatus())) {
                throw new IllegalArgumentException("The product is not for sale.");
            }

            // 상품 재고 확인
            if (product.getStock() < cartItem.getQuantity()) {
                throw new IllegalArgumentException("The product is out of stock.");
            }
        });
    }
}

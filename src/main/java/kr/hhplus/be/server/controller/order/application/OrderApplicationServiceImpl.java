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
        List<CartItem> cartItems = getValidatedCartItems(cartItemIds);
        BigDecimal totalPrice = calculateTotalPrice(cartItems);

        User user = cartItems.get(0).getUser();
        Order order = createOrder(user, totalPrice, cartItems);

        BigDecimal couponDiscountPrice = applyCouponIfAvailable(userCouponId, user, totalPrice);
        totalPrice = totalPrice.subtract(couponDiscountPrice);

        verifyBalanceSufficiency(user, totalPrice);
        processPaymentAndUsage(user, order, totalPrice, couponDiscountPrice, cartItems);

        return OrderVO.from(order);
    }

    private List<CartItem> getValidatedCartItems(List<Long> cartItemIds) {
        List<CartItem> cartItems = cartItemService.getCartItemsByIds(cartItemIds);
        validateCartItems(cartItemIds, cartItems);
        validateProducts(cartItems);
        return cartItems;
    }

    private BigDecimal calculateTotalPrice(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(cartItem -> cartItem.getProduct().getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal applyCouponIfAvailable(Long userCouponId, User user, BigDecimal totalPrice) {
        if (userCouponId == null) {
            return BigDecimal.ZERO;
        }

        UserCoupon userCoupon = fetchAndValidateUserCoupon(userCouponId, user);
        return totalPrice.divide(BigDecimal.valueOf(userCoupon.getCoupon().getDiscount()), 0, RoundingMode.DOWN);
    }

    private void verifyBalanceSufficiency(User user, BigDecimal totalPrice) {
        BigDecimal availableBalance = balanceHistoryService.calculate(user);
        if (totalPrice.compareTo(availableBalance) > 0) {
            throw new IllegalArgumentException("Insufficient balance.");
        }
    }

    private void processPaymentAndUsage(User user, Order order, BigDecimal totalPrice, BigDecimal couponDiscountPrice, List<CartItem> cartItems) {
        BalanceHistory usedBalanceHistory = balanceHistoryService.use(user, totalPrice);
        Payment payment = paymentService.saveSuccess(order, totalPrice);

        if (couponDiscountPrice.compareTo(BigDecimal.ZERO) > 0) {
            UserCoupon usedCoupon = userCouponService.use(fetchAndValidateUserCoupon(user.getId(), user));
            couponUsedHistoryService.save(user.getId(), usedCoupon.getId());
            paymentCouponService.save(payment, usedCoupon, couponDiscountPrice);
        }

        paymentBalanceService.save(payment, usedBalanceHistory, totalPrice);
        cartItemService.deleteCartItems(cartItems);
    }

    private UserCoupon fetchAndValidateUserCoupon(Long userCouponId, User user) {
        UserCoupon userCoupon = userCouponService.getUserCouponByCouponIdAndUserId(userCouponId, user.getId())
                .orElseThrow(() -> new EntityNotFoundException("The coupon does not exist."));

        if (!ObjectUtils.nullSafeEquals(userCoupon.getStatus(), CouponStatus.ACTIVE)) {
            throw new IllegalArgumentException("The coupon is not available.");
        }

        if (LocalDateTime.now().isAfter(userCoupon.getExpiredAt())) {
            throw new IllegalArgumentException("The coupon has expired.");
        }
        return userCoupon;
    }

    private Order createOrder(User user, BigDecimal totalPrice, List<CartItem> cartItems) {
        Order order = orderService.createOrder(user, totalPrice);
        List<OrderItem> orderItems = createOrderItems(order, cartItems);
        order.setOrderItems(orderItems);
        return order;
    }

    private List<OrderItem> createOrderItems(Order order, List<CartItem> cartItems) {
        return cartItems.stream()
                .map(cartItem -> orderItemService.createOrderItem(order, cartItem.getProduct(), cartItem.getQuantity(), cartItem.getProduct().getPrice()))
                .toList();
    }

    private void validateCartItems(List<Long> cartItemIds, List<CartItem> cartItems) {
        if (!ObjectUtils.nullSafeEquals(cartItemIds.size(), cartItems.size())) {
            throw new IllegalArgumentException("The number of products in the shopping cart is different.");
        }
    }

    private void validateProducts(List<CartItem> cartItems) {
        cartItems.forEach(cartItem -> {
            Product product = cartItem.getProduct();
            if (ObjectUtils.isEmpty(product)) {
                throw new IllegalArgumentException("The product does not exist.");
            }

            if (!ObjectUtils.nullSafeEquals(ProductStatus.SALE, product.getStatus())) {
                throw new IllegalArgumentException("The product is not for sale.");
            }

            if (product.getStock() < cartItem.getQuantity()) {
                throw new IllegalArgumentException("The product is out of stock.");
            }
        });
    }
}

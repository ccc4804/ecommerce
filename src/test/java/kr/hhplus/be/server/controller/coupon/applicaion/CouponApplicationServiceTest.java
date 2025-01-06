package kr.hhplus.be.server.controller.coupon.applicaion;

import kr.hhplus.be.server.domain.coupon.entity.Coupon;
import kr.hhplus.be.server.domain.coupon.entity.UserCoupon;
import kr.hhplus.be.server.domain.user.entity.User;
import kr.hhplus.be.server.service.coupon.CouponService;
import kr.hhplus.be.server.service.coupon.UserCouponService;
import kr.hhplus.be.server.service.coupon.vo.UserCouponVO;
import kr.hhplus.be.server.service.user.UserService;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class CouponApplicationServiceTest {

    @Mock
    private CouponService couponService;

    @Mock
    private UserCouponService userCouponService;

    @Mock
    private UserService userService;

    @InjectMocks
    private CouponApplicationServiceImpl couponApplicationService;

    @Test
    @DisplayName("쿠폰 코드로 쿠폰 발급 성공")
    void issueCouponByCode_success() {
        long userId = 1L;
        String couponCode = "COUPON123";
        User user = User.of().build();
        Coupon coupon = Coupon.of().build();

        when(userService.getUserById(userId)).thenReturn(Optional.of(user));
        when(couponService.getCouponByCode(couponCode)).thenReturn(Optional.of(coupon));

        couponApplicationService.issueCouponByCode(userId, couponCode);

        verify(couponService).issueCoupon(coupon);
        verify(userCouponService).issueCoupon(user, coupon);
    }

    @Test
    @DisplayName("사용자 조회 실패")
    void issueCouponByCode_userNotFound() {
        long userId = 1L;
        String couponCode = "COUPON123";

        when(userService.getUserById(userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> couponApplicationService.issueCouponByCode(userId, couponCode));
    }

    @Test
    @DisplayName("쿠폰 조회 실패")
    void issueCouponByCode_couponNotFound() {
        long userId = 1L;
        String couponCode = "COUPON123";
        User user = User.of().build();

        when(userService.getUserById(userId)).thenReturn(Optional.of(user));
        when(couponService.getCouponByCode(couponCode)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> couponApplicationService.issueCouponByCode(userId, couponCode));
    }

    @Test
    @DisplayName("사용자 보유 쿠폰 목록 조회 성공")
    void getUserCoupons_success() {
        Long userId = 1L;
        User user = User.of().build();
        UserCoupon userCoupon = UserCoupon.of().build();
        List<UserCoupon> userCoupons = List.of(userCoupon);

        when(userService.getUserById(any())).thenReturn(Optional.of(user));
        when(userCouponService.getUserCoupons(user)).thenReturn(userCoupons);

        List<UserCouponVO> result = couponApplicationService.getUserCoupons(userId);

        assertEquals(result.size(), 1);
    }

    @Test
    @DisplayName("사용자 보유 쿠폰 목록 조회 실패 - 사용자 없음")
    void getUserCoupons_userNotFound() {
        long userId = 1L;

        when(userService.getUserById(userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> couponApplicationService.getUserCoupons(userId));
    }
}
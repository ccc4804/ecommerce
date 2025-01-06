package kr.hhplus.be.server.service.coupon;

import kr.hhplus.be.server.domain.coupon.code.CouponStatus;
import kr.hhplus.be.server.domain.coupon.entity.Coupon;
import kr.hhplus.be.server.domain.coupon.entity.UserCoupon;
import kr.hhplus.be.server.domain.coupon.repository.UserCouponRepository;
import kr.hhplus.be.server.domain.user.entity.User;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class UserCouponServiceTest {

    @Mock
    private UserCouponRepository userCouponRepository;

    @InjectMocks
    private UserCouponServiceImpl userCouponService;

    @Test
    @DisplayName("사용자에게 쿠폰을 성공적으로 발행")
    void issueCoupon_Success() {
        User user = User.of().name("Tester").build(); // ID 설정
        Coupon coupon = Coupon.of().availableDay(30).build(); // ID 설정
        when(userCouponRepository.findByIdAndUserId(any(), any())).thenReturn(Optional.empty());

        userCouponService.issueCoupon(user, coupon);

        verify(userCouponRepository, times(1)).save(any(UserCoupon.class));
    }

    @Test
    @DisplayName("이미 발급된 쿠폰을 사용자에게 발행 시 예외 발생")
    void issueCoupon_AlreadyIssued() {
        // Given
        User user = User.of().build(); // ID 설정
        Coupon coupon = Coupon.of().build(); // ID 설정
        UserCoupon existingUserCoupon = UserCoupon.of().build();

        when(userCouponRepository.findByIdAndUserId(any(), any()))
                .thenReturn(Optional.of(existingUserCoupon));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userCouponService.issueCoupon(user, coupon);
        });

        assertEquals("User already has the coupon.", exception.getMessage());
    }


    @Test
    @DisplayName("사용자의 모든 쿠폰을 성공적으로 조회")
    void getUserCoupons_Success() {
        User user = User.of().build();
        List<UserCoupon> userCoupons = List.of(UserCoupon.of().build());
        when(userCouponRepository.findByUser(user)).thenReturn(userCoupons);

        List<UserCoupon> result = userCouponService.getUserCoupons(user);

        assertEquals(userCoupons.size(), result.size());
    }

    @Test
    @DisplayName("쿠폰 사용 성공")
    void useCoupon_Success() {
        UserCoupon userCoupon = UserCoupon.of().status(CouponStatus.ACTIVE).build();
        when(userCouponRepository.save(userCoupon)).thenReturn(userCoupon);

        UserCoupon result = userCouponService.use(userCoupon);

        assertEquals(CouponStatus.USED, result.getStatus());
        verify(userCouponRepository, times(1)).save(userCoupon);
    }
}
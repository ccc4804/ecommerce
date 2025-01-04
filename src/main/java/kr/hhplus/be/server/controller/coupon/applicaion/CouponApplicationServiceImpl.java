package kr.hhplus.be.server.controller.coupon.applicaion;

import kr.hhplus.be.server.domain.coupon.entity.Coupon;
import kr.hhplus.be.server.domain.coupon.entity.UserCoupon;
import kr.hhplus.be.server.domain.user.entity.User;
import kr.hhplus.be.server.service.coupon.CouponService;
import kr.hhplus.be.server.service.coupon.UserCouponService;
import kr.hhplus.be.server.service.coupon.vo.UserCouponVO;
import kr.hhplus.be.server.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponApplicationServiceImpl implements CouponApplicationService {

    private final CouponService couponService;
    private final UserCouponService userCouponService;
    private final UserService userService;

    // 쿠폰 코드로 쿠폰 발급
    @Override
    @Transactional
    public void issueCouponByCode(long userId, String couponCode) {

        // 사용자 조회
        User user = userService.getUserById(userId).orElseThrow(
                () -> new EntityNotFoundException("User not found. userId: " + userId));

        // 쿠폰 조회
        Coupon coupon = couponService.getCouponByCode(couponCode)
                .orElseThrow(() -> new EntityNotFoundException("Coupon not found. couponCode: " + couponCode));

        // 쿠폰 검증
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(coupon.getRegisterStartDate()) && now.isBefore(coupon.getRegisterEndDate())) {
            throw new IllegalArgumentException("Coupon registration period is not valid.");
        }

        // 쿠폰 재고 확인
        if (coupon.getStock() <= coupon.getCurrentStock()) {
            throw new IllegalArgumentException("Coupon stock is insufficient.");
        }

        // 쿠폰 발급
        userCouponService.issueCoupon(user, coupon);
        couponService.issueCoupon(coupon);

        log.info("issueCouponByCode userId: {}, couponCode: {}", userId, couponCode);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserCouponVO> getUserCoupons(Long userId) {

        // 사용자 조회
        User user = userService.getUserById(userId).orElseThrow(
                () -> new EntityNotFoundException("User not found. userId: " + userId));

        // 사용자 보유 쿠폰 목록 조회
        List<UserCoupon> userCoupons = userCouponService.getUserCoupons(user);

        return userCoupons.stream().map(UserCouponVO::from).toList();
    }
}

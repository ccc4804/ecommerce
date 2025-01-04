package kr.hhplus.be.server.service.coupon;

import kr.hhplus.be.server.domain.coupon.entity.Coupon;
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;

    @Override
    public Optional<Coupon> getCouponByCode(String code) {
        return couponRepository.findByCode(code);
    }

    @Override
    public void issueCoupon(Coupon coupon) {
        coupon.issueCoupon();
        couponRepository.save(coupon);
    }
}

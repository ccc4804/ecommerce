package kr.hhplus.be.server.service.payment.vo;

import kr.hhplus.be.server.domain.payment.entity.PaymentCoupon;
import kr.hhplus.be.server.service.coupon.vo.UserCouponVO;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class PaymentCouponVO {

    private Long id;
    private UserCouponVO userCoupon;
    private BigDecimal amount;

    @Builder
    public PaymentCouponVO(Long id, UserCouponVO userCoupon, BigDecimal amount) {
        this.id = id;
        this.userCoupon = userCoupon;
        this.amount = amount;
    }

    public static PaymentCouponVO from(PaymentCoupon paymentCoupon) {
        return PaymentCouponVO.builder()
                .id(paymentCoupon.getId())
                .userCoupon(UserCouponVO.from(paymentCoupon.getUserCoupon()))
                .amount(paymentCoupon.getAmount())
                .build();
    }
}

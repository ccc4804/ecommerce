package kr.hhplus.be.server.service.payment.vo;

import kr.hhplus.be.server.domain.payment.code.PaymentStatus;
import kr.hhplus.be.server.domain.payment.entity.Payment;
import kr.hhplus.be.server.service.order.vo.OrderVO;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class PaymentVO {

    private Long id;
    private OrderVO order;
    private BigDecimal amount;
    private PaymentStatus status;
    private PaymentCouponVO paymentCoupon;
    private PaymentBalanceVO paymentBalance;

    @Builder
    public PaymentVO(Long id, OrderVO order, BigDecimal amount, PaymentStatus status, PaymentCouponVO paymentCoupon, PaymentBalanceVO paymentBalance) {
        this.id = id;
        this.order = order;
        this.amount = amount;
        this.status = status;
        this.paymentCoupon = paymentCoupon;
        this.paymentBalance = paymentBalance;
    }

    public static PaymentVO from(Payment payment) {

        if (ObjectUtils.isEmpty(payment)) {
            return null;
        }

        return PaymentVO.builder()
                .id(payment.getId())
                .order(OrderVO.from(payment.getOrder()))
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .paymentCoupon(PaymentCouponVO.from(payment.getPaymentCoupon()))
                .paymentBalance(PaymentBalanceVO.from(payment.getPaymentBalance()))
                .build();
    }
}

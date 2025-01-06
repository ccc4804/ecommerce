package kr.hhplus.be.server.service.payment;

import kr.hhplus.be.server.domain.coupon.entity.UserCoupon;
import kr.hhplus.be.server.domain.payment.entity.Payment;
import kr.hhplus.be.server.domain.payment.entity.PaymentCoupon;
import kr.hhplus.be.server.domain.payment.repository.PaymentCouponRepository;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

@ExtendWith(MockitoExtension.class)
class PaymentCouponServiceTest {

    @Mock
    private PaymentCouponRepository paymentCouponRepository;

    @InjectMocks
    private PaymentCouponServiceImpl paymentCouponService;

    @Test
    @DisplayName("결제 쿠폰 저장 성공")
    void savePaymentCouponSuccess() {
        Payment payment = Payment.of().amount(new BigDecimal("100")).build();
        UserCoupon userCoupon = UserCoupon.of().build();
        BigDecimal amount = new BigDecimal("50");
        PaymentCoupon paymentCoupon = PaymentCoupon.of().payment(payment).userCoupon(userCoupon).amount(amount).build();

        when(paymentCouponRepository.save(any(PaymentCoupon.class))).thenReturn(paymentCoupon);

        PaymentCoupon savedPaymentCoupon = paymentCouponService.save(payment, userCoupon, amount);

        assertThat(savedPaymentCoupon).isNotNull();
        assertThat(savedPaymentCoupon.getPayment()).isEqualTo(payment);
        assertThat(savedPaymentCoupon.getUserCoupon()).isEqualTo(userCoupon);
        assertThat(savedPaymentCoupon.getAmount()).isEqualTo(amount);
    }
}
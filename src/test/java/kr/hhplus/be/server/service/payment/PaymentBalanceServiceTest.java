package kr.hhplus.be.server.service.payment;

import kr.hhplus.be.server.domain.balance.entity.BalanceHistory;
import kr.hhplus.be.server.domain.payment.entity.Payment;
import kr.hhplus.be.server.domain.payment.entity.PaymentBalance;
import kr.hhplus.be.server.domain.payment.repository.PaymentBalanceRepository;
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
class PaymentBalanceServiceTest {

    @Mock
    private PaymentBalanceRepository paymentBalanceRepository;

    @InjectMocks
    private PaymentBalanceServiceImpl paymentBalanceService;

    @Test
    @DisplayName("결제 잔액 저장 성공")
    void savePaymentBalanceSuccess() {
        Payment payment = Payment.of().amount(new BigDecimal("100")).build();
        BalanceHistory balanceHistory = BalanceHistory.of().build();
        BigDecimal amount = new BigDecimal("50");
        PaymentBalance paymentBalance = PaymentBalance.of().payment(payment).balanceHistory(balanceHistory).amount(amount).build();

        when(paymentBalanceRepository.save(any(PaymentBalance.class))).thenReturn(paymentBalance);

        PaymentBalance savedPaymentBalance = paymentBalanceService.save(payment, balanceHistory, amount);

        assertThat(savedPaymentBalance).isNotNull();
        assertThat(savedPaymentBalance.getPayment()).isEqualTo(payment);
        assertThat(savedPaymentBalance.getBalanceHistory()).isEqualTo(balanceHistory);
        assertThat(savedPaymentBalance.getAmount()).isEqualTo(amount);
    }
}
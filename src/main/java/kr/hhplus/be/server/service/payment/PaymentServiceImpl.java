package kr.hhplus.be.server.service.payment;

import kr.hhplus.be.server.domain.order.entity.Order;
import kr.hhplus.be.server.domain.payment.code.PaymentStatus;
import kr.hhplus.be.server.domain.payment.entity.Payment;
import kr.hhplus.be.server.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    // 결제 내역 저장
    @Override
    public Payment saveSuccess(Order order, BigDecimal amount) {
        Payment payment = Payment.of()
                .order(order)
                .amount(amount)
                .status(PaymentStatus.SUCCESS)
                .build();

        return paymentRepository.save(payment);
    }
}

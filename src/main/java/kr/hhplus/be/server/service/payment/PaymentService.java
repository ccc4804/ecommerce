package kr.hhplus.be.server.service.payment;

import kr.hhplus.be.server.domain.order.entity.Order;
import kr.hhplus.be.server.domain.payment.entity.Payment;

import java.math.BigDecimal;

public interface PaymentService {

    Payment saveSuccess(Order order, BigDecimal amount);
}

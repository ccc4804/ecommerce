package kr.hhplus.be.server.service.balance;

import kr.hhplus.be.server.domain.balance.entity.BalanceHistory;
import kr.hhplus.be.server.domain.user.entity.User;

import java.math.BigDecimal;

public interface BalanceHistoryService {

    BigDecimal calculate(User user);

    BalanceHistory use(User user, BigDecimal amount);
}

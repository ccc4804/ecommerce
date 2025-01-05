package kr.hhplus.be.server.service.balance;

import kr.hhplus.be.server.domain.balance.code.BalanceType;
import kr.hhplus.be.server.domain.balance.entity.BalanceHistory;
import kr.hhplus.be.server.domain.balance.repository.BalanceHistoryRepository;
import kr.hhplus.be.server.domain.user.entity.User;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class BalanceHistoryServiceTest {

    @Mock
    private BalanceHistoryRepository balanceHistoryRepository;

    @InjectMocks
    private BalanceHistoryServiceImpl balanceHistoryService;

    @Test
    @DisplayName("사용자 잔액을 계산합니다")
    void calculate_balance() {
        User user = User.of().name("Test User").balance(BigDecimal.ZERO).build();
        BalanceHistory history1 = BalanceHistory.of().user(user).type(BalanceType.CHARGE).amount(new BigDecimal("100")).build();
        BalanceHistory history2 = BalanceHistory.of().user(user).type(BalanceType.USE).amount(new BigDecimal("50")).build();
        when(balanceHistoryRepository.findByUser(user)).thenReturn(List.of(history1, history2));

        BigDecimal result = balanceHistoryService.calculate(user);

        assertEquals(new BigDecimal("50"), result);
        assertEquals(new BigDecimal("50"), user.getBalance());
    }

    @Test
    @DisplayName("사용자 잔액이 없는 경우 0을 반환합니다")
    void calculate_balance_no_history() {
        User user = User.of().name("Test User").balance(BigDecimal.ZERO).build();
        when(balanceHistoryRepository.findByUser(user)).thenReturn(Collections.emptyList());

        BigDecimal result = balanceHistoryService.calculate(user);

        assertEquals(BigDecimal.ZERO, result);
        assertEquals(BigDecimal.ZERO, user.getBalance());
    }

    @Test
    @DisplayName("잔액을 사용한 내역을 기록하고 사용자 잔액을 업데이트합니다")
    void use_balance() {
        User user = User.of().name("Test User").balance(new BigDecimal("100")).build();
        BigDecimal amount = new BigDecimal("50");
        BalanceHistory history = BalanceHistory.of().user(user).type(BalanceType.USE).amount(amount).build();
        when(balanceHistoryRepository.save(any(BalanceHistory.class))).thenReturn(history);

        BalanceHistory result = balanceHistoryService.use(user, amount);

        assertEquals(BalanceType.USE, result.getType());
        assertEquals(amount, result.getAmount());
        assertEquals(new BigDecimal("50"), user.getBalance());
    }
}
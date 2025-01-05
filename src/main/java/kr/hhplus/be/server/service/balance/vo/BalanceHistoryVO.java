package kr.hhplus.be.server.service.balance.vo;

import kr.hhplus.be.server.domain.balance.code.BalanceType;
import kr.hhplus.be.server.domain.balance.entity.BalanceHistory;
import kr.hhplus.be.server.service.user.vo.UserVO;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class BalanceHistoryVO {

    private Long id;
    private UserVO user;
    private BigDecimal amount;
    private BalanceType type;

    @Builder
    public BalanceHistoryVO(Long id, UserVO user, BigDecimal amount, BalanceType type) {
        this.id = id;
        this.user = user;
        this.amount = amount;
        this.type = type;
    }

    public static BalanceHistoryVO from(BalanceHistory balanceHistory) {

        if (ObjectUtils.isEmpty(balanceHistory)) {
            return null;
        }

        return BalanceHistoryVO.builder()
                .id(balanceHistory.getId())
                .user(UserVO.from(balanceHistory.getUser()))
                .amount(balanceHistory.getAmount())
                .type(balanceHistory.getType())
                .build();
    }
}

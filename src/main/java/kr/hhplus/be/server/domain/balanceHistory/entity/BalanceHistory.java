package kr.hhplus.be.server.domain.balanceHistory.entity;

import kr.hhplus.be.server.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "balance_history")
@Getter
@ToString
@NoArgsConstructor
public class BalanceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", insertable = false, nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, name = "change_amount")
    private BigDecimal changeAmount;

    @Column(nullable = false, name = "type")
    private String type;

    @Column(name = "current_balance")
    private Long referenceId;

    @Column(nullable = false, name = "current_balance")
    private BigDecimal currentBalance;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public BalanceHistory(User user, BigDecimal changeAmount, String type, Long referenceId, BigDecimal currentBalance) {
        this.user = user;
        this.changeAmount = changeAmount;
        this.type = type;
        this.referenceId = referenceId;
        this.currentBalance = currentBalance;
    }
}

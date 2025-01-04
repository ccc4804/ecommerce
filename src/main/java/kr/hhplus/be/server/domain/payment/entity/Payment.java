package kr.hhplus.be.server.domain.payment.entity;

import kr.hhplus.be.server.domain.order.entity.Order;
import kr.hhplus.be.server.domain.payment.code.PaymentStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", insertable = false, nullable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false, name = "amount")
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "status")
    private PaymentStatus status;

    @OneToOne(mappedBy = "payment")
    private PaymentCoupon paymentCoupon;

    @OneToOne(mappedBy = "payment")
    private PaymentBalance paymentBalance;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder(builderMethodName = "of")
    public Payment(Order order, BigDecimal amount, PaymentStatus status) {
        this.order = order;
        this.amount = amount;
        this.status = status;
    }
}
package kr.hhplus.be.server.domain.payment.entity;

import kr.hhplus.be.server.domain.coupon.entity.UserCoupon;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "payment_coupon")
public class PaymentCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", insertable = false, nullable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_coupon_id", nullable = false)
    private UserCoupon userCoupon;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Builder(builderMethodName = "of")
    public PaymentCoupon(Payment payment, UserCoupon userCoupon, BigDecimal amount) {
        this.payment = payment;
        this.userCoupon = userCoupon;
        this.amount = amount;
    }
}

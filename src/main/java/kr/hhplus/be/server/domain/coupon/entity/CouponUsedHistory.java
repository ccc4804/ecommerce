package kr.hhplus.be.server.domain.coupon.entity;

import kr.hhplus.be.server.domain.coupon.code.CouponUsedType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "coupon_used_history")
public class CouponUsedHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", insertable = false, nullable = false)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_coupon_id", nullable = false)
    private Long userCouponId;

    @Enumerated(EnumType.STRING)
    @Column(name = "used_type", nullable = false)
    private CouponUsedType usedType;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public CouponUsedHistory(Long id, Long userId, Long userCouponId, CouponUsedType usedType) {
        this.id = id;
        this.userId = userId;
        this.userCouponId = userCouponId;
        this.usedType = usedType;
    }

    @Builder(builderMethodName = "of")
    public CouponUsedHistory(Long userId, Long userCouponId, CouponUsedType usedType) {
        this.userId = userId;
        this.userCouponId = userCouponId;
        this.usedType = usedType;
    }
}
package kr.hhplus.be.server.domain.coupon.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "coupon")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", insertable = false, nullable = false)
    private Long id;

    // 쿠폰 이름
    @Column(name = "name", nullable = false)
    private String name;

    // 쿠폰 코드
    @Column(name = "code", nullable = false, unique = true)
    private String code;

    // 할인율
    @Column(name = "discount", nullable = false)
    private double discount;

    // 발급 수량
    @Column(name = "stock", nullable = false)
    private int stock;

    // 발급 수량
    @Column(name = "current_stock", nullable = false)
    private int currentStock;

    // 쿠폰 등록 시작일
    @Column(name = "register_start_date", nullable = false)
    private LocalDateTime registerStartDate;

    // 쿠폰 등록 종료일
    @Column(name = "register_end_date", nullable = false)
    private LocalDateTime registerEndDate;

    // 쿠폰 사용 기간 (null이면 무제한)
    @Column(name = "available_day")
    private Integer availableDay;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder(builderMethodName = "of")
    public Coupon(String name, String code, double discount, int currentStock, int stock,
                  LocalDateTime registerStartDate, LocalDateTime registerEndDate, Integer availableDay) {
        this.name = name;
        this.code = code;
        this.discount = discount;
        this.stock = stock;
        this.currentStock = currentStock;
        this.registerStartDate = registerStartDate;
        this.registerEndDate = registerEndDate;
        this.availableDay = availableDay;
    }

    public void issueCoupon() {
        this.currentStock++;
    }
}

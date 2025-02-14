package kr.hhplus.be.server.domain.coupon.repository;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.domain.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Coupon> findByCode(String code);
}

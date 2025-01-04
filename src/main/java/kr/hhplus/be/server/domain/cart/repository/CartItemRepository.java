package kr.hhplus.be.server.domain.cart.repository;

import kr.hhplus.be.server.domain.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByIdIn(List<Long> cartItemIds);
}

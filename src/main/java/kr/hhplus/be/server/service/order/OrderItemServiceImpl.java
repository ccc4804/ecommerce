package kr.hhplus.be.server.service.order;

import kr.hhplus.be.server.domain.order.entity.Order;
import kr.hhplus.be.server.domain.order.entity.OrderItem;
import kr.hhplus.be.server.domain.order.repository.OrderItemRepository;
import kr.hhplus.be.server.domain.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl implements OrderItemService {

    private final OrderItemRepository orderItemRepository;

    @Override
    public OrderItem createOrderItem(Order order, Product product, int quantity, BigDecimal price) {
        OrderItem orderItem = OrderItem.of()
                .order(order)
                .product(product)
                .quantity(quantity)
                .price(price)
                .build();

        return orderItemRepository.save(orderItem);
    }
}

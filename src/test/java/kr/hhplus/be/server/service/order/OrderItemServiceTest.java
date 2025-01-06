package kr.hhplus.be.server.service.order;

import kr.hhplus.be.server.domain.order.entity.Order;
import kr.hhplus.be.server.domain.order.entity.OrderItem;
import kr.hhplus.be.server.domain.order.repository.OrderItemRepository;
import kr.hhplus.be.server.domain.product.entity.Product;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

class OrderItemServiceTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private OrderItemServiceImpl orderItemService;

    public OrderItemServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("정상적인 주문 항목 생성")
    void createOrderItem_Success() {
        Order order = Order.of().build();
        Product product = Product.of().build();
        int quantity = 1;
        BigDecimal price = BigDecimal.valueOf(1000);

        OrderItem orderItem = OrderItem.of()
                .order(order)
                .product(product)
                .quantity(quantity)
                .price(price)
                .build();

        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(orderItem);

        OrderItem result = orderItemService.createOrderItem(order, product, quantity, price);

        assertNotNull(result);
    }
}
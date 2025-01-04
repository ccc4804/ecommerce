package kr.hhplus.be.server.service.order;

import kr.hhplus.be.server.domain.order.entity.Order;
import kr.hhplus.be.server.domain.order.entity.OrderItem;
import kr.hhplus.be.server.domain.product.entity.Product;

import java.math.BigDecimal;

public interface OrderItemService {

    OrderItem createOrderItem(Order order, Product product, int quantity, BigDecimal price);
}

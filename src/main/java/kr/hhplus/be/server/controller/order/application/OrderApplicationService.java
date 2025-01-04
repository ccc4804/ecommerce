package kr.hhplus.be.server.controller.order.application;

import kr.hhplus.be.server.service.order.vo.OrderVO;

import java.util.List;

public interface OrderApplicationService {

    OrderVO payOrder(List<Long> cartItemIds, Long userCouponId);
}

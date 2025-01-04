package kr.hhplus.be.server.controller.order;

import kr.hhplus.be.server.config.dto.ResponseDTO;
import kr.hhplus.be.server.controller.order.application.OrderApplicationService;
import kr.hhplus.be.server.controller.order.dto.OrderRequestDTO;
import kr.hhplus.be.server.controller.order.dto.OrderResponseDTO;
import kr.hhplus.be.server.service.order.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderApplicationService orderApplicationService;

    // 주문서 생성
    @PostMapping("/api/v1/orders")
    public ResponseDTO<OrderResponseDTO> createOrder(@RequestBody OrderRequestDTO requestDTO) {

        OrderVO orderVO = orderApplicationService.payOrder(requestDTO.getCartItemIds(), requestDTO.getUserCouponId());
        OrderResponseDTO responseDTO = OrderResponseDTO.from(orderVO);
        return ResponseDTO.success(responseDTO);
    }
}

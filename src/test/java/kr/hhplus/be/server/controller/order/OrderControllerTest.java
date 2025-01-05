package kr.hhplus.be.server.controller.order;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.config.dto.ResponseDTO;
import kr.hhplus.be.server.controller.order.dto.OrderRequestDTO;
import kr.hhplus.be.server.controller.order.dto.OrderResponseDTO;
import kr.hhplus.be.server.domain.balance.code.BalanceType;
import kr.hhplus.be.server.domain.balance.entity.BalanceHistory;
import kr.hhplus.be.server.domain.balance.repository.BalanceHistoryRepository;
import kr.hhplus.be.server.domain.cart.entity.CartItem;
import kr.hhplus.be.server.domain.cart.repository.CartItemRepository;
import kr.hhplus.be.server.domain.coupon.code.CouponStatus;
import kr.hhplus.be.server.domain.coupon.entity.Coupon;
import kr.hhplus.be.server.domain.coupon.entity.UserCoupon;
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository;
import kr.hhplus.be.server.domain.coupon.repository.UserCouponRepository;
import kr.hhplus.be.server.domain.order.code.OrderStatus;
import kr.hhplus.be.server.domain.product.code.ProductStatus;
import kr.hhplus.be.server.domain.product.entity.Product;
import kr.hhplus.be.server.domain.product.repository.ProductRepository;
import kr.hhplus.be.server.domain.user.entity.User;
import kr.hhplus.be.server.domain.user.repository.UserRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@AutoConfigureMockMvc
@SpringBootTest
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UserCouponRepository userCouponRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private BalanceHistoryRepository balanceHistoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("주문 성공 - 쿠폰 사용 O")
    void payOrderSuccess_useCoupon() throws Exception {
        User user = userRepository.save(User.of().name("TestUser").email("test@example.com").balance(BigDecimal.valueOf(500)).build());
        balanceHistoryRepository.save(BalanceHistory.of().user(user).amount(BigDecimal.valueOf(500)).type(BalanceType.CHARGE).build());

        Coupon coupon = couponRepository.save(Coupon.of().code("TEST_CODE").name("TestCoupon")
                .currentStock(0)
                .stock(100)
                .discount(50)
                .registerStartDate(LocalDateTime.now().minusDays(1))
                .registerEndDate(LocalDateTime.now().plusDays(1)).build());

        UserCoupon userCoupon = userCouponRepository.save(UserCoupon.of().user(user).coupon(coupon).status(CouponStatus.ACTIVE).expiredAt(LocalDateTime.now().plusDays(1)).build());

        Product product1 = productRepository.save(Product.of().name("TestProduct1").price(BigDecimal.valueOf(100)).stock(100).status(ProductStatus.SALE).build());
        Product product2 = productRepository.save(Product.of().name("TestProduct2").price(BigDecimal.valueOf(100)).stock(100).status(ProductStatus.SALE).build());

        CartItem cartItem1 = cartItemRepository.save(CartItem.of().user(user).product(product1).quantity(1).build());
        CartItem cartItem2 = cartItemRepository.save(CartItem.of().user(user).product(product2).quantity(1).build());

        List<Long> cartItemIds = List.of(cartItem1.getId(), cartItem2.getId());
        Long userCouponId = userCoupon.getId();

        OrderRequestDTO requestDTO = new OrderRequestDTO(cartItemIds, userCouponId);
        MvcResult result = mockMvc.perform(post("/api/v1/orders")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andReturn();

        ResponseDTO<OrderResponseDTO> responseDTO = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<ResponseDTO<OrderResponseDTO>>() {
        });
        OrderResponseDTO response = responseDTO.getData();

        assertEquals(OrderStatus.COMPLETED, response.getStatus());
        assertEquals(0, BigDecimal.valueOf(200).compareTo(response.getTotalPrice()));
        assertEquals(0, BigDecimal.valueOf(100).compareTo(response.getPayment().getTotalAmount()));
        assertEquals(0, BigDecimal.valueOf(100).compareTo(response.getPayment().getCouponDiscountAmount()));
        assertEquals(0, BigDecimal.valueOf(100).compareTo(response.getPayment().getBalanceAmount()));
    }

    @Test
    @DisplayName("주문 성공 - 쿠폰 사용 X")
    void payOrderSuccess_notUseCoupon() throws Exception {
        User user = userRepository.save(User.of().name("TestUser").email("test@example.com").balance(BigDecimal.valueOf(500)).build());
        balanceHistoryRepository.save(BalanceHistory.of().user(user).amount(BigDecimal.valueOf(500)).type(BalanceType.CHARGE).build());

        Product product1 = productRepository.save(Product.of().name("TestProduct1").price(BigDecimal.valueOf(100)).stock(100).status(ProductStatus.SALE).build());
        Product product2 = productRepository.save(Product.of().name("TestProduct2").price(BigDecimal.valueOf(100)).stock(100).status(ProductStatus.SALE).build());

        CartItem cartItem1 = cartItemRepository.save(CartItem.of().user(user).product(product1).quantity(1).build());
        CartItem cartItem2 = cartItemRepository.save(CartItem.of().user(user).product(product2).quantity(1).build());

        List<Long> cartItemIds = List.of(cartItem1.getId(), cartItem2.getId());

        OrderRequestDTO requestDTO = new OrderRequestDTO(cartItemIds, null);
        MvcResult result = mockMvc.perform(post("/api/v1/orders")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andReturn();

        ResponseDTO<OrderResponseDTO> responseDTO = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<ResponseDTO<OrderResponseDTO>>() {
        });
        OrderResponseDTO response = responseDTO.getData();

        assertEquals(OrderStatus.COMPLETED, response.getStatus());
        assertEquals(0, BigDecimal.valueOf(200).compareTo(response.getTotalPrice()));
        assertEquals(0, BigDecimal.valueOf(200).compareTo(response.getPayment().getTotalAmount()));
        assertEquals(0, BigDecimal.valueOf(200).compareTo(response.getPayment().getBalanceAmount()));
    }
}
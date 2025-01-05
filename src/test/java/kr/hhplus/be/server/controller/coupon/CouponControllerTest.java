package kr.hhplus.be.server.controller.coupon;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.config.dto.ResponseDTO;
import kr.hhplus.be.server.controller.coupon.dto.CouponIssueRequestDTO;
import kr.hhplus.be.server.controller.coupon.dto.CouponResponseDTO;
import kr.hhplus.be.server.domain.coupon.code.CouponStatus;
import kr.hhplus.be.server.domain.coupon.entity.Coupon;
import kr.hhplus.be.server.domain.coupon.entity.UserCoupon;
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository;
import kr.hhplus.be.server.domain.coupon.repository.UserCouponRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AutoConfigureMockMvc
@SpringBootTest
class CouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UserCouponRepository userCouponRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("쿠폰 발급 성공")
    void issueCouponSuccess() throws Exception {
        User user = userRepository.save(User.of().name("TestUser").email("test@example.com").balance(BigDecimal.ZERO).build());
        Coupon coupon = couponRepository.save(Coupon.of().code("TEST_CODE").name("TestCoupon")
                .stock(100)
                .registerStartDate(LocalDateTime.now().minusDays(1))
                .registerEndDate(LocalDateTime.now().plusDays(1)).build());

        CouponIssueRequestDTO requestDTO = new CouponIssueRequestDTO(user.getId(), coupon.getCode());
        mockMvc.perform(post("/api/v1/coupons/issue")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("보유 쿠폰 목록 조회 테스트")
    void getUserCoupons() throws Exception {

        User user = userRepository.save(User.of().name("TestUser").email("test@example.com").balance(BigDecimal.ZERO).build());
        Coupon coupon = couponRepository.save(Coupon.of().code("TEST_CODE").name("TestCoupon")
                .stock(100)
                .registerStartDate(LocalDateTime.now().minusDays(1))
                .registerEndDate(LocalDateTime.now().plusDays(1)).build());
        UserCoupon userCoupon = userCouponRepository.save(UserCoupon.of().user(user).coupon(coupon).status(CouponStatus.ACTIVE).expiredAt(LocalDateTime.now().plusDays(1)).build());

        MvcResult result = mockMvc.perform(get("/api/v1/coupons/" + user.getId()))
                .andExpect(status().isOk())
                .andReturn();

        ResponseDTO<CouponResponseDTO> responseDTO = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<ResponseDTO<CouponResponseDTO>>(){});
        CouponResponseDTO response = responseDTO.getData();

        assertEquals(1, response.getUserCoupons().size());
        assertEquals("TestCoupon", response.getUserCoupons().get(0).getCouponName());
    }
}
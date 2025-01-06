package kr.hhplus.be.server.controller.coupon;

import kr.hhplus.be.server.config.dto.ResponseDTO;
import kr.hhplus.be.server.controller.coupon.applicaion.CouponApplicationService;
import kr.hhplus.be.server.controller.coupon.dto.CouponIssueRequestDTO;
import kr.hhplus.be.server.controller.coupon.dto.CouponResponseDTO;
import kr.hhplus.be.server.service.coupon.vo.UserCouponVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CouponController {

    private final CouponApplicationService couponApplicationService;

    // 쿠폰 발급 기능 API
    @PostMapping("/api/v1/coupons/issue")
    public ResponseDTO<String> issueCouponSuccess(@RequestBody CouponIssueRequestDTO requestDTO) {

        couponApplicationService.issueCouponByCode(requestDTO.getUserId(), requestDTO.getCouponCode());

        return ResponseDTO.success("SUCCESS");
    }

    // 보유 쿠폰 목록 조회
    @GetMapping("/api/v1/coupons/{userId}")
    public ResponseDTO<CouponResponseDTO> getUserCoupons(@PathVariable Long userId) {
        List<UserCouponVO> userCouponVOS = couponApplicationService.getUserCoupons(userId);

        CouponResponseDTO responseDTO
                = new CouponResponseDTO(
                    userCouponVOS.stream().map(CouponResponseDTO.UserCouponResponseData::from).toList()
                );

        return ResponseDTO.success(responseDTO);
    }
}
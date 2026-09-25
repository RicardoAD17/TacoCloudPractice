package tacos.web.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tacos.pricing.CouponService;
import tacos.web.DTO.QuoteRequest;
import tacos.web.DTO.QuoteResponse;

@RestController
@RequestMapping(path = "/api/coupons", produces = "application/json")
@CrossOrigin(origins = "*")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping("/validate")
    public ResponseEntity<QuoteResponse> validateCoupon(@RequestBody QuoteRequest request) {
        QuoteResponse response = couponService.validateAndQuote(request.getCode(), request.getSubtotal());
        if (!response.isValid()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }
}
package tacos.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.stereotype.Service;
import tacos.web.DTO.QuoteResponse;

@Service
public class CouponService {

    private final DiscountProperties props;
    private final Clock clock;

    public CouponService(DiscountProperties props, Clock clock) {
        this.props = props;
        this.clock = clock;
    }

    public QuoteResponse validateAndQuote(String code, BigDecimal subtotal) {
        if (code == null || code.trim().isEmpty()) {
            return new QuoteResponse(false, BigDecimal.ZERO, subtotal, "Sin cupón");
        }

        String normalizedCode = code.trim().toUpperCase();

        DiscountProperties.Coupon coupon = props.getCodes().entrySet().stream()
                .filter(entry -> entry.getKey().toUpperCase().equals(normalizedCode))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
        if (coupon == null) {
            return new QuoteResponse(false, BigDecimal.ZERO, subtotal, "Código inválido o expirado");
        }

       LocalDateTime now = LocalDateTime.now(clock);

        if (coupon.getValidFrom() != null) {
            LocalDateTime from = LocalDateTime.parse(coupon.getValidFrom());
            if (now.isBefore(from)) {
                return new QuoteResponse(false, BigDecimal.ZERO, subtotal, "Código inválido o expirado");
            }
        }

        if (coupon.getValidUntil() != null) {
            LocalDateTime until = LocalDateTime.parse(coupon.getValidUntil());
            if (now.isAfter(until)) {
                return new QuoteResponse(false, BigDecimal.ZERO, subtotal, "Código inválido o expirado");
            }
        }

        if (subtotal.compareTo(coupon.getMinPurchase()) < 0) {
            return new QuoteResponse(false, BigDecimal.ZERO, subtotal, "No se alcanzó el mínimo de compra de $" + coupon.getMinPurchase());
        }

        BigDecimal discount = BigDecimal.ZERO;
        if (coupon.getType() == DiscountProperties.Type.FIXED) {
            discount = coupon.getValue();
        } else if (coupon.getType() == DiscountProperties.Type.PERCENTAGE) {
            discount = subtotal.multiply(coupon.getValue().divide(BigDecimal.valueOf(100)));
        }

        if (coupon.getMaxDiscount() != null && discount.compareTo(coupon.getMaxDiscount()) > 0) {
            discount = coupon.getMaxDiscount();
        }
        if (discount.compareTo(subtotal) > 0) {
            discount = subtotal; 
        }

        discount = discount.setScale(2, RoundingMode.HALF_UP);
        BigDecimal newTotal = subtotal.subtract(discount).setScale(2, RoundingMode.HALF_UP);

        return new QuoteResponse(true, discount, newTotal, "Cupón aplicado correctamente");
    }
}
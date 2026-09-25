package tacos.pricing;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "tacocloud.promotions")
public class DiscountProperties {
    
    private Map<String, Coupon> codes = new HashMap<>();

    @Data
    public static class Coupon {
        private Type type; 
        private BigDecimal value;
        private BigDecimal minPurchase = BigDecimal.ZERO;
        private BigDecimal maxDiscount;
        
        private String validFrom;
        private String validUntil;
    }

    public enum Type {
        PERCENTAGE, FIXED
    }
}
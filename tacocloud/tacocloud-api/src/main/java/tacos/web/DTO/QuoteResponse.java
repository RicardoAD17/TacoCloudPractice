package tacos.web.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QuoteResponse {
    private boolean valid;
    private java.math.BigDecimal discountAmount;
    private java.math.BigDecimal newTotal;
    private String message;
}
package tacos.web.DTO;

import lombok.Data;

@Data 
public class QuoteRequest {
    private String code;
    private java.math.BigDecimal subtotal;
}

package tacos.web.DTO;

import java.math.BigDecimal;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class OrderResponse {
    private String id;
    private String deliveryName;
    private String username;
    private BigDecimal subtotal;
    private BigDecimal total;
    private List<TacoResponse> tacos;
    private String discountCode;
    private java.math.BigDecimal discountAmount = java.math.BigDecimal.ZERO;
}

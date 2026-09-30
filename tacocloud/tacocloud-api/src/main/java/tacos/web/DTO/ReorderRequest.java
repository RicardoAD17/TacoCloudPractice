package tacos.web.DTO;

import lombok.Data;

@Data
public class ReorderRequest {
    private String paymentMethodId;
    private boolean confirmPriceChange;
    private String idempotencyKey; 
}
package tacos.web.DTO;

import lombok.Data;
import java.util.Date;

@Data
public class OrderSummaryDTO {
    private String id;
    private Date placedAt;
    private String deliveryName;
    private int tacoCount;
}
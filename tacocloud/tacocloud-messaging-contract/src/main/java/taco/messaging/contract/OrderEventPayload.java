package tacos.messaging.contract; 

import lombok.Data;
import java.util.Date;
import java.util.List; 

@Data
public class OrderEventPayload {
    private String orderId;
    private Date placedAt;
    private String deliveryName;
    private String status;
    private List<String> tacoNames;
}
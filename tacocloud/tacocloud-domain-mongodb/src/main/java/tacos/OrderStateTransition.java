package tacos;

import lombok.Data;
import tacos.model.OrderStatus;

import java.util.Date;

@Data
public class OrderStateTransition {
    private OrderStatus fromStatus;
    private OrderStatus toStatus;
    private Date timestamp;
    private String actor;
    private String reason;
}
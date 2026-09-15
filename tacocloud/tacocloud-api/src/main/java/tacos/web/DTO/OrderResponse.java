package tacos.web.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class OrderResponse {
    private String id;
    private String deliveryName;
    private String username;
}

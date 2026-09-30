package tacos.web.DTO;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TacoResponse {
    private String name;
    private int quantity;
    private BigDecimal price; 
}
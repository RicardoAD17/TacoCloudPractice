package tacos.web.DTO;

import lombok.Data;
import java.util.Date;
import java.util.List;
import tacos.Taco;

@Data
public class KitchenOrderDTO {
    private String id;
    private Date placedAt;
    private List<Taco> tacos;
    private int estimatedPrepMinutes; 
}
package tacos.web.DTO;

import lombok.Data;
import java.util.List;

@Data
public class ReorderResponse {
    private String status; 
    private String newOrderId;
    private double oldTotal;
    private double newTotal;
    private List<String> warnings;
}
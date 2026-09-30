package tacos.web.DTO;

import lombok.Data;
import java.util.Date;
import java.util.List;
import tacos.Taco;

@Data
public class OrderDetailDTO {
    private String id;
    private Date placedAt;
    private String deliveryName;
    private String street;
    private String city;
    private String state;
    private String zip;
    private List<Taco> tacos;
}
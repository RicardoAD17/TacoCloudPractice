package tacos.web.DTO;

import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter 
public class ModificacionOrderDTO {
    //opcional marcado con Optional/empty
    //private Optional<String> deliveryName=Optional.empty();
    //la declaracion sencilla ya pretende un valor opcional 
    private String deliveryName;
    private  String deliveryStreet;
    private String deliveryCity;
    private String deliveryState;
    private  String deliveryZip;
}

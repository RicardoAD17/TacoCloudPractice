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
    @Size(min=3,max=30,message="Si env[ias un nombre, debe ser valido")
    private String deliveryName;
    private  String deliveryStreet;
    private String deliveryCity;
    private String deliveryState;
    @Size(min=5,max=5,message="El código postal debe ser valor numerico y de logitud de 5 digitos")
    private  String deliveryZip;
}

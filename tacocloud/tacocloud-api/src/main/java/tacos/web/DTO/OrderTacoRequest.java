package tacos.web.DTO;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter 
public class OrderTacoRequest {
    @NotBlank
    @Size(min=3,max=30,message = "Ingresa un nombre valido mayor a 3 caracteres")
    private String deliveryName;
    @NotBlank(message="la calle es obligatoria")
    private String deliveryStreet;
    @NotBlank(message="la ciudad de entrega es obligatoria")
    private String deliveryCity;
    @NotBlank(message="el estado es obligatorio")
    private String deliveryState;
    @NotBlank(message="el codigo postal es obligatorio")
    private String deliveryZip;
    @NotBlank(message="Un token de pago es requerido")
    private String paymentToken;

    private List<TacoRequest> tacos;
    private String discountCode;
}

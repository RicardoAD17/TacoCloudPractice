package tacos.web.DTO;

import javax.validation.constraints.NotBlank;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter 
@Setter 
public class TokenRequest {
    private String ccNumber;
    
    private String ccExpiration;
    @NotBlank(message = "El cvv es obligatorio")
    private String ccCVV; 
}

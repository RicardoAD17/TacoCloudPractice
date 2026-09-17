package tacos.web.DTO;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter 
@Setter 
public class TokenRequest {
    private String ccNumber;
    private String ccExpiration;
    private String ccCVV; 
}

package tacos.web.DTO;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter 
@Setter 
public class TokenResponse {
    private String paymentToken;
    private String brand;
    private String last4;
    private String expiration;
}
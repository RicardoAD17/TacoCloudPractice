package tacos.web.DTO;
import reactor.core.publisher.Mono;
public interface PaymentGateway {
    Mono<TokenResponse> tokenize(TokenRequest request);
     
}

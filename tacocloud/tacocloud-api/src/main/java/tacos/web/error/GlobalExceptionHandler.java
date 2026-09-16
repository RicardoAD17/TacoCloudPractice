package tacos.web.error;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.web.server.ServerHttpSecurity.HttpsRedirectSpec;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;

@RestControllerAdvice 
public class GlobalExceptionHandler {
    @ExceptionHandler (WebExchangeBindException.class)
    public ResponseEntity<ApiProblem> handleValidationException(WebExchangeBindException ex, ServerWebExchange exchange) {
        List<ApiProblem.Violation> violations= ex.getBindingResult().getFieldErrors().stream()
        .map(error-> new ApiProblem.Violation(error.getField(),error.getDefaultMessage()))
        .collect(Collectors.toList());
        ApiProblem problem= ApiProblem.builder()
        .type("https://taco-cloud.com/probs/validation-error")
        .title("Error de Validación")
        .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
        .detail("La solicitud contiene datos invalidos o reglas de negocio incumplidas")
        .instance(exchange.getRequest().getPath().value())
        .code("ERR_VALIDATION_FAILED")
        .violations(violations)
        .build();
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
        .header("Content-Type", "application/problem+json")
        .body(problem);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiProblem> handleAllUncaughtExceptions(Exception ex,ServerWebExchange exchange){
        ApiProblem problem = ApiProblem.builder()
        .type("https://taco-cloud.com/probs/internal-server-error")
        .title("Error interno del Servidor")
        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
        .detail("Ha ocurrido un error inesperado. Por favor contacte a soporte.")
        .instance(exchange.getRequest().getPath().value())
        .code("ERR_INTERNAL_SERVER")
        .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .header("Content-Type","application/problem+json")
        .body(problem);

    }
}

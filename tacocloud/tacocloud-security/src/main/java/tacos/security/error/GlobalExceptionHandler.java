package tacos.security.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ApiProblem> handleValidationException(WebExchangeBindException ex, ServerWebExchange exchange) {
        
        List<ApiProblem.Violation> violations = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ApiProblem.Violation(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());

        String path = (exchange != null && exchange.getRequest() != null) ? exchange.getRequest().getPath().value() : "/unknown";

        ApiProblem problem = ApiProblem.builder()
                .type("https://taco-cloud.com/probs/validation-error")
                .title("Error de Validación")
                .status(HttpStatus.UNPROCESSABLE_ENTITY.value()) 
                .detail("La solicitud contiene datos inválidos o reglas de negocio incumplidas.")
                .instance(path)
                .code("ERR_VALIDATION_FAILED")
                .violations(violations)
                .build();

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                             .header("Content-Type", "application/problem+json")
                             .body(problem);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiProblem> handleNotFoundException(NotFoundException ex, ServerWebExchange exchange) {
        String path = (exchange != null && exchange.getRequest() != null) ? exchange.getRequest().getPath().value() : "/unknown";
        
        ApiProblem problem = ApiProblem.builder()
                .type("https://taco-cloud.com/probs/not-found")
                .title("Recurso No Encontrado")
                .status(HttpStatus.NOT_FOUND.value())
                .detail(ex.getMessage())
                .instance(path)
                .code("ERR_NOT_FOUND")
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .header("Content-Type", "application/problem+json")
                             .body(problem);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiProblem> handleConflictException(ConflictException ex, ServerWebExchange exchange) {
        String path = (exchange != null && exchange.getRequest() != null) ? exchange.getRequest().getPath().value() : "/unknown";
        
        ApiProblem problem = ApiProblem.builder()
                .type("https://taco-cloud.com/probs/conflict")
                .title("Conflicto de Estado")
                .status(HttpStatus.CONFLICT.value())
                .detail(ex.getMessage())
                .instance(path)
                .code("ERR_CONFLICT")
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT)
                             .header("Content-Type", "application/problem+json")
                             .body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiProblem> handleAllUncaughtExceptions(Exception ex, ServerWebExchange exchange) {
        String path = (exchange != null && exchange.getRequest() != null) ? exchange.getRequest().getPath().value() : "/unknown";
        
        ApiProblem problem = ApiProblem.builder()
                .type("https://taco-cloud.com/probs/internal-server-error")
                .title("Error Interno del Servidor")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .detail("Ha ocurrido un error inesperado. " + ex.getMessage())
                .instance(path)
                .code("ERR_INTERNAL_SERVER")
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .header("Content-Type", "application/problem+json")
                             .body(problem);
    }
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiProblem> handleForbiddenException(ForbiddenException ex, ServerWebExchange exchange) {
        String path = (exchange != null && exchange.getRequest() != null) ? exchange.getRequest().getPath().value() : "/unknown";
        
        ApiProblem problem = ApiProblem.builder()
                .type("https://taco-cloud.com/probs/forbidden")
                .title("Acceso Denegado")
                .status(HttpStatus.FORBIDDEN.value())
                .detail(ex.getMessage())
                .instance(path)
                .code("ERR_FORBIDDEN")
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                             .header("Content-Type", "application/problem+json")
                             .body(problem);
    }
}
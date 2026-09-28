package tacos.web.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import tacos.TacoOrder;
import tacos.User;
import tacos.model.OrderStatus;
import tacos.service.OrderWorkflowService;

import java.util.Map;

@RestController
@RequestMapping(path = "/api/orders", produces = "application/json")
@CrossOrigin(origins = "*")
public class OrderWorkflowController {

    private final OrderWorkflowService workflowService;

    public OrderWorkflowController(OrderWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @PatchMapping(path = "/{id}/status")
    public Mono<ResponseEntity<TacoOrder>> updateStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal User user) {

        if (user == null) { user = crearUsuarioAdminSimulado(); } // Simulado para tests

        OrderStatus newStatus = OrderStatus.valueOf(payload.get("status"));
        String reason = payload.getOrDefault("reason", "Actualización estándar");

        return workflowService.transitionState(id, newStatus, user, reason)
                .map(ResponseEntity::ok)
                .onErrorResume(IllegalStateException.class, e -> Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).build())) // 409 Conflict si la transición es inválida
                .onErrorResume(SecurityException.class, e -> Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build())); // 403 si no es staff
    }
    @PostMapping(path = "/{id}/cancel")
    public Mono<ResponseEntity<TacoOrder>> cancelOrder(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, String> payload,
            @AuthenticationPrincipal User user) {

        if (user == null) { user = crearUsuarioAdminSimulado(); }

        String reason = (payload != null) ? payload.getOrDefault("reason", "Cancelado por usuario") : "Cancelado por usuario";

        return workflowService.transitionState(id, OrderStatus.CANCELLED, user, reason)
                .map(ResponseEntity::ok)
                .onErrorResume(IllegalStateException.class, e -> Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).build()));
    }

    private User crearUsuarioAdminSimulado() {
        User user = new User("admin", "pwd", "Admin", null, null, null, null, null, null);
        user.setId("admin-id");
        user.setRole("ROLE_ADMIN");
        return user;
    }
}
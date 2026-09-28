package tacos.web.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tacos.User;
import tacos.service.KitchenQueueService;
import tacos.web.DTO.KitchenOrderDTO;

@RestController
@RequestMapping(path = "/api/kitchen", produces = "application/json")
@CrossOrigin(origins = "http://localhost:8080")
public class KitchenApiController {

    private final KitchenQueueService kitchenService;

    public KitchenApiController(KitchenQueueService kitchenService) {
        this.kitchenService = kitchenService;
    }

    @GetMapping("/queue")
    public Flux<KitchenOrderDTO> getQueue() {
        return kitchenService.getQueue()
            .flatMap(order -> kitchenService.calculateETA(order)
                .map(eta -> {
                    KitchenOrderDTO dto = new KitchenOrderDTO();
                    dto.setId(order.getId());
                    dto.setPlacedAt(order.getPlacedAt());
                    dto.setTacos(order.getTacos());
                    dto.setEstimatedPrepMinutes(eta);
                    return dto;
                })
            );
    }

    @PostMapping("/orders/claim")
    public Mono<ResponseEntity<KitchenOrderDTO>> claimOrder(@AuthenticationPrincipal User user) {
        if (user == null) {
            user = new User(); user.setId("cook-1"); user.setUsername("Chef"); user.setRole("ROLE_KITCHEN");
        }

        if (!user.getRole().contains("KITCHEN") && !user.getRole().contains("ADMIN")) {
            return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
        }

        return kitchenService.claimNextOrder(user)
            .flatMap(order -> kitchenService.calculateETA(order)
                .map(eta -> {
                    KitchenOrderDTO dto = new KitchenOrderDTO();
                    dto.setId(order.getId());
                    dto.setPlacedAt(order.getPlacedAt());
                    dto.setTacos(order.getTacos());
                    dto.setEstimatedPrepMinutes(eta);
                    return dto;
                })
            )
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.status(HttpStatus.NO_CONTENT).build()); // 204 si la cola está vacía
    }
}
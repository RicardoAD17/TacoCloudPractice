package tacos.web.api;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tacos.TacoOrder;
import tacos.User;
import tacos.data.OrderRepository;
import tacos.web.DTO.OrderDetailDTO;
import tacos.web.DTO.OrderSummaryDTO;

@RestController
@RequestMapping(path = "/api/users/me/orders", produces = "application/json")
@CrossOrigin(origins = "http://localhost:8080")
public class UserOrderController {

    private final OrderRepository orderRepo;

    public UserOrderController(OrderRepository orderRepo) {
        this.orderRepo = orderRepo;
    }

    @GetMapping
    public Flux<OrderSummaryDTO> myOrders(
            @AuthenticationPrincipal User user, 
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        
        PageRequest pageRequest = PageRequest.of(page, Math.min(size, 50), 
            Sort.by(Sort.Direction.DESC, "placedAt", "id"));
        
       
        return orderRepo.findByUser_id(user.getId(), pageRequest)
                .map(this::toSummaryDTO);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<OrderDetailDTO>> myOrderDetails(
            @PathVariable String id,
            @AuthenticationPrincipal User user) { 
        
       
        return orderRepo.findByIdAndUser_Id(id, user.getId())
                .map(order -> ResponseEntity.ok(toDetailDTO(order)))
                .defaultIfEmpty(ResponseEntity.notFound().build()); // 404 seguro
    }
    private OrderSummaryDTO toSummaryDTO(TacoOrder order) {
        OrderSummaryDTO dto = new OrderSummaryDTO();
        dto.setId(order.getId());
        dto.setPlacedAt(order.getPlacedAt());
        dto.setDeliveryName(order.getDeliveryName());
        dto.setTacoCount(order.getTacos() != null ? order.getTacos().size() : 0);
        return dto;
    }

    private OrderDetailDTO toDetailDTO(TacoOrder order) {
        OrderDetailDTO dto = new OrderDetailDTO();
        dto.setId(order.getId());
        dto.setPlacedAt(order.getPlacedAt());
        dto.setDeliveryName(order.getDeliveryName());
        dto.setStreet(order.getDeliveryStreet());
        dto.setCity(order.getDeliveryCity());
        dto.setState(order.getDeliveryState());
        dto.setZip(order.getDeliveryZip());
        dto.setTacos(order.getTacos());
        return dto;
    }
}
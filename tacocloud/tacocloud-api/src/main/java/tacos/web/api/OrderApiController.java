package tacos.web.api;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tacos.TacoOrder;
import tacos.User;
import tacos.data.OrderRepository;
import tacos.messaging.OrderMessagingService;
import tacos.web.DTO.ModificacionOrderDTO;
import tacos.web.DTO.OrderMapper;
import tacos.web.DTO.OrderResponse;
import tacos.web.DTO.OrderTacoRequest;
import tacos.web.error.NotFoundException;

@RestController
@RequestMapping(path="/api/orders", produces="application/json")
@CrossOrigin(origins="http://localhost:8080")
public class OrderApiController {

  private final OrderRepository repo;
  private final OrderMessagingService orderMessages;
  private final OrderMapper orderMapper;
  private final EmailOrderService emailOrderService;

  public OrderApiController(OrderRepository repo,
                            OrderMessagingService orderMessages,
                            EmailOrderService emailOrderService,
                            OrderMapper orderMapper) {
    this.repo = repo;
    this.orderMessages = orderMessages;
    this.emailOrderService = emailOrderService;
    this.orderMapper = orderMapper;
  }

  @GetMapping(produces="application/json")
  public Flux<OrderResponse> allOrders() {
    return repo.findAll().map(orderMapper::toResponse);
  }

  @PostMapping(consumes="application/json")
  public Mono<ResponseEntity<OrderResponse>> postOrder(@Valid @RequestBody OrderTacoRequest order) {
      TacoOrder orderToSave = orderMapper.toDomain(order);
      return repo.save(orderToSave)
                 .map(savedOrder -> ResponseEntity.status(HttpStatus.CREATED).body(orderMapper.toResponse(savedOrder)));
  }

  @PostMapping(path="fromEmail", consumes="application/json")
  public Mono<ResponseEntity<OrderResponse>> postOrderFromEmail(@RequestBody Mono<EmailOrder> emailOrder) {
      return emailOrderService.convertEmailOrderToDomainOrder(emailOrder)
      .flatMap(ordenConvertida -> {
        return repo.save(ordenConvertida).flatMap(ordenGuardada -> {
          return Mono.fromRunnable(() -> {
            orderMessages.sendOrder(ordenGuardada);
          }).thenReturn(ordenGuardada);
        });
      })
      .map(ordenMapeada -> ResponseEntity.status(HttpStatus.CREATED).body(orderMapper.toResponse(ordenMapeada)));
  }

  @PutMapping(path="/{orderId}", consumes="application/json")
  public Mono<ResponseEntity<OrderResponse>> putOrder(@Valid @RequestBody ModificacionOrderDTO order, 
                                                      @PathVariable("orderId") String orderId,
                                                      @AuthenticationPrincipal User user) {
    return repo.findById(orderId).flatMap(existingOrder -> {
      boolean creador = user != null && user.getId() != null && existingOrder.getUser() != null && existingOrder.getUser().getId().equals(user.getId());
      if (!creador) {
        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).<OrderResponse>build());
      }
      existingOrder.setDeliveryCity(order.getDeliveryCity());
      existingOrder.setDeliveryName(order.getDeliveryName());
      existingOrder.setDeliveryState(order.getDeliveryState());
      existingOrder.setDeliveryStreet(order.getDeliveryStreet());
      existingOrder.setDeliveryZip(order.getDeliveryZip());
      
      return repo.save(existingOrder)
                 .map(ordenGuardada -> ResponseEntity.ok(orderMapper.toResponse(ordenGuardada)));
                 
    })
    .switchIfEmpty(Mono.error(new NotFoundException("No se encontró la orden con ID: " + orderId)));
  }

  @PatchMapping(path="/{orderId}", consumes="application/json")
  public Mono<ResponseEntity<OrderResponse>> patchOrder(@PathVariable("orderId") String orderId,
                                                        @Valid @RequestBody ModificacionOrderDTO orderPatch) {
    return repo.findById(orderId)
          .map(order -> {
          if (orderPatch.getDeliveryName() != null) {
            order.setDeliveryName(orderPatch.getDeliveryName());
          }
          if (orderPatch.getDeliveryStreet() != null) {
            order.setDeliveryStreet(orderPatch.getDeliveryStreet());
          }
          if (orderPatch.getDeliveryCity() != null) {
            order.setDeliveryCity(orderPatch.getDeliveryCity());
          }
          if (orderPatch.getDeliveryState() != null) {
            order.setDeliveryState(orderPatch.getDeliveryState());
          }
          if (orderPatch.getDeliveryZip() != null) {
            order.setDeliveryZip(orderPatch.getDeliveryZip());
          }
          return order;
        })
        .flatMap(repo::save)
        .map(ordenGuardada -> ResponseEntity.ok(orderMapper.toResponse(ordenGuardada)))
        .switchIfEmpty(Mono.error(new NotFoundException("No se pudo actualizar. No se encontró la orden con ID: " + orderId)));
  }

  @DeleteMapping("/{orderId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteOrder(@PathVariable("orderId") String orderId) {
    return repo.findById(orderId)
        .switchIfEmpty(Mono.error(new NotFoundException("No se puede eliminar. No se encontró la orden con ID: " + orderId)))
        .flatMap(existingOrder -> repo.deleteById(orderId));
  }
}
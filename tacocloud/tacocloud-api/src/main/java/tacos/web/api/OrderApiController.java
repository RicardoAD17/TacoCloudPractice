package tacos.web.api;

import javax.validation.Valid;

import org.springframework.data.domain.PageRequest;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tacos.messaging.contract.OrderMessagingService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tacos.TacoOrder;
import tacos.User;
import tacos.data.OrderRepository;
import tacos.security.error.ForbiddenException;
import tacos.security.error.NotFoundException;
import tacos.web.DTO.ModificacionOrderDTO;
import tacos.web.DTO.OrderMapper;
import tacos.web.DTO.OrderResponse;
import tacos.web.DTO.OrderTacoRequest;
import org.springframework.data.domain.PageRequest;
import tacos.messaging.contract.OrderEvent;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import org.springframework.data.domain.Pageable;
import io.micrometer.core.instrument.Timer;
import tacos.data.IdempotencyRecordRepository;
import org.springframework.util.DigestUtils;
import org.springframework.dao.DuplicateKeyException;
import tacos.data.IdempotencyRecord;
@RestController
@RequestMapping(path={"/api/v1/orders", "/api/orders"}, produces="application/json")
@CrossOrigin(origins="http://localhost:8080")
public class OrderApiController {

  private final OrderRepository repo;
  private final OrderMessagingService orderMessages;
  private final OrderMapper orderMapper;
  private final EmailOrderService emailOrderService;
  private final OrderPricingService pricingService;
  private final InventoryService inventoryService;
  private final MeterRegistry meterRegistry;
  private final IdempotencyRecordRepository idempotencyRepo;
  public OrderApiController(OrderRepository repo,
                            OrderMessagingService orderMessages,
                            EmailOrderService emailOrderService,
                            OrderMapper orderMapper,OrderPricingService pricingService,
                            InventoryService inventoryService, MeterRegistry meterRegistry,
                            IdempotencyRecordRepository idempotencyRepo) {
    this.repo = repo;
    this.orderMessages = orderMessages;
    this.emailOrderService = emailOrderService;
    this.orderMapper = orderMapper;
    this.pricingService = pricingService;
    this.inventoryService = inventoryService;
    this.meterRegistry= meterRegistry;
    this.idempotencyRepo = idempotencyRepo;
  }

  @GetMapping(produces="application/json")
  public Flux<OrderResponse> allOrders(
          @AuthenticationPrincipal User user) {

      Pageable pageable = PageRequest.of(0, 20);

      return repo.findByUserOrderByPlacedAtDesc(user, pageable)
              .map(orderMapper::toResponse);
  }

@PostMapping(consumes="application/json")
public Mono<ResponseEntity<OrderResponse>> postOrder(
    @Valid @RequestBody OrderTacoRequest orderRequest, 
    @AuthenticationPrincipal User user,
    @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
final String finalKey = (idempotencyKey == null || idempotencyKey.isEmpty()) 
                      ? java.util.UUID.randomUUID().toString() 
                      : idempotencyKey;
String canonicalString = orderRequest.getDeliveryName() + "|" + 
                      orderRequest.getDeliveryZip() + "|" + 
                      (orderRequest.getTacos() != null ? orderRequest.getTacos().size() : 0);
String requestHash = DigestUtils.md5DigestAsHex(canonicalString.getBytes());

String userId = user != null ? user.getUsername() : "anonymous";
IdempotencyRecord newRecord = new IdempotencyRecord();
newRecord.setUserId(userId);
newRecord.setIdempotencyKey(finalKey);
newRecord.setRequestHash(requestHash);
newRecord.setStatus("IN_PROGRESS");

Timer.Sample sample = Timer.start(meterRegistry);
return idempotencyRepo.save(newRecord)
      .flatMap(insertedRecord -> 
          pricingService.calculateOrderTotals(orderMapper.toDomain(orderRequest))
              .flatMap(calculatedOrder -> {
                  calculatedOrder.setUser(user);
                  calculatedOrder.setPaymentToken(orderRequest.getPaymentToken());
                  return inventoryService.reserveStock(calculatedOrder, finalKey)
                          .thenReturn(calculatedOrder);
              })
              .flatMap(repo::save)
              .flatMap(savedOrder -> {

                  insertedRecord.setStatus("COMPLETED");
                  insertedRecord.setOrderId(savedOrder.getId());
                 return idempotencyRepo.save(insertedRecord).thenReturn((TacoOrder) savedOrder);
              })
              .map(savedOrder -> {
                  meterRegistry.counter("tacocloud.orders.processed", "status", "success").increment();
                  sample.stop(meterRegistry.timer("tacocloud.order.placement.time", "status", "success"));
                  return ResponseEntity.status(HttpStatus.CREATED).body(orderMapper.toResponse((TacoOrder) savedOrder));
              })
      )
      .onErrorResume(DuplicateKeyException.class, ex -> 
          idempotencyRepo.findByUserIdAndIdempotencyKey(userId, finalKey)
              .flatMap(existingRecord -> {
                  if (!existingRecord.getRequestHash().equals(requestHash)) {
                      return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).build()); 
                  }
                  if ("IN_PROGRESS".equals(existingRecord.getStatus())) {
                      return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).build()); 
                  }

                  return repo.findById(existingRecord.getOrderId())
                      .map(existingOrder -> {
                          
                          return ResponseEntity.ok(orderMapper.toResponse(existingOrder));
                      });
              })
      )
      .onErrorResume(ex -> {
          if (!(ex instanceof DuplicateKeyException)) {
              meterRegistry.counter("tacocloud.orders.processed", "status", "inventory_failed").increment();
              sample.stop(meterRegistry.timer("tacocloud.order.placement.time", "status", "error"));
              
              if (ex instanceof org.springframework.web.server.ResponseStatusException) {
                  return Mono.error(ex);
              }
              return Mono.error(new org.springframework.web.server.ResponseStatusException(
                      HttpStatus.CONFLICT, "Error al procesar: " + ex.getMessage()));
          }
          return Mono.error(ex);
      });
  }
@PostMapping(path="fromEmail", consumes="application/json")
  public Mono<ResponseEntity<OrderResponse>> postOrderFromEmail(@RequestBody Mono<EmailOrder> emailOrder) {
      return emailOrderService.convertEmailOrderToDomainOrder(emailOrder)
      .flatMap(ordenConvertida -> {
        return repo.save(ordenConvertida).flatMap(ordenGuardada -> {
          return Mono.fromRunnable(() -> {
              OrderEvent event = new OrderEvent();
              orderMessages.sendOrderEvent(event);
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
      boolean isAdmin= user!=null && user.getAuthorities()!=null &&  user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
      boolean creador = user != null && user.getId() != null && existingOrder.getUser() != null && existingOrder.getUser().getId().equals(user.getId());
      if (!creador && !isAdmin) {
        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).<OrderResponse>build());
      }
      existingOrder.setDeliveryCity(order.getDeliveryCity());
      existingOrder.
      
      
      setDeliveryName(order.getDeliveryName());
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
                                                        @Valid @RequestBody ModificacionOrderDTO orderPatch,
                                                        @AuthenticationPrincipal User user) {
    return repo.findById(orderId)
        .flatMap(existingOrder -> {
          boolean isAdmin= user!=null && user.getAuthorities()!=null &&  user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
          boolean creador = user != null && user.getId() != null && existingOrder.getUser() != null && existingOrder.getUser().getId().equals(user.getId());
          
          if (!creador && !isAdmin) {
            return Mono.<TacoOrder>error(new ForbiddenException("No tienes permisos para actualizar esta orden."));
          }

          if (orderPatch.getDeliveryName() != null) {
            existingOrder.setDeliveryName(orderPatch.getDeliveryName());
          }
          if (orderPatch.getDeliveryStreet() != null) {
            existingOrder.setDeliveryStreet(orderPatch.getDeliveryStreet());
          }
          if (orderPatch.getDeliveryCity() != null) {
            existingOrder.setDeliveryCity(orderPatch.getDeliveryCity());
          }
          if (orderPatch.getDeliveryState() != null) {
            existingOrder.setDeliveryState(orderPatch.getDeliveryState());
          }
          if (orderPatch.getDeliveryZip() != null) {
            existingOrder.setDeliveryZip(orderPatch.getDeliveryZip());
          }
          return repo.save(existingOrder);
        })
        .map(ordenGuardada -> ResponseEntity.ok(orderMapper.toResponse(ordenGuardada)))
        .switchIfEmpty(Mono.error(new NotFoundException("No se pudo actualizar. No se encontró la orden con ID: " + orderId)));
  }
  @DeleteMapping("/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteOrder(@PathVariable("orderId") String orderId,
                                  @AuthenticationPrincipal User user) {
      return repo.findById(orderId)
          .switchIfEmpty(Mono.error(new NotFoundException("No se puede eliminar. No se encontró la orden con ID: " + orderId)))
          .flatMap(existingOrder -> {
            boolean isAdmin= user!=null && user.getAuthorities()!=null &&  user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean creador = user != null && user.getId() != null && existingOrder.getUser() != null && existingOrder.getUser().getId().equals(user.getId());
            
            if (!creador && !isAdmin) {
              return Mono.error(new ForbiddenException("No tienes permisos para eliminar esta orden."));
            }
            return repo.deleteById(orderId);
          });
    }
    @PostMapping(path="/{orderId}/reorder", consumes="application/json")
      public Mono<ResponseEntity<OrderResponse>> reorder(
              @PathVariable("orderId") String orderId,
              @RequestBody java.util.Map<String, Object> requestBody,
              @AuthenticationPrincipal User user) {

          return repo.findById(orderId)
              .flatMap(existingOrder -> {
                  OrderEvent event = new OrderEvent();
                  orderMessages.sendOrderEvent(event);
                  return Mono.just(ResponseEntity.ok(orderMapper.toResponse(existingOrder)));
              })
              .switchIfEmpty(Mono.error(new NotFoundException("No se encontró la orden histórica con ID: " + orderId)));
      }
}
package tacos.web.api;


import static org.mockito.Mockito.when;

import javax.sound.midi.Patch;

import org.hibernate.validator.internal.constraintvalidators.bv.EmailValidator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;
import tacos.TacoOrder;
import tacos.User;
import tacos.data.OrderRepository;
import tacos.messaging.OrderMessagingService;
import tacos.web.DTO.ModificacionOrderDTO;
import tacos.web.DTO.OrderMapper;

public class OrderTacoApiTest {
    
    @Test 
    public void zipTestValuePatch(){
        TacoOrder ordenOriginal= new TacoOrder();
        ordenOriginal.setId("123");
        ordenOriginal.setDeliveryState("TX");
        ordenOriginal.setDeliveryZip("00000");

        ModificacionOrderDTO requestPrueba=new ModificacionOrderDTO();
        requestPrueba.setDeliveryZip("12345");
        
        OrderRepository repo= new Mockito().mock(OrderRepository.class);
        OrderMessagingService orderMessaging= new Mockito().mock(OrderMessagingService.class);
        EmailOrderService emailService= new Mockito().mock(EmailOrderService.class);
        OrderMapper orderMapper= new Mockito().mock(OrderMapper.class);
        Mockito.when(repo.findById("123")).thenReturn(Mono.just((ordenOriginal)));
        Mockito.when(repo.save(Mockito.any())).thenAnswer(i-> Mono.just(i.getArgument(0)));

        WebTestClient testClient = WebTestClient.bindToController(
        new OrderApiController(repo, orderMessaging, emailService,orderMapper))
        .build();
        testClient.patch().uri("/api/orders/123").bodyValue(requestPrueba)
        .accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk().expectBody().jsonPath("$.deliveryZip").isEqualTo("12345") 
        .jsonPath("$.deliveryState").isEqualTo("TX");
    }
    @Test void putCorrecto(){
        User mockUser = new User("testuser", "password", "Test Name", "Street", "City", "State", "12345", "555", "test@test.com");
        mockUser.setId("user123");
        TacoOrder ordenOriginal = new TacoOrder();
        ordenOriginal.setId("123");
        ordenOriginal.setUser(mockUser);
        ordenOriginal.setDeliveryCity("Prueba1");
        ordenOriginal.setDeliveryName("Prueba1");
        ordenOriginal.setDeliveryState("Prueba1");
        ordenOriginal.setDeliveryStreet("Prueba1");
        ordenOriginal.setDeliveryZip("12345");
        ModificacionOrderDTO requestPrueba=new ModificacionOrderDTO();
        requestPrueba.setDeliveryCity("PUTPrueba1");
        requestPrueba.setDeliveryName("PUTPrueba1");
        requestPrueba.setDeliveryState("PUTPrueba1");
        requestPrueba.setDeliveryStreet("PUTPrueba1");
        requestPrueba.setDeliveryZip("67890");
        OrderRepository repo= new Mockito().mock(OrderRepository.class);
        OrderMessagingService orderMessaging= new Mockito().mock(OrderMessagingService.class);
        EmailOrderService emailService= new Mockito().mock(EmailOrderService.class);
        OrderMapper orderMapper= new Mockito().mock(OrderMapper.class);
        Mockito.when(repo.findById("123")).thenReturn(Mono.just(ordenOriginal));
        Mockito.when(repo.save(Mockito.any(TacoOrder.class))).thenReturn(Mono.just(ordenOriginal));
       WebTestClient testClient = WebTestClient.bindToController(new OrderApiController(repo, orderMessaging, emailService,orderMapper))
        .argumentResolvers(configurer -> configurer.addCustomResolver(new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.getParameterType().equals(User.class);
            }
            @Override
            public Mono<Object> resolveArgument(MethodParameter parameter, BindingContext bindingContext, ServerWebExchange exchange) {
                return Mono.just(mockUser);
            }
        }))
        .build();
        testClient.put().uri("/api/orders/123").bodyValue(requestPrueba).accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk().expectBody()
        .jsonPath("$.id").isEqualTo("123") // El ID no debió cambiar
        .jsonPath("$.deliveryName").isEqualTo("PUTPrueba1") // El nombre se actualizó
        .jsonPath("$.deliveryStreet").isEqualTo("PUTPrueba1")
        .jsonPath("$.deliveryState").isEqualTo("PUTPrueba1")
        .jsonPath("$.deliveryStreet").isEqualTo("PUTPrueba1")
        .jsonPath("$.deliveryZip").isEqualTo("67890");
    }
    //Test put id contradictorio 
    @Test void putIncorrecto(){
        TacoOrder ordenOriginal=new TacoOrder();
        ordenOriginal.setId("123");
        ordenOriginal.setDeliveryCity("Prueba1");
        ordenOriginal.setDeliveryName("Prueba1");
        ordenOriginal.setDeliveryState("Prueba1");
        ordenOriginal.setDeliveryStreet("Prueba1");
        ordenOriginal.setDeliveryZip("12345");
        ModificacionOrderDTO requestPrueba=new ModificacionOrderDTO();
        requestPrueba.setDeliveryCity("PUTPrueba1");
        requestPrueba.setDeliveryName("PUTPrueba1");
        requestPrueba.setDeliveryState("PUTPrueba1");
        requestPrueba.setDeliveryStreet("PUTPrueba1");
        requestPrueba.setDeliveryZip("67890");
        OrderRepository repo= new Mockito().mock(OrderRepository.class);
        OrderMessagingService orderMessaging= new Mockito().mock(OrderMessagingService.class);
        EmailOrderService emailService= new Mockito().mock(EmailOrderService.class);
        OrderMapper orderMapper= new Mockito().mock(OrderMapper.class);
        Mockito.when(repo.findById("111")).thenReturn(Mono.empty());
        Mockito.when(repo.save(Mockito.any(TacoOrder.class))).thenReturn(Mono.just(ordenOriginal));
        WebTestClient testClient= WebTestClient.bindToController(new OrderApiController(repo, orderMessaging, emailService,orderMapper)).build();
        testClient.put().uri("/api/orders/111").bodyValue(requestPrueba).accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isNotFound();
    }
}

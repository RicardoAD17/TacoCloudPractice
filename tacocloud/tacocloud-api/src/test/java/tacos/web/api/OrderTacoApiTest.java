package tacos.web.api;

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
import tacos.security.error.GlobalExceptionHandler;
import tacos.web.DTO.ModificacionOrderDTO;
import tacos.web.DTO.OrderMapper;

public class OrderTacoApiTest {
    @Test
    public void zipTestValuePatch() {
        User mockUser = new User(
            "testuser",
            "{bcrypt}dummy",
            "Test User",
            "Street",
            "City",
            "State",
            "20000",
            "4491234567",
            "test@test.com"
        );
        mockUser.setId("user_id_123");
        TacoOrder ordenOriginal = new TacoOrder();
        ordenOriginal.setId("123");
        ordenOriginal.setDeliveryState("TX");
        ordenOriginal.setDeliveryZip("00000");
        ordenOriginal.setUser(mockUser);
        ModificacionOrderDTO requestPrueba = new ModificacionOrderDTO();
        requestPrueba.setDeliveryZip("12345");
        OrderRepository repo = Mockito.mock(OrderRepository.class);
        OrderMessagingService orderMessaging = Mockito.mock(OrderMessagingService.class);
        EmailOrderService emailService = Mockito.mock(EmailOrderService.class);
        OrderMapper orderMapper =
            new OrderMapper();
        Mockito.when(repo.findById("123"))
            .thenReturn(Mono.just(ordenOriginal));
        Mockito.when(repo.save(Mockito.any(TacoOrder.class)))
            .thenAnswer(invocation ->
                Mono.just(invocation.getArgument(0))
            );
        WebTestClient testClient =
            WebTestClient.bindToController(
                new OrderApiController(
                    repo,
                    orderMessaging,
                    emailService,
                    orderMapper
                )
            )
            .controllerAdvice(new GlobalExceptionHandler())
            .argumentResolvers(configurer ->
                configurer.addCustomResolver(
                    new HandlerMethodArgumentResolver() {
                        @Override
                        public boolean supportsParameter(
                                MethodParameter parameter) {
                            return parameter
                                .getParameterType()
                                .equals(User.class);
                        }
                        @Override
                        public Mono<Object> resolveArgument(
                                MethodParameter parameter,
                                BindingContext bindingContext,
                                ServerWebExchange exchange) {

                            return Mono.just(mockUser);
                        }
                    }
                )
            )
            .build();
        testClient
            .patch()
            .uri("/api/orders/123")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(requestPrueba)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody()

            .jsonPath("$.id")
            .isEqualTo("123");
    }


    @Test
    void putCorrecto() {

        User mockUser = new User(
            "testuser",
            "password",
            "Test Name",
            "Street",
            "City",
            "State",
            "12345",
            "555",
            "test@test.com"
        );

        mockUser.setId("user123");

        TacoOrder ordenOriginal = new TacoOrder();

        ordenOriginal.setId("123");
        ordenOriginal.setUser(mockUser);
        ordenOriginal.setDeliveryCity("Prueba1");
        ordenOriginal.setDeliveryName("Prueba1");
        ordenOriginal.setDeliveryState("Prueba1");
        ordenOriginal.setDeliveryStreet("Prueba1");
        ordenOriginal.setDeliveryZip("12345");

        ModificacionOrderDTO requestPrueba =
            new ModificacionOrderDTO();

        requestPrueba.setDeliveryCity("PUTPrueba1");
        requestPrueba.setDeliveryName("PUTPrueba1");
        requestPrueba.setDeliveryState("PUTPrueba1");
        requestPrueba.setDeliveryStreet("PUTPrueba1");
        requestPrueba.setDeliveryZip("67890");

        OrderRepository repo =
            Mockito.mock(OrderRepository.class);

        OrderMessagingService orderMessaging =
            Mockito.mock(OrderMessagingService.class);

        EmailOrderService emailService =
            Mockito.mock(EmailOrderService.class);

        OrderMapper orderMapper =
            new OrderMapper();

        Mockito.when(repo.findById("123"))
            .thenReturn(Mono.just(ordenOriginal));

        Mockito.when(repo.save(Mockito.any(TacoOrder.class)))
            .thenReturn(Mono.just(ordenOriginal));

        WebTestClient testClient =
            WebTestClient.bindToController(
                new OrderApiController(
                    repo,
                    orderMessaging,
                    emailService,
                    orderMapper
                )
            )
            .controllerAdvice(new GlobalExceptionHandler())
            .argumentResolvers(configurer ->
                configurer.addCustomResolver(
                    new HandlerMethodArgumentResolver() {

                        @Override
                        public boolean supportsParameter(
                                MethodParameter parameter) {

                            return parameter
                                .getParameterType()
                                .equals(User.class);
                        }

                        @Override
                        public Mono<Object> resolveArgument(
                                MethodParameter parameter,
                                BindingContext bindingContext,
                                ServerWebExchange exchange) {

                            return Mono.just(mockUser);
                        }
                    }
                )
            )
            .build();

        testClient
            .put()
            .uri("/api/orders/123")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(requestPrueba)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()

            .expectStatus()
            .isOk()

            .expectBody()

            .jsonPath("$.id")
            .isEqualTo("123")

            .jsonPath("$.deliveryName")
            .isEqualTo("PUTPrueba1");
    }


    @Test
    void putIncorrecto() {

        TacoOrder ordenOriginal =
            new TacoOrder();

        ordenOriginal.setId("123");
        ordenOriginal.setDeliveryCity("Prueba1");
        ordenOriginal.setDeliveryName("Prueba1");
        ordenOriginal.setDeliveryState("Prueba1");
        ordenOriginal.setDeliveryStreet("Prueba1");
        ordenOriginal.setDeliveryZip("12345");

        ModificacionOrderDTO requestPrueba =
            new ModificacionOrderDTO();

        requestPrueba.setDeliveryCity("PUTPrueba1");
        requestPrueba.setDeliveryName("PUTPrueba1");
        requestPrueba.setDeliveryState("PUTPrueba1");
        requestPrueba.setDeliveryStreet("PUTPrueba1");
        requestPrueba.setDeliveryZip("67890");

        OrderRepository repo =
            Mockito.mock(OrderRepository.class);

        OrderMessagingService orderMessaging =
            Mockito.mock(OrderMessagingService.class);

        EmailOrderService emailService =
            Mockito.mock(EmailOrderService.class);

        OrderMapper orderMapper =
            new OrderMapper();

        Mockito.when(repo.findById("111"))
            .thenReturn(Mono.empty());

        User mockUser = new User(
            "testuser",
            "password",
            "Test Name",
            "Street",
            "City",
            "State",
            "12345",
            "555",
            "test@test.com"
        );

        WebTestClient testClient =
            WebTestClient.bindToController(
                new OrderApiController(
                    repo,
                    orderMessaging,
                    emailService,
                    orderMapper
                )
            )
            .controllerAdvice(new GlobalExceptionHandler())
            .argumentResolvers(configurer ->
                configurer.addCustomResolver(
                    new HandlerMethodArgumentResolver() {

                        @Override
                        public boolean supportsParameter(
                                MethodParameter parameter) {

                            return parameter
                                .getParameterType()
                                .equals(User.class);
                        }

                        @Override
                        public Mono<Object> resolveArgument(
                                MethodParameter parameter,
                                BindingContext bindingContext,
                                ServerWebExchange exchange) {

                            return Mono.just(mockUser);
                        }
                    }
                )
            )
            .build();

        testClient
            .put()
            .uri("/api/orders/111")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(requestPrueba)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()

            .expectStatus()
            .isNotFound();
    }
}
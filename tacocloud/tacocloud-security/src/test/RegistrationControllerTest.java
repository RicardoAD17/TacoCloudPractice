import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Mono;
import tacos.User;
import tacos.data.UserRepository;
import tacos.security.error.ConflictException;
import tacos.web.error.GlobalExceptionHandler;

import static org.assertj.core.api.Assertions.assertThat;
public class RegistrationControllerTest {
    private UserRepository userRepo;
    private PasswordEncoder passwordEncoder;
    private WebTestClient client;

    @BeforeEach
    public void setup() {
        userRepo = Mockito.mock(UserRepository.class);
        passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        
        RegistrationController controller = new RegistrationController(userRepo, passwordEncoder);

        client = WebTestClient.bindToController(controller)
                .controllerAdvice(new GlobalExceptionHandler()) 
                .build();
    }

    @Test
    public void testPasswordEncodingAndHash() {
        
        String rawPassword = "password123";
        RegistrationForm form = new RegistrationForm();
        form.setUsername("testuser");
        form.setPassword(rawPassword);
        form.setFullname("Test Name");
        form.setEmail("test@test.com");

        User user = form.toUser(passwordEncoder);

        assertThat(user.getPassword()).isNotEqualTo(rawPassword);
        assertThat(user.getPassword()).startsWith("{bcrypt}");
        assertThat(passwordEncoder.matches(rawPassword, user.getPassword())).isTrue();
    }

    @Test
    public void testRegisterUser_Exito() {
        RegistrationForm form = new RegistrationForm();
        form.setUsername("nuevoUser");
        form.setPassword("secreta123");
        form.setFullname("Nuevo Usuario");
        form.setEmail("nuevo@test.com");

        User savedUser = new User(
            "nuevoUser", 
            passwordEncoder.encode("secreta123"), 
            "Nuevo Usuario", "Calle 1", "Aguascalientes", "AGS", "20000", "4491234567", "nuevo@test.com"
        );
        savedUser.setId("user_id_999");

        Mockito.when(userRepo.save(Mockito.any(User.class))).thenReturn(Mono.just(savedUser));

        client.post()
                .uri("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(form)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("user_id_999")
                .jsonPath("$.username").isEqualTo("nuevoUser")
                .jsonPath("$.email").isEqualTo("nuevo@test.com")
                .jsonPath("$.password").doesNotExist(); 
    }

    @Test
    public void testRegisterUser_DuplicadoConflict() {
        RegistrationForm form = new RegistrationForm();
        form.setUsername("duplicado");
        form.setPassword("secreta123");
        form.setFullname("Usuario Duplicado");
        form.setEmail("duplicado@test.com");

        Mockito.when(userRepo.save(Mockito.any(User.class)))
                .thenReturn(Mono.error(new DuplicateKeyException("Key violation")));

        client.post()
                .uri("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(form)
                .exchange()
                .expectStatus().isEqualTo(409) 
                .expectBody()
                .jsonPath("$.status").isEqualTo(409)
                .jsonPath("$.title").isEqualTo("Conflicto de Estado");
    }    
}

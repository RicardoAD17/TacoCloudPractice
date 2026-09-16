package tacos.security;

import java.net.URI;

import javax.validation.Valid;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import reactor.core.publisher.Mono;
import tacos.data.UserRepository;
import tacos.security.DTO.UserResponse;
import tacos.security.error.ConflictException;

@RestController
@RequestMapping(path = "/api/users", produces = "application/json")
public class RegistrationController {
  
  private final UserRepository userRepo;
  private final PasswordEncoder passwordEncoder;

  public RegistrationController(UserRepository userRepo, PasswordEncoder passwordEncoder) {
    this.userRepo = userRepo;
    this.passwordEncoder = passwordEncoder;
  }
  
  @PostMapping
  public Mono<ResponseEntity<UserResponse>> registerUser(
          @Valid @RequestBody RegistrationForm form, 
          UriComponentsBuilder uriBuilder) {
      
      // 1. Convertimos el form a User (esto ejecuta el HASH de la contraseña)
      return Mono.just(form.toUser(passwordEncoder))
          // 2. Intentamos guardar en la BD
          .flatMap(userToSave -> userRepo.save(userToSave))
          // 3. Mapeamos el usuario guardado a un DTO seguro (sin contraseña)
          .map(savedUser -> {
              UserResponse response = UserResponse.builder()
                  .id(savedUser.getId())
                  .username(savedUser.getUsername())
                  .fullname(savedUser.getFullname())
                  .email(savedUser.getEmail())
                  .build();
                  
              URI location = uriBuilder.path("/api/users/{id}").buildAndExpand(savedUser.getId()).toUri();
              return ResponseEntity.created(location).body(response);
          })
          // 4. Protección contra carreras: Si la BD lanza error de duplicado (índice único)
          .onErrorResume(DuplicateKeyException.class, e -> 
              Mono.error(new ConflictException("El nombre de usuario o email ya está registrado."))
          );
  }
}
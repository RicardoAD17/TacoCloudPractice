package tacos.security;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.Data;
import tacos.User;

@Data
public class RegistrationForm {

  @NotBlank(message = "El nombre de usuario es obligatorio")
  @Size(min = 4, max = 20, message = "El usuario debe tener entre 4 y 20 caracteres")
  private String username;

  @NotBlank(message = "La contraseña es obligatoria")
  @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
  private String password;

  @NotBlank(message = "El nombre completo es obligatorio")
  private String fullname;
  private String street;
  private String city;
  private String state;
  private String zip;
  private String phone;
  @NotBlank(message = "El email es obligatorio")
  @Email(message = "Debe ser un email válido")
  private String email;
  
  public User toUser(PasswordEncoder passwordEncoder) {
    return new User(
        username, passwordEncoder.encode(password), 
        fullname, street, city, state, zip, phone, email);
  }
  
}

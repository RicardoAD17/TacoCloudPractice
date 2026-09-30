package tacos.security;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.
                                              UserDetailsService;
import org.springframework.security.core.userdetails.
                                       UsernameNotFoundException;
import org.springframework.stereotype.Service;

import tacos.User;
import tacos.data.UserRepository;

@Service
public class UserRepositoryUserDetailsService 
        implements UserDetailsService {

  private UserRepository userRepo;

  @Autowired
  public UserRepositoryUserDetailsService(UserRepository userRepo) {
    this.userRepo = userRepo;
  }
  
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    
    System.out.println("[LOGIN] Postman está intentando entrar con el usuario: '" + username + "'");
    
    System.out.println(" Usuarios que realmente existen en MongoDB en este momento:");
    userRepo.findAll().doOnNext(u -> System.out.println(" -> Nombre en BD: '" + u.getUsername() + "'")).blockLast();

    // Ahora sí, buscamos al usuario
    User user = userRepo.findByUsername(username).block();
    
    if (user != null) {
      System.out.println("[LOGIN] ¡Usuario encontrado!: " + user.getUsername());
      return user;
    }
    
    System.out.println("[LOGIN] El findByUsername no encontró a: '" + username + "'");
    throw new UsernameNotFoundException("User '" + username + "' not found");
  }

}

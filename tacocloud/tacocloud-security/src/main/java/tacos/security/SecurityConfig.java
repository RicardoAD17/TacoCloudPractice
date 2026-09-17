package tacos.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation
             .authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web
             .builders.HttpSecurity;
import org.springframework.security.config.annotation.web
                        .configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web
                        .configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SuppressWarnings("deprecation")
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
  
  @Autowired
  private UserDetailsService userDetailsService;
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
      .cors()
      .and()
      .authorizeRequests()
        .antMatchers(HttpMethod.OPTIONS).permitAll() 
        .antMatchers(HttpMethod.POST, "/api/users", "/register").permitAll() 
        .antMatchers("/h2-console/**").permitAll() 

        .antMatchers(HttpMethod.GET, "/api/ingredients/**", "/api/tacos/**").permitAll()
        .antMatchers(HttpMethod.POST, "/api/ingredients/**").hasRole("ADMIN")
        .antMatchers(HttpMethod.PUT, "/api/ingredients/**").hasRole("ADMIN")
        .antMatchers(HttpMethod.DELETE, "/api/ingredients/**").hasRole("ADMIN")
        .antMatchers("/api/orders/**").hasAnyRole("USER", "ADMIN")
        .antMatchers("/api/payment-methods/**").hasAnyRole("USER","ADMIN") 
        .antMatchers("/api/kitchen/**").hasAnyRole("KITCHEN", "ADMIN")

        .antMatchers("/actuator/health").permitAll()
        .antMatchers("/actuator/**").hasRole("ADMIN")
        .antMatchers("/data-api/**").hasRole("ADMIN")
        .anyRequest().authenticated()
        
      .and()
        .formLogin()
          .loginPage("/login")
          .permitAll() 
          
      .and()
        .httpBasic()
          .realmName("Taco Cloud")
          
      .and()
        .logout()
          .logoutSuccessUrl("/")
          .permitAll() 
          
      .and()
        .csrf()
          .ignoringAntMatchers("/h2-console/**", "/api/**")

      .and()  
        .headers()
          .frameOptions()
            .sameOrigin();
  }
  @Bean
  public PasswordEncoder encoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder(); 
  }
  
  
  @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
      auth
        .userDetailsService(userDetailsService)
        .passwordEncoder(encoder());
    }

}

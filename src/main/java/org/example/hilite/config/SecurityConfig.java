package org.example.hilite.config;

import org.example.hilite.common.util.JwtUtil;
import org.example.hilite.filter.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

  private final JwtUtil jwtUtil;

  public SecurityConfig(JwtUtil jwtUtil) {
    this.jwtUtil = jwtUtil;
  }

  @Bean
  public UserDetailsService userDetailsService() {
    return new InMemoryUserDetailsManager(
        User.withUsername("user").password("{noop}password").roles("USER").build());
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }

  // todo Exception handling 구현
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth -> auth.requestMatchers("/login").permitAll().anyRequest().authenticated())
        .addFilterBefore(
            new JwtFilter(jwtUtil, userDetailsService()),
            UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}

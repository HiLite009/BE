package org.example.hilite.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hilite.common.util.JwtUtil;
import org.example.hilite.filter.ReactiveJwtAuthenticationWebFilter;
import org.example.hilite.service.ReactiveCustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class ReactiveSecurityConfig {

  private final JwtUtil jwtUtil;
  private final ReactiveCustomUserDetailsService userDetailsService;
  private final ReactiveDynamicAuthorizationManager dynamicAuthorizationManager;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public ReactiveAuthenticationManager reactiveAuthenticationManager() {
    UserDetailsRepositoryReactiveAuthenticationManager authManager =
        new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
    authManager.setPasswordEncoder(passwordEncoder());
    return authManager;
  }

  @Bean
  public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
    return http
        // CSRF 비활성화 (최신 방식)
        .csrf(csrf -> csrf.disable())

        // CORS 설정 (최신 방식)
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))

        // 세션 관리
        .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())

        // 인증 규칙 설정
        .authorizeExchange(exchanges -> exchanges
            // OPTIONS 요청 허용
            .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()

            // 공개 엔드포인트
            .pathMatchers("/login", "/signup", "/test", "/health").permitAll()
            .pathMatchers("/check-email", "/validate-signup").permitAll()

            // API 문서화 엔드포인트
            .pathMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
            .pathMatchers("/webjars/**", "/swagger-resources/**").permitAll()

            // 정적 리소스
            .pathMatchers("/static/**", "/css/**", "/js/**", "/images/**").permitAll()

            // 관리자 전용 엔드포인트
            .pathMatchers("/admin/**", "/api/admin/**").hasAuthority("ROLE_ADMIN")

            // 나머지 모든 요청은 동적 권한 관리
            .anyExchange().access(this::checkDynamicPermission))

        // JWT 필터 추가
        .addFilterBefore(
            reactiveJwtAuthenticationWebFilter(),
            SecurityWebFiltersOrder.AUTHENTICATION)

        // 예외 처리
        .exceptionHandling(exceptions -> exceptions
            .authenticationEntryPoint(customAuthenticationEntryPoint())
            .accessDeniedHandler(customAccessDeniedHandler()))

        .build();
  }

  @Bean
  public ReactiveJwtAuthenticationWebFilter reactiveJwtAuthenticationWebFilter() {
    return new ReactiveJwtAuthenticationWebFilter(jwtUtil, userDetailsService);
  }

  /**
   * 동적 권한 확인
   */
  private Mono<org.springframework.security.authorization.AuthorizationDecision> checkDynamicPermission(
      Mono<org.springframework.security.core.Authentication> authentication,
      AuthorizationContext context) {

    String requestPath = context.getExchange().getRequest().getPath().value();

    return authentication
        .cast(org.springframework.security.core.Authentication.class)
        .flatMap(auth -> {
          if (auth == null || !auth.isAuthenticated()) {
            log.debug("Authentication is null or not authenticated for path: {}", requestPath);
            return Mono.just(
                new org.springframework.security.authorization.AuthorizationDecision(false));
          }

          // 사용자의 권한 목록 추출
          java.util.Collection<String> userRoles = auth.getAuthorities().stream()
              .map(org.springframework.security.core.GrantedAuthority::getAuthority)
              .collect(java.util.stream.Collectors.toList());

          log.debug("Checking dynamic permission for user: {} with roles: {} for path: {}",
              auth.getName(), userRoles, requestPath);

          return dynamicAuthorizationManager.hasPermission(requestPath, userRoles)
              .map(org.springframework.security.authorization.AuthorizationDecision::new)
              .doOnNext(decision ->
                  log.debug("Dynamic authorization decision for {}: {}", requestPath,
                      decision.isGranted()));
        })
        .switchIfEmpty(
            Mono.just(new org.springframework.security.authorization.AuthorizationDecision(false)));
  }

  /**
   * CORS 설정 (WebFlux용)
   */
  @Bean
  public org.springframework.web.cors.reactive.CorsConfigurationSource corsConfigurationSource() {
    org.springframework.web.cors.CorsConfiguration configuration =
        new org.springframework.web.cors.CorsConfiguration();

    configuration.addAllowedOriginPattern("*");
    configuration.addAllowedMethod("*");
    configuration.addAllowedHeader("*");
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);

    org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource source =
        new org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);

    return source;
  }

  /**
   * 인증 실패 처리
   */
  @Bean
  public org.springframework.security.web.server.ServerAuthenticationEntryPoint customAuthenticationEntryPoint() {
    return (exchange, ex) -> {
      org.springframework.http.server.reactive.ServerHttpResponse response = exchange.getResponse();
      response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
      response.getHeaders().add("Content-Type", "application/json");

      String errorBody = """
          {
            "timestamp": "%s",
            "status": 401,
            "error": "Unauthorized",
            "code": "AUTHENTICATION_REQUIRED",
            "message": "인증이 필요합니다.",
            "path": "%s"
          }
          """.formatted(
          java.time.LocalDateTime.now(),
          exchange.getRequest().getPath().value()
      );

      org.springframework.core.io.buffer.DataBuffer buffer =
          response.bufferFactory().wrap(errorBody.getBytes());

      return response.writeWith(reactor.core.publisher.Mono.just(buffer));
    };
  }

  /**
   * 접근 거부 처리
   */
  @Bean
  public org.springframework.security.web.server.authorization.ServerAccessDeniedHandler customAccessDeniedHandler() {
    return (exchange, denied) -> {
      org.springframework.http.server.reactive.ServerHttpResponse response = exchange.getResponse();
      response.setStatusCode(org.springframework.http.HttpStatus.FORBIDDEN);
      response.getHeaders().add("Content-Type", "application/json");

      String errorBody = """
          {
            "timestamp": "%s",
            "status": 403,
            "error": "Forbidden",
            "code": "ACCESS_DENIED",
            "message": "접근 권한이 없습니다.",
            "path": "%s"
          }
          """.formatted(
          java.time.LocalDateTime.now(),
          exchange.getRequest().getPath().value()
      );

      org.springframework.core.io.buffer.DataBuffer buffer =
          response.bufferFactory().wrap(errorBody.getBytes());

      return response.writeWith(reactor.core.publisher.Mono.just(buffer));
    };
  }
}
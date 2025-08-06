package org.example.hilite.filter;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hilite.common.util.JwtUtil;
import org.example.hilite.service.ReactiveCustomUserDetailsService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
public class ReactiveJwtAuthenticationWebFilter implements WebFilter {

  private final JwtUtil jwtUtil;
  private final ReactiveCustomUserDetailsService userDetailsService;

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    String path = exchange.getRequest().getPath().value();
    String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

    log.debug("Processing request for path: {} with auth header: {}",
        path, authHeader != null ? "Bearer ***" : "null");

    // Authorization 헤더가 없거나 Bearer로 시작하지 않으면 그냥 진행
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      log.debug("No valid authorization header found, proceeding without authentication");
      return chain.filter(exchange);
    }

    String token = authHeader.substring(7);

    return authenticateToken(token)
        .flatMap(authentication -> {
          log.debug("Authentication successful for user: {}", authentication.getName());
          return chain.filter(exchange)
              .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
        })
        .onErrorResume(JwtException.class, ex -> {
          log.warn("JWT authentication failed: {}", ex.getMessage());
          return handleJwtError(exchange, ex);
        })
        .onErrorResume(Exception.class, ex -> {
          log.error("Unexpected error during JWT authentication: {}", ex.getMessage());
          return handleJwtError(exchange, ex);
        });
  }

  /**
   * JWT 토큰 인증 처리
   */
  private Mono<UsernamePasswordAuthenticationToken> authenticateToken(String token) {
    try {
      String username = jwtUtil.validateAndGetUsername(token);

      if (username != null) {
        return userDetailsService.findByUsername(username)
            .map(userDetails -> {
              UsernamePasswordAuthenticationToken authentication =
                  new UsernamePasswordAuthenticationToken(
                      userDetails,
                      null,
                      userDetails.getAuthorities());

              log.debug("Created authentication for user: {} with authorities: {}",
                  username, userDetails.getAuthorities());

              return authentication;
            });
      }

      return Mono.error(new JwtException("Invalid token: username is null"));

    } catch (JwtException e) {
      return Mono.error(e);
    } catch (Exception e) {
      return Mono.error(new JwtException("Token validation failed: " + e.getMessage()));
    }
  }

  /**
   * JWT 에러 처리
   */
  private Mono<Void> handleJwtError(ServerWebExchange exchange, Throwable ex) {
    log.debug("Handling JWT error: {}", ex.getMessage());

    var response = exchange.getResponse();
    response.setStatusCode(HttpStatus.UNAUTHORIZED);
    response.getHeaders().add("Content-Type", "application/json");

    String errorBody = String.format("""
        {
          "timestamp": "%s",
          "status": 401,
          "error": "Unauthorized",
          "code": "INVALID_JWT_TOKEN",
          "message": "유효하지 않은 JWT 토큰입니다.",
          "path": "%s"
        }
        """,
        java.time.LocalDateTime.now(),
        exchange.getRequest().getPath().value()
    );

    var buffer = response.bufferFactory().wrap(errorBody.getBytes());
    return response.writeWith(Mono.just(buffer));
  }
}
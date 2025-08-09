package org.example.hilite.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.example.hilite.common.util.JwtUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final UserDetailsService userDetailsService;

  public JwtFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
    this.jwtUtil = jwtUtil;
    this.userDetailsService = userDetailsService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {

    logger.info("=== JWT FILTER STARTED ===");
    String header = request.getHeader("Authorization");
    logger.info("Request URI: " + request.getRequestURI());
    logger.info("Authorization Header: " + header);

    if (header != null && header.startsWith("Bearer ")) {
      String token = header.substring(7);
      try {
        String username = jwtUtil.validateAndGetUsername(token);
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
          UserDetails userDetails = userDetailsService.loadUserByUsername(username);

          UsernamePasswordAuthenticationToken auth =
              new UsernamePasswordAuthenticationToken(
                  userDetails, null, userDetails.getAuthorities());
          auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(auth);
        }
      } catch (JwtException e) {
        // JWT 관련 예외 처리 (토큰 만료, 위조 등)
        logger.warn("JWT authentication failed: {}", e);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response
            .getWriter()
            .write(
                new ObjectMapper()
                    .writeValueAsString(
                        Map.of(
                            "status",
                            401,
                            "error",
                            "UNAUTHORIZED",
                            "message",
                            "Invalid or expired JWT token",
                            "path",
                            request.getRequestURI())));
        return; // 필터 체인을 더 이상 진행하지 않음
      }
    }

    logger.info("=== JWT FILTER ENDING ===");
    chain.doFilter(request, response);
  }
}

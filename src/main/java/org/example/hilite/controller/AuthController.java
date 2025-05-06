package org.example.hilite.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hilite.common.util.JwtUtil;
import org.example.hilite.dto.reqeust.LoginRequestDto;
import org.example.hilite.dto.reqeust.SignupRequestDto;
import org.example.hilite.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthController {

  private final AuthenticationManager authenticationManager;
  private final UserDetailsService userDetailsService;
  private final UserService userService;
  private final JwtUtil jwtUtil;

  @PostMapping("/login")
  public ResponseEntity<String> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
    String username = loginRequestDto.username();
    String password = loginRequestDto.password();

    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    UserDetails user = userDetailsService.loadUserByUsername(username);
    return ResponseEntity.ok(jwtUtil.generateToken(user.getUsername()));
  }

  @GetMapping("/check-email")
  public ResponseEntity<Boolean> checkEmail(@RequestBody String email) {
    return ResponseEntity.ok(true);
  }

  @PostMapping("/signup")
  public String signup(@RequestBody @Valid SignupRequestDto requestDto) {
    userService.signup(requestDto);
    return "회원가입이 완료되었습니다.";
  }
}

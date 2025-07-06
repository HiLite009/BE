package org.example.hilite.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hilite.common.exception.CustomException;
import org.example.hilite.common.exception.ErrorCode;
import org.example.hilite.common.util.JwtUtil;
import org.example.hilite.dto.reqeust.LoginRequestDto;
import org.example.hilite.dto.reqeust.SignupRequestDto;
import org.example.hilite.dto.response.LoginResponseDto;
import org.example.hilite.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

  private final AuthenticationManager authenticationManager;
  private final UserDetailsService userDetailsService;
  private final MemberService memberService;
  private final JwtUtil jwtUtil;

  @PostMapping("/login")
  public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
    // GlobalExceptionHandler 로그인에 대한 인증 예외를 처리
    Authentication authentication;
    try {
      authentication =
          authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(
                  loginRequestDto.username(), loginRequestDto.password()));
    } catch (BadCredentialsException e) {
      throw new CustomException(ErrorCode.LOGIN_FAILED);
    }

    UserDetails user = (UserDetails) authentication.getPrincipal();
    String token = jwtUtil.generateToken(user.getUsername());

    log.info("Member logged in successfully: {}", user.getUsername());

    return ResponseEntity.ok(new LoginResponseDto(token, user.getUsername(), "로그인이 완료되었습니다."));
  }

  @GetMapping("/check-email")
  public ResponseEntity<Boolean> checkEmail(String email) {
    return ResponseEntity.ok(true);
  }

  @PostMapping("/signup")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "OK",
            content = @Content(mediaType = "text/plain")),
        @ApiResponse(responseCode = "400", description = "Bad Request")
      })
  public String signup(@RequestBody @Valid SignupRequestDto requestDto) {
    memberService.signup(requestDto);
    return "회원가입이 완료되었습니다.";
  }
}

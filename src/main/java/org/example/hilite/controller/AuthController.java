package org.example.hilite.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "인증", description = "로그인/회원가입 API")
public class AuthController {

  private final ReactiveAuthenticationManager authenticationManager;
  private final MemberService memberService;
  private final JwtUtil jwtUtil;

  /**
   * 로그인
   */
  @PostMapping("/login")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "로그인 성공"),
      @ApiResponse(responseCode = "401", description = "로그인 실패",
          content = @Content(mediaType = "application/json"))
  })
  public Mono<ResponseEntity<LoginResponseDto>> login(
      @Valid @RequestBody LoginRequestDto loginRequestDto) {
    log.debug("Login attempt for username: {}", loginRequestDto.username());

    return authenticationManager
        .authenticate(new UsernamePasswordAuthenticationToken(
            loginRequestDto.username(),
            loginRequestDto.password()))
        .map(authentication -> {
          String username = authentication.getName();
          String token = jwtUtil.generateToken(username);

          log.info("Member logged in successfully: {}", username);

          LoginResponseDto response = new LoginResponseDto(
              token,
              username,
              "로그인이 완료되었습니다."
          );

          return ResponseEntity.ok(response);
        })
        .onErrorMap(AuthenticationException.class,
            ex -> {
              log.warn("Authentication failed for user: {}", loginRequestDto.username());
              return new CustomException(ErrorCode.LOGIN_FAILED);
            })
        .doOnError(error ->
            log.error("Login error for user {}: {}", loginRequestDto.username(),
                error.getMessage()));
  }

  /**
   * 이메일 중복 확인
   */
  @GetMapping("/check-email")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "이메일 중복 확인 완료")
  })
  public Mono<ResponseEntity<Boolean>> checkEmail(@RequestParam String email) {
    log.debug("Checking email duplication: {}", email);

    return memberService.checkEmail(email)
        .map(exists -> {
          log.debug("Email {} exists: {}", email, exists);
          return ResponseEntity.ok(!exists); // 사용 가능하면 true 반환
        })
        .doOnError(error ->
            log.error("Error checking email {}: {}", email, error.getMessage()));
  }

  /**
   * 회원가입
   */
  @PostMapping("/signup")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "회원가입 성공",
          content = @Content(mediaType = "text/plain")),
      @ApiResponse(responseCode = "400", description = "잘못된 요청")
  })
  public Mono<ResponseEntity<String>> signup(@RequestBody @Valid SignupRequestDto requestDto) {
    log.debug("Signup attempt for username: {}", requestDto.username());

    return memberService.signup(requestDto)
        .then(Mono.fromCallable(() -> {
          log.info("Member signup completed successfully: {}", requestDto.username());
          return ResponseEntity.ok("회원가입이 완료되었습니다.");
        }))
        .doOnError(error ->
            log.error("Signup error for user {}: {}", requestDto.username(), error.getMessage()));
  }

  /**
   * 회원가입 폼 유효성 사전 검증
   */
  @PostMapping("/validate-signup")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "유효성 검증 완료")
  })
  public Mono<ResponseEntity<String>> validateSignup(
      @RequestBody @Valid SignupRequestDto requestDto) {
    log.debug("Validating signup form for username: {}", requestDto.username());

    // 비밀번호 확인 검증
    if (!requestDto.password().equals(requestDto.passwordConfirm())) {
      return Mono.just(ResponseEntity.badRequest()
          .body("비밀번호와 비밀번호 확인이 일치하지 않습니다."));
    }

    // 사용자명 중복 검사
    return memberService.getMemberInfo(requestDto.username())
        .then(Mono.just(ResponseEntity.badRequest()
            .body("이미 존재하는 아이디입니다.")))
        .onErrorReturn(ResponseEntity.ok("유효한 회원가입 정보입니다."));
  }
}
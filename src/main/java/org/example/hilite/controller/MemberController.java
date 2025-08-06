package org.example.hilite.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hilite.common.exception.ApiErrorResponse;
import org.example.hilite.dto.response.MemberResponseDto;
import org.example.hilite.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "회원 관리", description = "회원 정보 조회 및 관리 API")
public class MemberController {

  private final MemberService memberService;

  /**
   * 내 기본 정보 조회
   */
  @GetMapping("/info")
  @Operation(summary = "내 기본 정보 조회", description = "현재 로그인한 사용자의 기본 정보를 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "정보 조회 성공",
          content = {@Content(schema = @Schema(implementation = MemberResponseDto.class))}),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
          content = {@Content(schema = @Schema(implementation = ApiErrorResponse.class))})
  })
  public Mono<ResponseEntity<MemberResponseDto>> getMyInfo(ServerWebExchange exchange) {
    return exchange.getPrincipal()
        .cast(java.security.Principal.class)
        .map(Principal::getName)
        .flatMap(username -> {
          log.debug("Getting info for authenticated user: {}", username);
          return memberService.getMemberInfo(username);
        })
        .map(ResponseEntity::ok)
        .doOnSuccess(response ->
            log.debug("Successfully retrieved member info"))
        .doOnError(error ->
            log.error("Error retrieving member info: {}", error.getMessage()));
  }

  /**
   * 특정 사용자 정보 조회 (관리자 권한 또는 본인만)
   */
  @GetMapping("/info/{username}")
  @Operation(summary = "특정 사용자 정보 조회", description = "특정 사용자의 정보를 조회합니다. (본인 또는 관리자만 가능)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "정보 조회 성공",
          content = {@Content(schema = @Schema(implementation = MemberResponseDto.class))}),
      @ApiResponse(responseCode = "403", description = "권한 없음",
          content = {@Content(schema = @Schema(implementation = ApiErrorResponse.class))}),
      @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
          content = {@Content(schema = @Schema(implementation = ApiErrorResponse.class))})
  })
  public Mono<ResponseEntity<MemberResponseDto>> getMemberInfo(
      @PathVariable String username,
      ServerWebExchange exchange) {

    return exchange.getPrincipal()
        .cast(java.security.Principal.class)
        .map(Principal::getName)
        .flatMap(currentUsername -> {
          log.debug("User {} requesting info for user {}", currentUsername, username);

          // 본인 정보 조회이거나 관리자인 경우만 허용 (추후 Security에서 처리 가능)
          return memberService.getMemberInfo(username);
        })
        .map(ResponseEntity::ok)
        .doOnSuccess(response ->
            log.debug("Successfully retrieved member info for: {}", username))
        .doOnError(error ->
            log.error("Error retrieving member info for {}: {}", username, error.getMessage()));
  }

  /**
   * 역할 추가 (관리자 전용)
   */
  @PostMapping("/{username}/roles")
  @Operation(summary = "사용자에게 역할 추가", description = "특정 사용자에게 역할을 추가합니다. (관리자 전용)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "역할 추가 성공"),
      @ApiResponse(responseCode = "403", description = "권한 없음"),
      @ApiResponse(responseCode = "404", description = "사용자 또는 역할을 찾을 수 없음")
  })
  public Mono<ResponseEntity<String>> addRoleToMember(
      @PathVariable String username,
      @RequestParam String roleName,
      ServerWebExchange exchange) {

    return exchange.getPrincipal()
        .cast(java.security.Principal.class)
        .map(Principal::getName)
        .flatMap(currentUsername -> {
          log.debug("Admin {} adding role {} to user {}", currentUsername, roleName, username);

          return memberService.addRoleToMember(username, roleName)
              .then(Mono.just(ResponseEntity.ok("역할이 성공적으로 추가되었습니다.")));
        })
        .doOnSuccess(response ->
            log.info("Successfully added role {} to member {}", roleName, username))
        .doOnError(error ->
            log.error("Error adding role {} to member {}: {}", roleName, username, error.getMessage()));
  }

  /**
   * 역할 제거 (관리자 전용)
   */
  @PostMapping("/{username}/roles/remove")
  @Operation(summary = "사용자에서 역할 제거", description = "특정 사용자에서 역할을 제거합니다. (관리자 전용)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "역할 제거 성공"),
      @ApiResponse(responseCode = "403", description = "권한 없음"),
      @ApiResponse(responseCode = "404", description = "사용자 또는 역할을 찾을 수 없음")
  })
  public Mono<ResponseEntity<String>> removeRoleFromMember(
      @PathVariable String username,
      @RequestParam String roleName,
      ServerWebExchange exchange) {

    return exchange.getPrincipal()
        .cast(java.security.Principal.class)
        .map(Principal::getName)
        .flatMap(currentUsername -> {
          log.debug("Admin {} removing role {} from user {}", currentUsername, roleName, username);

          return memberService.removeRoleFromMember(username, roleName)
              .then(Mono.just(ResponseEntity.ok("역할이 성공적으로 제거되었습니다.")));
        })
        .doOnSuccess(response ->
            log.info("Successfully removed role {} from member {}", roleName, username))
        .doOnError(error ->
            log.error("Error removing role {} from member {}: {}", roleName, username, error.getMessage()));
  }
}
package org.example.hilite.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hilite.common.exception.ApiErrorResponse;
import org.example.hilite.dto.reqeust.ProtectedPageRequestDto;
import org.example.hilite.dto.reqeust.RoleRequestDto;
import org.example.hilite.dto.response.MemberResponseDto;
import org.example.hilite.dto.response.ProtectedPageResponseDto;
import org.example.hilite.dto.response.RoleResponseDto;
import org.example.hilite.service.AccessPageService;
import org.example.hilite.service.MemberService;
import org.example.hilite.service.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "관리자", description = "관리자 전용 API")
public class AdminController {

  private final RoleService roleService;
  private final AccessPageService accessPageService;
  private final MemberService memberService;

  // ==================== 역할 관리 ====================

  /**
   * 권한 생성
   */
  @PostMapping("/roles")
  @Operation(summary = "권한 생성", description = "새로운 권한을 생성합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "권한 생성 성공",
          content = {@Content(schema = @Schema(implementation = RoleResponseDto.class))}),
      @ApiResponse(responseCode = "409", description = "이미 존재하는 권한",
          content = {@Content(schema = @Schema(implementation = ApiErrorResponse.class))}),
      @ApiResponse(responseCode = "500", description = "권한 생성 실패",
          content = {@Content(schema = @Schema(implementation = ApiErrorResponse.class))})
  })
  public Mono<ResponseEntity<RoleResponseDto>> createRole(@RequestBody RoleRequestDto dto) {
    log.debug("Admin creating role: {}", dto.name());

    return roleService.createRole(dto)
        .map(ResponseEntity::ok)
        .doOnSuccess(response ->
            log.info("Successfully created role: {}", response.getBody().name()))
        .doOnError(error ->
            log.error("Error creating role {}: {}", dto.name(), error.getMessage()));
  }

  /**
   * 모든 권한 조회
   */
  @GetMapping("/roles")
  @Operation(summary = "모든 권한 조회", description = "시스템의 모든 권한을 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "권한 목록 조회 성공")
  })
  public Flux<RoleResponseDto> getRoles() {
    log.debug("Admin getting all roles");

    return roleService.getAllRoles()
        .doOnNext(role -> log.debug("Retrieved role: {}", role.name()))
        .doOnError(error -> log.error("Error getting all roles: {}", error.getMessage()));
  }

  /**
   * 권한 삭제
   */
  @DeleteMapping("/roles/{id}")
  @Operation(summary = "권한 삭제", description = "특정 권한을 삭제합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "권한 삭제 성공"),
      @ApiResponse(responseCode = "404", description = "권한을 찾을 수 없음",
          content = {@Content(schema = @Schema(implementation = ApiErrorResponse.class))})
  })
  public Mono<ResponseEntity<Void>> deleteRole(@PathVariable Long id) {
    log.debug("Admin deleting role with id: {}", id);

    return roleService.deleteRole(id)
        .then(Mono.just(ResponseEntity.noContent().<Void>build()))
        .doOnSuccess(response -> log.info("Successfully deleted role with id: {}", id))
        .doOnError(error -> log.error("Error deleting role {}: {}", id, error.getMessage()));
  }

  /**
   * 접근 페이지 생성
   */
  @PostMapping("/access-pages")
  @Operation(summary = "접근 페이지 생성", description = "새로운 접근 페이지를 생성합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "페이지 생성 성공",
          content = {@Content(schema = @Schema(implementation = ProtectedPageResponseDto.class))}),
      @ApiResponse(responseCode = "409", description = "이미 존재하는 경로",
          content = {@Content(schema = @Schema(implementation = ApiErrorResponse.class))})
  })
  public Mono<ResponseEntity<ProtectedPageResponseDto>> createAccessPage(
      @RequestBody ProtectedPageRequestDto request) {
    log.debug("Admin creating access page: {}", request.path());

    return accessPageService.saveAccessPage(request.path())
        .map(ResponseEntity::ok)
        .doOnSuccess(response ->
            log.info("Successfully created access page: {}", response.getBody().path()))
        .doOnError(error ->
            log.error("Error creating access page {}: {}", request.path(), error.getMessage()));
  }

  /**
   * 모든 접근 페이지 조회
   */
  @GetMapping("/access-pages")
  @Operation(summary = "모든 접근 페이지 조회", description = "시스템의 모든 접근 페이지를 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "페이지 목록 조회 성공")
  })
  public Flux<ProtectedPageResponseDto> getAccessPages() {
    log.debug("Admin getting all access pages");

    return accessPageService.getAllAccessPages()
        .doOnNext(page -> log.debug("Retrieved access page: {}", page.path()))
        .doOnError(error -> log.error("Error getting all access pages: {}", error.getMessage()));
  }

  /**
   * 접근 페이지 삭제
   */
  @DeleteMapping("/access-pages/{id}")
  @Operation(summary = "접근 페이지 삭제", description = "특정 접근 페이지를 삭제합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "페이지 삭제 성공"),
      @ApiResponse(responseCode = "404", description = "페이지를 찾을 수 없음",
          content = {@Content(schema = @Schema(implementation = ApiErrorResponse.class))})
  })
  public Mono<ResponseEntity<Void>> deleteAccessPage(@PathVariable Long id) {
    log.debug("Admin deleting access page with id: {}", id);

    return accessPageService.deleteAccessPage(id)
        .then(Mono.just(ResponseEntity.noContent().<Void>build()))
        .doOnSuccess(response -> log.info("Successfully deleted access page with id: {}", id))
        .doOnError(error -> log.error("Error deleting access page {}: {}", id, error.getMessage()));
  }

  // ==================== 회원 관리 ====================

  /**
   * 모든 회원 조회
   */
  @GetMapping("/member/list")
  @Operation(summary = "모든 회원 조회", description = "모든 회원의 기본 정보를 조회합니다. (관리자 전용)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "회원 목록 조회 성공"),
      @ApiResponse(responseCode = "403", description = "권한 없음",
          content = {@Content(schema = @Schema(implementation = ApiErrorResponse.class))})
  })
  public Flux<MemberResponseDto> getAllMembers(ServerWebExchange exchange) {
    return exchange.getPrincipal()
        .cast(java.security.Principal.class)
        .map(java.security.Principal::getName)
        .flatMapMany(adminUsername -> {
          log.debug("Admin {} requesting all members list", adminUsername);

          return memberService.getAllMembers()
              .doOnNext(member -> log.debug("Retrieved member: {}", member.username()));
        })
        .doOnError(error -> log.error("Error getting all members: {}", error.getMessage()));
  }

  /**
   * 특정 회원 조회
   */
  @GetMapping("/member/{username}")
  @Operation(summary = "특정 회원 조회", description = "특정 회원의 상세 정보를 조회합니다. (관리자 전용)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "회원 정보 조회 성공",
          content = {@Content(schema = @Schema(implementation = MemberResponseDto.class))}),
      @ApiResponse(responseCode = "403", description = "권한 없음",
          content = {@Content(schema = @Schema(implementation = ApiErrorResponse.class))}),
      @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
          content = {@Content(schema = @Schema(implementation = ApiErrorResponse.class))})
  })
  public Mono<ResponseEntity<MemberResponseDto>> getMemberProfile(
      @PathVariable String username,
      ServerWebExchange exchange) {

    return exchange.getPrincipal()
        .cast(java.security.Principal.class)
        .map(java.security.Principal::getName)
        .flatMap(adminUsername -> {
          log.debug("Admin {} requesting profile for user: {}", adminUsername, username);

          return memberService.getMemberInfo(username)
              .map(ResponseEntity::ok);
        })
        .doOnSuccess(response ->
            log.debug("Successfully retrieved member profile for: {}", username))
        .doOnError(error ->
            log.error("Error retrieving member profile for {}: {}", username, error.getMessage()));
  }
}
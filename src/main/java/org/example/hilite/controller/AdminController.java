package org.example.hilite.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {

  private final RoleService roleService;
  private final AccessPageService accessPageService;
  private final MemberService memberService;

  @PostMapping("/roles")
  @Operation(summary = "권한 생성", description = "권한을 생성합니다.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "권한 생성 성공",
            content = {@Content(schema = @Schema(implementation = RoleResponseDto.class))}),
        @ApiResponse(
            responseCode = "500",
            description = "권한 생성 실패",
            content = {@Content(schema = @Schema(implementation = ApiErrorResponse.class))})
      })
  public ResponseEntity<RoleResponseDto> createRole(@RequestBody RoleRequestDto dto) {
    return ResponseEntity.ok(roleService.createRole(dto));
  }

  @GetMapping("/roles")
  public ResponseEntity<List<RoleResponseDto>> getRoles() {
    return ResponseEntity.ok(roleService.getAllRoles());
  }

  @DeleteMapping("/roles/{id}")
  public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
    roleService.deleteRole(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/access-pages")
  public ResponseEntity<ProtectedPageResponseDto> createAccessPage(
      @RequestBody ProtectedPageRequestDto request) {
    ProtectedPageResponseDto response = accessPageService.saveAccessPage(request.path());
    return ResponseEntity.ok(response);
  }

  @GetMapping("/access-pages")
  public ResponseEntity<List<ProtectedPageResponseDto>> getAccessPages() {
    return ResponseEntity.ok(accessPageService.getAllAccessPages());
  }

  @DeleteMapping("/access-pages/{id}")
  public ResponseEntity<Void> deleteAccessPage(@PathVariable Long id) {
    accessPageService.deleteAccessPage(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/member/list")
  @Operation(summary = "모든 회원 조회", description = "모든 회원의 기본 정보를 조회합니다. (관리자 전용)")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "회원 목록 조회 성공"),
        @ApiResponse(
            responseCode = "403",
            description = "권한 없음",
            content = {@Content(schema = @Schema(implementation = ApiErrorResponse.class))})
      })
  public ResponseEntity<List<MemberResponseDto>> getAllMembers() {
    List<MemberResponseDto> members = memberService.getAllMembers();
    return ResponseEntity.ok(members);
  }

  @GetMapping("/member/{username}")
  @Operation(summary = "특정 회원 조회", description = "특정 회원의 상세 정보를 조회합니다. (관리자 전용)")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "회원 정보 조회 성공",
            content = {@Content(schema = @Schema(implementation = MemberResponseDto.class))}),
        @ApiResponse(
            responseCode = "403",
            description = "권한 없음",
            content = {@Content(schema = @Schema(implementation = ApiErrorResponse.class))}),
        @ApiResponse(
            responseCode = "404",
            description = "사용자를 찾을 수 없음",
            content = {@Content(schema = @Schema(implementation = ApiErrorResponse.class))})
      })
  public ResponseEntity<MemberResponseDto> getMemberProfile(@PathVariable String username) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    log.debug(
        "Admin endpoint accessed by: {} with authorities: {} for target user: {}",
        auth.getName(),
        auth.getAuthorities(),
        username);

    MemberResponseDto profile = memberService.getMemberInfo(username);
    return ResponseEntity.ok(profile);
  }
}

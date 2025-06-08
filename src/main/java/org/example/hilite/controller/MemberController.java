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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "회원 관리", description = "회원 정보 조회 API")
public class MemberController {

  private final MemberService memberService;

  @GetMapping("/info")
  @Operation(summary = "내 기본 정보 조회", description = "현재 로그인한 사용자의 기본 정보를 조회합니다.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "정보 조회 성공",
            content = {@Content(schema = @Schema(implementation = MemberResponseDto.class))}),
        @ApiResponse(
            responseCode = "401",
            description = "인증되지 않은 사용자",
            content = {@Content(schema = @Schema(implementation = ApiErrorResponse.class))})
      })
  public ResponseEntity<MemberResponseDto> getMyInfo(Principal principal) {
    MemberResponseDto info = memberService.getMemberInfo(principal.getName());
    return ResponseEntity.ok(info);
  }
}

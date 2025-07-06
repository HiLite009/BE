package org.example.hilite.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponseDto(
    @Schema(description = "JWT 토큰", example = "eyJhbGciOiJIUzI1NiJ9...") String token,
    @Schema(description = "사용자 이름", example = "testuser") String username,
    @Schema(description = "응답 메시지", example = "로그인이 완료되었습니다.") String message) {}

package org.example.hilite.dto.reqeust;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequestDto(
    @NotBlank(message = "아이디는 필수 입력값입니다.") String username,
    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
        @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력하세요.")
        @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$",
            message = "비밀번호는 문자와 숫자를 포함해야 합니다.")
        String password,
    @NotBlank(message = "비밀번호 확인은 필수 입력값입니다.") String passwordConfirm,
    @NotBlank(message = "이메일은 필수 입력값입니다.") @Email(message = "올바른 이메일 형식이 아닙니다.") String email) {}

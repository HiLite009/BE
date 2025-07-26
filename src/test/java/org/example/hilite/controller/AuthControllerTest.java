package org.example.hilite.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.hilite.dto.reqeust.LoginRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("로그인 성공 테스트")
    void loginSuccessTest() throws Exception {
        LoginRequestDto loginRequestDto = new LoginRequestDto("user", "user123"); // Use existing test user
        String requestBody = objectMapper.writeValueAsString(loginRequestDto);

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andDo(document("login-success",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Auth")
                                .description("로그인 성공")
                                .requestFields(
                                        fieldWithPath("username").description("사용자 이름"),
                                        fieldWithPath("password").description("비밀번호")
                                )
                                .responseFields(
                                        fieldWithPath("token").description("JWT 토큰"),
                                        fieldWithPath("username").description("로그인한 사용자 이름"),
                                        fieldWithPath("message").description("응답 메시지")
                                )
                                .build())));
    }

    @Test
    @DisplayName("로그인 실패 테스트 - 잘못된 자격 증명")
    void loginFailureInvalidCredentialsTest() throws Exception {
        LoginRequestDto loginRequestDto = new LoginRequestDto("nonexistentuser", "wrongpassword");
        String requestBody = objectMapper.writeValueAsString(loginRequestDto);

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized()) // Assuming 401 for bad credentials
                .andDo(document("login-failure-invalid-credentials",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Auth")
                                .description("로그인 실패 - 잘못된 자격 증명")
                                .requestFields(
                                        fieldWithPath("username").description("사용자 이름"),
                                        fieldWithPath("password").description("비밀번호")
                                )
                                .responseFields(
                                        fieldWithPath("timestamp").description("오류 발생 시간"),
                                        fieldWithPath("status").description("HTTP 상태 코드"),
                                        fieldWithPath("error").description("오류 메시지"),
                                        fieldWithPath("code").description("커스텀 오류 코드"),
                                        fieldWithPath("message").description("커스텀 오류 메시지"),
                                        fieldWithPath("path").description("요청 경로"),
                                        fieldWithPath("fieldErrors").description("필드 오류 목록").optional()
                                )
                                .build())));
    }

    @Test
    @DisplayName("로그인 실패 테스트 - 유효성 검사 실패 (빈 아이디)")
    void loginFailureEmptyUsernameTest() throws Exception {
        LoginRequestDto loginRequestDto = new LoginRequestDto("", "password");
        String requestBody = objectMapper.writeValueAsString(loginRequestDto);

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest()) // Assuming 400 for validation errors
                .andDo(document("login-failure-empty-username",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Auth")
                                .description("로그인 실패 - 유효성 검사 실패 (빈 아이디)")
                                .requestFields(
                                        fieldWithPath("username").description("사용자 이름"),
                                        fieldWithPath("password").description("비밀번호")
                                )
                                .responseFields(
                                        fieldWithPath("timestamp").description("오류 발생 시간"),
                                        fieldWithPath("status").description("HTTP 상태 코드"),
                                        fieldWithPath("error").description("오류 메시지"),
                                        fieldWithPath("code").description("커스텀 오류 코드"),
                                        fieldWithPath("message").description("커스텀 오류 메시지"),
                                        fieldWithPath("path").description("요청 경로"),
                                        fieldWithPath("fieldErrors.username").description("아이디 유효성 검사 오류 메시지")
                                )
                                .build())));
    }

    @Test
    @DisplayName("로그인 실패 테스트 - 유효성 검사 실패 (빈 비밀번호)")
    void loginFailureEmptyPasswordTest() throws Exception {
        LoginRequestDto loginRequestDto = new LoginRequestDto("testuser", "");
        String requestBody = objectMapper.writeValueAsString(loginRequestDto);

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest()) // Assuming 400 for validation errors
                .andDo(document("login-failure-empty-password",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Auth")
                                .description("로그인 실패 - 유효성 검사 실패 (빈 비밀번호)")
                                .requestFields(
                                        fieldWithPath("username").description("사용자 이름"),
                                        fieldWithPath("password").description("비밀번호")
                                )
                                .responseFields(
                                        fieldWithPath("timestamp").description("오류 발생 시간"),
                                        fieldWithPath("status").description("HTTP 상태 코드"),
                                        fieldWithPath("error").description("오류 메시지"),
                                        fieldWithPath("code").description("커스텀 오류 코드"),
                                        fieldWithPath("message").description("커스텀 오류 메시지"),
                                        fieldWithPath("path").description("요청 경로"),
                                        fieldWithPath("fieldErrors.password").description("비밀번호 유효성 검사 오류 메시지")
                                )
                                .build())));
    }
}

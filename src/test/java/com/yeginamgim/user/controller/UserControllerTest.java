package com.yeginamgim.user.controller;

import com.yeginamgim.auth.jwt.JWTService;
import com.yeginamgim.global.exception.InvalidTokenException;
import com.yeginamgim.user.dto.response.UserSignupResponseDto;
import com.yeginamgim.user.dto.request.UserWithdrawRequestDto;
import com.yeginamgim.user.dto.response.UserInfoResponseDto;
import com.yeginamgim.user.dto.response.UserWithdrawResponseDto;
import com.yeginamgim.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasKey;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class UserControllerTest {

    private final UserService userService = mock(UserService.class);
    private final JWTService jwtService = mock(JWTService.class);
    private final UserController userController = new UserController(userService, jwtService);
    private final MockMvc mockMvc = standaloneSetup(userController).build();

    @Test
    void signupAcceptsMultipartFormDataWithoutProfileImage() throws Exception {
        UserSignupResponseDto serviceResponse = UserSignupResponseDto.builder()
                .email("new@example.com")
                .nickname("new-user")
                .birthDate("060615")
                .build();

        when(userService.signup(any())).thenReturn(serviceResponse);

        mockMvc.perform(multipart("/api/user/signup")
                        .param("email", "new@example.com")
                        .param("password", "password123")
                        .param("nickname", "new-user")
                        .param("birthDate", "060615"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new@example.com"))
                .andExpect(jsonPath("$.nickname").value("new-user"))
                .andExpect(jsonPath("$", not(hasKey("password"))));
    }

    @Test
    void signupRejectsJsonRequestBody() throws Exception {
        mockMvc.perform(post("/api/user/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "new@example.com",
                                  "password": "password123",
                                  "nickname": "new-user"
                                }
                                """))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void getMyInfoDelegatesAuthorizationHeaderParsingToJwtService() {
        when(jwtService.extractEmailFromBearerToken("Bearer token")).thenReturn("user@example.com");
        when(userService.getMyInfo("user@example.com")).thenReturn(UserInfoResponseDto.builder()
                .email("user@example.com")
                .nickname("user")
                .build());

        ResponseEntity<?> response = userController.getMyInfo("Bearer token");

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(jwtService).extractEmailFromBearerToken("Bearer token");
    }

    @Test
    void getMyInfoPropagatesInvalidTokenExceptionFromJwtService() {
        when(jwtService.extractEmailFromBearerToken(null)).thenThrow(new InvalidTokenException());

        assertThatThrownBy(() -> userController.getMyInfo(null))
                .isInstanceOf(InvalidTokenException.class);
        verify(jwtService).extractEmailFromBearerToken(null);
    }

    @Test
    void withdrawDelegatesTokenSubjectToUserService() {
        UserWithdrawRequestDto request = UserWithdrawRequestDto.builder()
                .password("password123")
                .build();
        UserWithdrawResponseDto serviceResponse = UserWithdrawResponseDto.of(LocalDateTime.now());

        when(jwtService.extractEmailFromBearerToken("Bearer token")).thenReturn("user@example.com");
        when(userService.withdraw("user@example.com", request)).thenReturn(serviceResponse);

        ResponseEntity<?> response = userController.withdraw("Bearer token", request);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo(serviceResponse);
        verify(jwtService).extractEmailFromBearerToken("Bearer token");
        verify(userService).withdraw("user@example.com", request);
    }
}

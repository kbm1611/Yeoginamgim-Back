package com.yeginamgim.user.controller;

import com.yeginamgim.auth.jwt.JWTService;
import com.yeginamgim.global.exception.InvalidTokenException;
import com.yeginamgim.user.dto.request.UserWithdrawRequestDto;
import com.yeginamgim.user.dto.response.UserInfoResponseDto;
import com.yeginamgim.user.dto.response.UserWithdrawResponseDto;
import com.yeginamgim.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserControllerTest {

    private final UserService userService = mock(UserService.class);
    private final JWTService jwtService = mock(JWTService.class);
    private final UserController userController = new UserController(userService, jwtService);

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

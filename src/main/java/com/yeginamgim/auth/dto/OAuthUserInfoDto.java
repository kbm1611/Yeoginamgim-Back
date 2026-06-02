package com.yeginamgim.auth.dto;

import com.yeginamgim.user.enums.LoginProvider;

public record OAuthUserInfoDto(
        LoginProvider provider,
        String providerId,
        String email,
        String nickname,
        String profileImageUrl
) {
}

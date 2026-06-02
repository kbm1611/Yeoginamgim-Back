package com.yeginamgim.auth.oauth;

import com.yeginamgim.user.enums.LoginProvider;

public record OAuthUserInfo(
        LoginProvider provider,
        String providerId,
        String email,
        String nickname,
        String profileImageUrl
) {
}

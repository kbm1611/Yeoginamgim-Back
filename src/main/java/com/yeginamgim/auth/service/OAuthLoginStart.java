package com.yeginamgim.auth.service;

import java.time.Duration;

public record OAuthLoginStart(
        String authorizationUrl,
        String state,
        Duration ttl
) {
}

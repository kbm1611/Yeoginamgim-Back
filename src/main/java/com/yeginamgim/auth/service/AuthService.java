package com.yeginamgim.auth.service;

import com.yeginamgim.auth.dto.request.LoginRequestDto;
import com.yeginamgim.auth.dto.response.LoginResponseDto;
import com.yeginamgim.auth.jwt.JWTService;
import com.yeginamgim.user.entity.UserEntity;
import com.yeginamgim.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepo;
    private final JWTService jwtSvc;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 로그인
    public LoginResponseDto login(LoginRequestDto loginReqDto){
        UserEntity userEntity = userRepo.findByEmail(loginReqDto.getEmail()).orElse(null);

        if(userEntity == null) return null;

        boolean isPasswordMatch = passwordEncoder.matches(
                loginReqDto.getPassword(),
                userEntity.getPassword()
        );

        if(!isPasswordMatch) return null;

        String token = jwtSvc.createToken(userEntity.getEmail());

        return LoginResponseDto.builder()
                .token(token)
                .email(userEntity.getEmail())
                .nickname(userEntity.getNickname())
                .profileImageUrl(userEntity.getProfileImageUrl())
                .build();
    }


}

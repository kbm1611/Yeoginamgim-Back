package com.yeginamgim.user.service;

import com.yeginamgim.global.exception.DuplicateMemberException;
import com.yeginamgim.global.exception.UserNotFoundException;
import com.yeginamgim.global.file.FileService;
import com.yeginamgim.user.dto.request.UserSignupRequestDto;
import com.yeginamgim.user.dto.request.UserUpdateRequestDto;
import com.yeginamgim.user.dto.response.UserInfoResponseDto;
import com.yeginamgim.user.dto.response.UserSignupResponseDto;
import com.yeginamgim.user.entity.UserEntity;
import com.yeginamgim.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepo;
    private final FileService fileSvc;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserSignupResponseDto signup(UserSignupRequestDto userReqDto) {
        if (userRepo.findByEmail(userReqDto.getEmail()).isPresent()) {
            throw new DuplicateMemberException();
        }

        UserEntity saveEntity = userReqDto.toEntity();

        String fileName = fileSvc.profileUpload(userReqDto.getProfileUploadFile());
        if (fileName != null) {
            saveEntity.setProfileImageUrl("/upload/profile/" + fileName);
        }

        String pwd = passwordEncoder.encode(userReqDto.getPassword());
        saveEntity.setPassword(pwd);

        try {
            UserEntity savedEntity = userRepo.save(saveEntity);
            return UserSignupResponseDto.builder()
                    .email(savedEntity.getEmail())
                    .nickname(savedEntity.getNickname())
                    .profileImageUrl(savedEntity.getProfileImageUrl())
                    .createdAt(savedEntity.getCreatedAt())
                    .build();
        } catch (DataIntegrityViolationException e) {
            fileSvc.deleteProfileFile(saveEntity.getProfileImageUrl());
            throw new DuplicateMemberException();
        }
    }

    public UserInfoResponseDto getMyInfo(String email) {
        UserEntity userEntity = userRepo.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        return userEntity.toInfoDto();
    }

    @Transactional
    public UserInfoResponseDto updateUserInfo(String email, UserUpdateRequestDto userUpdDto) {
        UserEntity userEntity = userRepo.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        if (userUpdDto.getNickname() != null) {
            userEntity.setNickname(userUpdDto.getNickname());
        }

        String fileName = fileSvc.profileUpload(userUpdDto.getProfileUploadFile());
        if (fileName != null) {
            String oldProfileImageUrl = userEntity.getProfileImageUrl();
            userEntity.setProfileImageUrl("/upload/profile/" + fileName);
            fileSvc.deleteProfileFile(oldProfileImageUrl);
        }

        return userEntity.toInfoDto();
    }
}

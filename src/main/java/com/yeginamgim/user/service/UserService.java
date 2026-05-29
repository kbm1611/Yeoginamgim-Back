package com.yeginamgim.user.service;

import com.yeginamgim.global.exception.DuplicateMemberException;
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

    // 회원가입
    public UserSignupResponseDto signup(UserSignupRequestDto userReqDto ){

        // 이메일이 존재하는 지 확인
        if(userRepo.findByEmail(userReqDto.getEmail()).isPresent()) throw new DuplicateMemberException();

        UserEntity saveEntity = userReqDto.toEntity();

        // 프로필 이미지 처리
        String fileName = fileSvc.profileUpload( userReqDto.getProfileUploadFile() );
        if( fileName != null ) {
            saveEntity.setProfileImageUrl( "/upload/profile/" + fileName );
        }

        // 비밀번호 처리
        String pwd = passwordEncoder.encode( userReqDto.getPassword() );
        saveEntity.setPassword(pwd);

        try{
            UserEntity savedEntity = userRepo.save(saveEntity);
            return UserSignupResponseDto.builder()
                    .email(savedEntity.getEmail())
                    .nickname(savedEntity.getNickname())
                    .profileImageUrl(savedEntity.getProfileImageUrl())
                    .createdAt(savedEntity.getCreatedAt())
                    .build();

            // DB 유니크 제약 조건에 걸릴 시 --> 동시에 가입한 경우
        } catch( DataIntegrityViolationException e){
            fileSvc.deleteProfileFile(saveEntity.getProfileImageUrl());
            throw new DuplicateMemberException();
        }
    }

    // 정보 조회
    public UserInfoResponseDto getMyInfo(String email){
        UserEntity userEntity = userRepo.findByEmail(email).orElse(null);

        if(userEntity == null) return null;
        return userEntity.toInfoDto();
    }

    // 정보수정
    @Transactional
    public UserInfoResponseDto updateUserInfo(String email, UserUpdateRequestDto userUpdDto) {
        UserEntity userEntity = userRepo.findByEmail( email ).orElse(null);
        if (userEntity == null) return null;

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

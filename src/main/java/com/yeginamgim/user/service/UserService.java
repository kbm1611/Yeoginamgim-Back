package com.yeginamgim.user.service;

import com.yeginamgim.global.exception.DuplicateMemberException;
import com.yeginamgim.global.exception.InvalidBirthDateException;
import com.yeginamgim.global.exception.UserNotFoundException;
import com.yeginamgim.global.file.FileService;
import com.yeginamgim.user.dto.request.UserSignupRequestDto;
import com.yeginamgim.user.dto.request.UserUpdateRequestDto;
import com.yeginamgim.user.dto.response.UserInfoResponseDto;
import com.yeginamgim.user.dto.response.UserSignupResponseDto;
import com.yeginamgim.user.entity.UserEntity;
import com.yeginamgim.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.DateTimeException;
import java.time.YearMonth;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final Pattern BIRTH_DATE_PATTERN = Pattern.compile("^\\d{6}$");

    private final UserRepository userRepo;
    private final FileService fileSvc;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public UserSignupResponseDto signup(UserSignupRequestDto userReqDto) {
        if (userRepo.findByEmail(userReqDto.getEmail()).isPresent()) {
            throw new DuplicateMemberException();
        }

        UserEntity saveEntity = userReqDto.toEntity();
        saveEntity.setBirthDate(normalizeBirthDate(userReqDto.getBirthDate()));

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

    @Transactional(readOnly = true)
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

        if (userUpdDto.getBirthDate() != null) {
            userEntity.setBirthDate(normalizeBirthDate(userUpdDto.getBirthDate()));
        }

        String fileName = fileSvc.profileUpload(userUpdDto.getProfileUploadFile());
        if (fileName != null) {
            String oldProfileImageUrl = userEntity.getProfileImageUrl();
            userEntity.setProfileImageUrl("/upload/profile/" + fileName);
            fileSvc.deleteProfileFile(oldProfileImageUrl);
        }

        return userEntity.toInfoDto();
    }

    private String normalizeBirthDate(String birthDate) {
        if (!StringUtils.hasText(birthDate)) {
            return null;
        }

        String normalizedBirthDate = birthDate.trim();
        if (!BIRTH_DATE_PATTERN.matcher(normalizedBirthDate).matches()
                || !isActualBirthDate(normalizedBirthDate)) {
            throw new InvalidBirthDateException();
        }

        return normalizedBirthDate;
    }

    private boolean isActualBirthDate(String birthDate) {
        int year = Integer.parseInt(birthDate.substring(0, 2));
        int month = Integer.parseInt(birthDate.substring(2, 4));
        int day = Integer.parseInt(birthDate.substring(4, 6));
        int fullYear = year >= 50 ? 1900 + year : 2000 + year;

        try {
            return YearMonth.of(fullYear, month).isValidDay(day);
        } catch (DateTimeException exception) {
            return false;
        }
    }
}

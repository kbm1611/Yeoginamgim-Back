package com.yeginamgim.user.service;

import com.yeginamgim.auth.service.EmailVerificationRedisService;
import com.yeginamgim.global.exception.AccountWithdrawalException;
import com.yeginamgim.global.exception.DuplicateMemberException;
import com.yeginamgim.global.exception.EmailVerificationException;
import com.yeginamgim.global.exception.InvalidBirthDateException;
import com.yeginamgim.global.exception.UserNotFoundException;
import com.yeginamgim.global.file.FileService;
import com.yeginamgim.report.repository.ReportRepository;
import com.yeginamgim.trace.repository.TraceLikeRepository;
import com.yeginamgim.user.dto.request.UserSignupRequestDto;
import com.yeginamgim.user.dto.request.UserUpdateRequestDto;
import com.yeginamgim.user.dto.request.UserWithdrawRequestDto;
import com.yeginamgim.user.dto.response.UserInfoResponseDto;
import com.yeginamgim.user.dto.response.UserSignupResponseDto;
import com.yeginamgim.user.dto.response.UserWithdrawResponseDto;
import com.yeginamgim.user.entity.UserEntity;
import com.yeginamgim.user.enums.LoginProvider;
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
    private static final String WITHDRAWAL_CONFIRMATION = "회원탈퇴";

    private final UserRepository userRepo;
    private final FileService fileSvc;
    private final TraceLikeRepository traceLikeRepository;
    private final ReportRepository reportRepository;
    private final EmailVerificationRedisService emailVerificationRedisService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 회원가입
    @Transactional
    public UserSignupResponseDto signup(UserSignupRequestDto userReqDto) {
        if (userRepo.findByEmail(userReqDto.getEmail()).isPresent()) {
            throw new DuplicateMemberException();
        }

        if (!emailVerificationRedisService.isVerified(userReqDto.getEmail())) {
            throw EmailVerificationException.required();
        }

        UserEntity saveEntity = userReqDto.toEntity();
        saveEntity.setProfileImageUrl(null);
        saveEntity.setBirthDate(normalizeBirthDate(userReqDto.getBirthDate()));

        String fileName = fileSvc.profileUpload(userReqDto.getProfileUploadFile());
        if (fileName != null) {
            saveEntity.setProfileImageUrl("/upload/profile/" + fileName);
        }

        String pwd = passwordEncoder.encode(userReqDto.getPassword());
        saveEntity.setPassword(pwd);

        try {
            UserEntity savedEntity = userRepo.save(saveEntity);
            emailVerificationRedisService.clearVerificationState(savedEntity.getEmail());
            return UserSignupResponseDto.builder()
                    .email(savedEntity.getEmail())
                    .nickname(savedEntity.getNickname())
                    .profileImageUrl(savedEntity.getProfileImageUrl())
                    .birthDate(savedEntity.getBirthDate())
                    .createdAt(savedEntity.getCreatedAt())
                    .build();
        } catch (DataIntegrityViolationException e) {
            fileSvc.deleteProfileFile(saveEntity.getProfileImageUrl());
            throw new DuplicateMemberException();
        }
    }

    // 유저 정보 확인
    @Transactional(readOnly = true)
    public UserInfoResponseDto getMyInfo(String email) {
        UserEntity userEntity = userRepo.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        return userEntity.toInfoDto();
    }

    // 유저 정보 수정
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

    // 회원 탈퇴
    @Transactional
    public UserWithdrawResponseDto withdraw(String email, UserWithdrawRequestDto request) {
        UserEntity userEntity = userRepo.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        validateWithdrawalRequest(userEntity, request);

        Long userId = userEntity.getUserId();
        traceLikeRepository.deleteByUser_UserId(userId);
        reportRepository.deleteByUser_UserId(userId);
        userEntity.withdraw();

        return UserWithdrawResponseDto.of(userEntity.getDeletedAt());
    }

    // 회원탈퇴 검증
    private void validateWithdrawalRequest(UserEntity userEntity, UserWithdrawRequestDto request) {
        if (request == null) {
            throw new AccountWithdrawalException("회원 탈퇴 확인 정보가 필요합니다.");
        }

        if (userEntity.getProvider() == LoginProvider.LOCAL) {
            validateLocalWithdrawal(userEntity, request.getPassword());
            return;
        }

        validateOAuthWithdrawal(request.getConfirmation());
    }

    // 로컬 회원
    private void validateLocalWithdrawal(UserEntity userEntity, String password) {
        if (!StringUtils.hasText(password)
                || userEntity.getPassword() == null
                || !passwordEncoder.matches(password, userEntity.getPassword())) {
            throw new AccountWithdrawalException("비밀번호가 올바르지 않습니다.");
        }
    }

    // 소셜 회원
    private void validateOAuthWithdrawal(String confirmation) {
        if (!WITHDRAWAL_CONFIRMATION.equals(confirmation == null ? null : confirmation.trim())) {
            throw new AccountWithdrawalException("회원 탈퇴 확인 문구를 입력해주세요.");
        }
    }

    // 생일 정규화
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

    // 생일이 존재하는 지 확인하는 함수
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

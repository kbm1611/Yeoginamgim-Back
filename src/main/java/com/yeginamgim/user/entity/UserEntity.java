package com.yeginamgim.user.entity;

import com.yeginamgim.global.entity.BaseTime;
import com.yeginamgim.user.dto.request.UserSignupRequestDto;
import com.yeginamgim.user.dto.response.UserInfoResponseDto;
import com.yeginamgim.user.enums.LoginProvider;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table( name = "users" )
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity extends BaseTime {
    private static final DateTimeFormatter WITHDRAWN_EMAIL_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final String WITHDRAWN_NICKNAME = "탈퇴한 사용자";

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    private Long userId;

    @Column( length = 255, nullable = false, unique = true )
    private String email;

    // 일반 로그인 회원만 사용
    @Column( length = 255 )
    private String password;

    @Column( length = 255, nullable = false )
    private String nickname;

    @Column( length = 1000 )
    private String profileImageUrl;

    @Column( name = "birth_date", length = 6 )
    private String birthDate;

    @Enumerated(EnumType.STRING)
    @Column( length = 30, nullable = false )
    private LoginProvider provider;

    @Column( length = 100 )
    private String providerId;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public UserInfoResponseDto toInfoDto(){
        return UserInfoResponseDto.builder()
                .email(email)
                .nickname(nickname)
                .profileImageUrl( profileImageUrl )
                .birthDate(birthDate)
                .provider(provider == null ? null : provider.name())
                .build();
    }

    public boolean isWithdrawn() {
        return deletedAt != null;
    }

    public void withdraw() {
        LocalDateTime withdrawnAt = LocalDateTime.now();
        this.deletedAt = withdrawnAt;
        this.email = createWithdrawnEmail(withdrawnAt);
        this.password = null;
        this.nickname = WITHDRAWN_NICKNAME;
        this.profileImageUrl = null;
        this.birthDate = null;
    }

    private String createWithdrawnEmail(LocalDateTime withdrawnAt) {
        String idPart = userId == null ? "unknown" : userId.toString();
        return "withdrawn-" + idPart + "-"
                + WITHDRAWN_EMAIL_TIME_FORMATTER.format(withdrawnAt)
                + "@deleted.yeginamgim.local";
    }
}

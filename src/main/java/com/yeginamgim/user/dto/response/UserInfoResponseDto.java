package com.yeginamgim.user.dto.response;

import com.yeginamgim.user.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
// 회원 정보 응답에 대한 Dto
public class UserInfoResponseDto {
    private String email;
    private String nickname;
    private String profileImageUrl;
    private String birthDate;
    private String provider;
}

package com.yeginamgim.user.dto.request;

import com.yeginamgim.user.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import static com.yeginamgim.user.enums.LoginProvider.LOCAL;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
// 회원가입 요청에 대한 Dto
public class UserSignupRequestDto {
    private String email;
    private String password;
    private String nickname;
    private String profileImageUrl;

    private MultipartFile profileUploadFile;

    private String createAt;

    public UserEntity toEntity(){
        return UserEntity.builder()
                .email( email )
                .password( password )
                .nickname( nickname )
                .profileImageUrl( profileImageUrl )
                .provider( LOCAL )
                .build();
    }
}

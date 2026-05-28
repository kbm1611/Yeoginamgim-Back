package com.yeginamgim.user.dto.request;

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
// 회원 정보수정 요청에 대한 Dto
public class UserUpdateRequestDto {
    private String email;
    private String nickname;
    private String profileImageUrl;

    private MultipartFile profileUploadFile;
    private String updateAt;

    public UserEntity toEntity(){
        return UserEntity.builder()
                .email( email )
                .nickname( nickname )
                .profileImageUrl( profileImageUrl )
                .build();
    }
}

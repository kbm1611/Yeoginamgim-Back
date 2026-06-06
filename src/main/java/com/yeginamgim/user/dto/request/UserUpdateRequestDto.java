package com.yeginamgim.user.dto.request;

import com.yeginamgim.user.entity.UserEntity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
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
    @Email(message = "email must be valid.")
    @Size(max = 255, message = "email must be 255 characters or less.")
    private String email;

    @Size(max = 255, message = "nickname must be 255 characters or less.")
    private String nickname;

    @Size(max = 1000, message = "profileImageUrl must be 1000 characters or less.")
    private String profileImageUrl;

    @Size(max = 6, message = "birthDate must be 6 characters or less.")
    private String birthDate;

    private MultipartFile profileUploadFile;
    private String updateAt;

    public UserEntity toEntity(){
        return UserEntity.builder()
                .email( email )
                .nickname( nickname )
                .birthDate( birthDate )
                .build();
    }
}

package com.yeginamgim.user.dto.request;

import com.yeginamgim.user.entity.UserEntity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    @NotBlank(message = "email is required.")
    @Email(message = "email must be valid.")
    @Size(max = 255, message = "email must be 255 characters or less.")
    private String email;

    @NotBlank(message = "password is required.")
    @Size(min = 8, max = 255, message = "password must be between 8 and 255 characters.")
    private String password;

    @NotBlank(message = "nickname is required.")
    @Size(max = 255, message = "nickname must be 255 characters or less.")
    private String nickname;

    @Size(max = 6, message = "birthDate must be 6 characters or less.")
    private String birthDate;

    private MultipartFile profileUploadFile;

    public UserEntity toEntity(){
        return UserEntity.builder()
                .email( email )
                .nickname( nickname )
                .birthDate( birthDate )
                .provider( LOCAL )
                .build();
    }
}

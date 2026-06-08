package com.yeginamgim.user.dto.request;

import jakarta.validation.constraints.Pattern;
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
    @Pattern(regexp = ".*\\S.*", message = "nickname must not be blank.")
    @Size(max = 255, message = "nickname must be 255 characters or less.")
    private String nickname;

    @Size(max = 6, message = "birthDate must be 6 characters or less.")
    private String birthDate;

    private MultipartFile profileUploadFile;
}

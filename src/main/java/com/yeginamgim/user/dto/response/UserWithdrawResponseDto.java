package com.yeginamgim.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserWithdrawResponseDto {
    private boolean withdrawn;
    private LocalDateTime withdrawnAt;

    public static UserWithdrawResponseDto of(LocalDateTime withdrawnAt) {
        return UserWithdrawResponseDto.builder()
                .withdrawn(true)
                .withdrawnAt(withdrawnAt)
                .build();
    }
}

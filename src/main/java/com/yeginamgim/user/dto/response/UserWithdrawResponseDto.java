package com.yeginamgim.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserWithdrawResponseDto {
    private boolean withdrawn;
    private Instant withdrawnAt;

    public static UserWithdrawResponseDto of(Instant withdrawnAt) {
        return UserWithdrawResponseDto.builder()
                .withdrawn(true)
                .withdrawnAt(withdrawnAt)
                .build();
    }
}

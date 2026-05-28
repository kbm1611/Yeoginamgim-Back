package com.yeginamgim.user.entity;

import com.yeginamgim.global.entity.BaseTime;
import com.yeginamgim.user.enums.LoginProvider;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table( name = "users" )
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity extends BaseTime {
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

    @Enumerated(EnumType.STRING)
    @Column( length = 30, nullable = false )
    private LoginProvider provider;

    @Column( length = 100 )
    private String providerId;
}

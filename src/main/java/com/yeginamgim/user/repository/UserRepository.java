package com.yeginamgim.user.repository;

import com.yeginamgim.user.entity.UserEntity;
import com.yeginamgim.user.enums.LoginProvider;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByProviderAndProviderId(LoginProvider provider, String providerId);

    @Query("""
            select user
            from UserEntity user
            where user.email = :email
            and user.deletedAt is null
            """)
    Optional<UserEntity> findByEmail(@Param("email") String email);

    @Query("""
            select user
            from UserEntity user
            where user.deletedAt is null
            and user.email <> :currentEmail
            and (
                lower(user.nickname) like lower(concat('%', :keyword, '%'))
                or (:userId is not null and user.userId = :userId)
            )
            order by user.nickname asc, user.userId asc
            """)
    List<UserEntity> searchActiveUsers(
            @Param("currentEmail") String currentEmail,
            @Param("keyword") String keyword,
            @Param("userId") Long userId,
            Pageable pageable
    );
}

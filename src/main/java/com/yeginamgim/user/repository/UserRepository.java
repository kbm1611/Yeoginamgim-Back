package com.yeginamgim.user.repository;

import com.yeginamgim.user.entity.UserEntity;
import com.yeginamgim.user.enums.LoginProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}

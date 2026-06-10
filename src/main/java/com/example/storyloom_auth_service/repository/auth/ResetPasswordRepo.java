package com.example.storyloom_auth_service.repository.auth;

import com.example.storyloom_auth_service.model.auth.PasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ResetPasswordRepo extends JpaRepository<PasswordReset,Long> {

    Optional<PasswordReset> findByEmailAndOtpAndUsedFalse(String email, String otp);
    Optional<PasswordReset> findTopByEmailOrderByGeneratedTimeDesc(String email);

    void deleteByExpiryTimeBefore(LocalDateTime expiryTime);
}

package com.example.storyloom_auth_service.repository.auth;

import com.example.storyloom_auth_service.model.auth.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpVerificationRepo extends JpaRepository<OtpVerification,Long> {

    Optional<OtpVerification> findByEmailAndOtpAndVerifiedFalse(String email, String otp);
    Optional<OtpVerification> findTopByEmailOrderByGeneratedTimeDesc(String email);

    void deleteByExpiryTimeBefore(LocalDateTime expiryTime);

}

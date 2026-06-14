package com.example.storyloom_auth_service.model.user;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "User name required")
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank(message = "Password Required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Email required")
    @Email(message = "Invalid email format")
    @Column(unique = true, nullable = false)
    private String email;

   // @NotBlank(message = "Phone number required")
    @Column(nullable = false)
    private String phone;
    private Boolean emailVerified = false;
    private String verificationOtp;
    private LocalDateTime otpGeneratedTime;


}

package com.example.storyloom_auth_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;


    @Value("${app.email.from}")
    private String adminEmail;

    public void sendOtpEmail(String toEmail, String otp){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(adminEmail);
        message.setTo(toEmail);
        message.setSubject("StoryLoom - Email Verification");
        message.setText("Your OTP for email verification is: " + otp +
                "\n\nThis OTP will expire in 5 minutes." +
                "\n\nIf you didn't request this, please ignore.");

        mailSender.send(message);

    }

    public void sendResetOtpEmail(String toEmail, String otp){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(adminEmail);
        message.setTo(toEmail);
        message.setSubject("StoryLoom - Reset Password");
        message.setText("Your OTP for Password Reset is: "+ otp +
                "\n\nThis OTP will expire in 5 minutes." +
                "\n\nIf you didn't request this, please ignore.");
        mailSender.send(message);
    }


    public void sendSetPinOtpEmail(String toEmail, String otp){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(adminEmail);
        message.setTo(toEmail);
        message.setSubject("StoryLoom - Email Verification");
        message.setText("Your OTP for Setting the pin is: " + otp +
                "\n\nThis OTP will expire in 5 minutes." +
                "\n\nIf you didn't request this, please ignore.");

        mailSender.send(message);

    }

}


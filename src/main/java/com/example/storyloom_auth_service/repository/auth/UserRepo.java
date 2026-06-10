package com.example.storyloom_auth_service.repository.auth;

import com.example.storyloom_auth_service.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {
    User findByUsername(String username);
    Optional<User> findByEmail(String email);


    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}

package com.example.nexle.repository;

import com.example.nexle.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    Token findByUserId(Long userId);
    List<Token> findByUserEmail(String userEmail);
    Token findByRefreshToken(String refreshToken);
}

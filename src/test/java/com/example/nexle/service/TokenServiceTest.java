package com.example.nexle.service;

import com.example.nexle.entity.Token;
import com.example.nexle.entity.User;
import com.example.nexle.exception.UserApiException;
import com.example.nexle.payload.SignupDto;
import com.example.nexle.payload.TokenDto;
import com.example.nexle.payload.UserDto;
import com.example.nexle.repository.TokenRepository;
import com.example.nexle.repository.UserRepository;
import com.example.nexle.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class TokenServiceTest {
    @InjectMocks
    private TokenService tokenService;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock TokenRepository tokenRepository;
    @BeforeEach
    public void setUp(){
        MockitoAnnotations.openMocks(this);
    }
    @Test
    public void testCreateToken(){
        String accessToken = "ACCESS_TOKEN";
        String refreshToken = "REFRESH_TOKEN";
        User user = new User("Nguyen Viet", "Thanh Toan", "thanhtoan.nguyenviet@gmail.com","password1234");
        when(jwtTokenProvider.generateAccessToken(any(Date.class),any(Date.class),any(String.class))).thenReturn(accessToken);
        when(jwtTokenProvider.generateRefreshToken(any(Date.class),any(Date.class),any(String.class))).thenReturn(refreshToken);
        Map<String,String> tokenResult = tokenService.createToken(user);
        assertNotNull(tokenResult);
        assertEquals(tokenResult.get("accessToken"),"ACCESS_TOKEN");
        assertEquals(tokenResult.get("refreshToken"),"REFRESH_TOKEN");
    }
    @Test
    public void testDeleteToken(){
        String authToken = "TEST";
        List<Token> tokens = new ArrayList<>();
        Token token1 = new Token();
        token1.setId(1L);
        Token token2 = new Token();
        token1.setId(2L);
        tokens.add(token1);
        tokens.add(token2);
        when( tokenRepository.findByUserEmail(any())).thenReturn(tokens);
        tokenService.deleteToken(authToken);
        verify(tokenRepository).deleteAll(tokens);
    }
    @Test
    public void testFaildRefreshToken(){
        String refreshToken = "REFRESHTOKEN";
        UserApiException notFoundToken = Assertions.assertThrows(UserApiException.class, () -> {
            tokenService.refreshToken(refreshToken);
        });
        assertEquals(HttpStatus.NOT_FOUND, notFoundToken.getStatus());

        Token token = new Token();
        token.setExpiresIn("1");
        UserApiException validRefreshToken = Assertions.assertThrows(UserApiException.class, () -> {
            when(tokenRepository.findByRefreshToken(refreshToken)).thenReturn(token);
            tokenService.refreshToken(refreshToken);
        });
        assertEquals(HttpStatus.BAD_REQUEST, validRefreshToken.getStatus());
    }
    @Test
    public void testRefreshToken(){
        String accessToken = "ACCESS_TOKEN";
        String refreshToken = "REFRESH_TOKEN";
        Token token = new Token();
        long expiresIn = LocalDateTime.now().getSecond()+259200000;
        token.setExpiresIn(expiresIn+"");
        Claims claims = Jwts.claims().setSubject("email@gmail.com");
        when(jwtTokenProvider.getClaims(refreshToken)).thenReturn(claims);
        when(tokenRepository.findByRefreshToken(refreshToken)).thenReturn(token);
        when(jwtTokenProvider.generateAccessToken(any(Date.class),any(Date.class),any(String.class))).thenReturn(accessToken);
        when(jwtTokenProvider.generateRefreshToken(any(Date.class),any(Date.class),any(String.class))).thenReturn(refreshToken);

        TokenDto tokenResult = tokenService.refreshToken(refreshToken);

        assertNotNull(tokenResult);
        assertEquals(tokenResult.getToken(),"ACCESS_TOKEN");
        assertEquals(tokenResult.getRefreshToken(),"REFRESH_TOKEN");
    }
}

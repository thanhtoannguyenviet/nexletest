package com.example.nexle.service;

import com.example.nexle.entity.Token;
import com.example.nexle.entity.User;
import com.example.nexle.exception.UserApiException;
import com.example.nexle.payload.TokenDto;
import com.example.nexle.repository.TokenRepository;
import com.example.nexle.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TokenService {
    @Autowired
    TokenRepository tokenRepository;

    @Autowired
    JwtTokenProvider jwtTokenProvider;
    @Value("${app.jwt-expiration-1hour-to-seconds}")
    private int jwtExpirationInSec;
    @Value("${app.jwt-expiration-30days-to-seconds}")
    private long jwtExpirationForRefreshTokenInSec;
    private final int SECOND_TO_MILLISECONDS = 1000;
    public Map<String, String> createToken(User user){
        String email = user.getEmail();
        Map<String, String> rs = new HashMap<>();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInSec*SECOND_TO_MILLISECONDS);
        String genAccessToken = jwtTokenProvider.generateAccessToken(now,expiryDate,email);
        String genRefreshToken = jwtTokenProvider.generateRefreshToken(now,expiryDate,email);
        Token token = new Token();
        token.setExpiresIn(expiryDate.getTime()+"");
        token.setRefreshToken(genRefreshToken);
        token.setUser(user);
        tokenRepository.save(token);
        rs.put("accessToken", genAccessToken);
        rs.put("refreshToken", genRefreshToken);
        return rs;
    }
    public void deleteToken(String authToken){
        String token = jwtTokenProvider.getJWTfromString(authToken);
        String username = jwtTokenProvider.getUsernameFromJWT(token);
        List<Token> tokens = tokenRepository.findByUserEmail(username);
        tokenRepository.deleteAll(tokens);
    }


    public TokenDto refreshToken(String refreshToken) {
        Token token = tokenRepository.findByRefreshToken(refreshToken);
        if(token==null){
            throw new UserApiException(HttpStatus.NOT_FOUND,"Token doesn't exist");
        }
        long expiredRefreshToken = Long.parseLong(token.getExpiresIn())-jwtExpirationInSec*SECOND_TO_MILLISECONDS;
        boolean isRefreshTokenValid = expiredRefreshToken - (LocalDateTime.now().getSecond()+jwtExpirationForRefreshTokenInSec)*SECOND_TO_MILLISECONDS>0;
        if(isRefreshTokenValid==false){
            throw new UserApiException(HttpStatus.BAD_REQUEST,"Token is expired");
        }
        Claims claims = jwtTokenProvider.getClaims(refreshToken);
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInSec*SECOND_TO_MILLISECONDS);
        String genAccessToken = jwtTokenProvider.generateAccessToken(now,expiryDate,refreshToken);
        String genRefreshToken = jwtTokenProvider.generateRefreshToken(now,expiryDate,claims.getSubject());
        TokenDto tokenDto = new TokenDto(genAccessToken,genRefreshToken);
        return tokenDto;

    }
}

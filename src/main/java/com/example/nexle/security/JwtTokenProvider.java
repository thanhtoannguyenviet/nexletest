package com.example.nexle.security;

import com.example.nexle.exception.UserApiException;
import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Map;

@Component
public class JwtTokenProvider {
    @Value("${app.jwt-secret}")
    private String jwtSecret;

    public String getUsernameFromJWT(String token){
        if(token.startsWith("Bearer ")){
            token = token.substring(7);
        }
        Claims claims = getClaims(token);
        return claims.getSubject();
    }
    public Claims getClaims(String token){
        return Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
    }
    public String generateAccessToken(Date now, Date expiryDate, String subject) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }
    public String generateRefreshToken(Date now, Date expiryDate,String subject) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }
    public String getJWTfromString(String requestToken){
        String bearerToken = requestToken;
        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7); //sub bearer
        }
        return null;
    }
    public boolean validateToken(String token){
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        }catch (SignatureException ex){
            throw new UserApiException(HttpStatus.BAD_REQUEST,"Invalid JWT signature");
        }catch (MalformedJwtException ex){
            throw new UserApiException(HttpStatus.BAD_REQUEST,"Invalid JWT token");
        }catch (ExpiredJwtException ex){
            throw new UserApiException(HttpStatus.BAD_REQUEST,"Invalid JWT token");
        }catch (UnsupportedJwtException ex){
            throw new UserApiException(HttpStatus.BAD_REQUEST,"Unsupported JWT token");
        }catch (IllegalArgumentException ex){
            throw new UserApiException(HttpStatus.BAD_REQUEST,"JWT claims string is empty");
        }
    }
}
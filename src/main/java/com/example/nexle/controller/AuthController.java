package com.example.nexle.controller;

import com.example.nexle.payload.*;
import com.example.nexle.service.TokenService;
import com.example.nexle.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {
    @Autowired
    private UserService userService;
    @Autowired
    private TokenService tokenService;
    @PostMapping("/sign-up")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupDto signupDto){
        if(userService.existByEmail(signupDto.getEmail())){
            return new ResponseEntity<>("Email is already taken", HttpStatus.BAD_REQUEST);
        }else {
            UserDto userDto = userService.createUser(signupDto);
            return new ResponseEntity<>(userDto,HttpStatus.CREATED);
        }
    }
    @PostMapping("/sign-in")
    public ResponseEntity<?> loginUser(@Valid @RequestBody SigninDto signinDto){
        UserResponseDto signinResponse = userService.login(signinDto);
        return new ResponseEntity<>(signinResponse, HttpStatus.OK);
    }
    @PostMapping("/sign-out")
    public ResponseEntity<String> logoutUser(HttpServletRequest request){
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
        }else tokenService.deleteToken(authorizationHeader);
        return new ResponseEntity<>("Log out completed", HttpStatus.NO_CONTENT);
    }
    @PostMapping("/refresh-token")
    public ResponseEntity<TokenDto> tokenDtoResponseEntity(@RequestBody TokenDto tokenRefresh){
        String refreshToken = tokenRefresh.getRefreshToken();
        TokenDto tokenDto = tokenService.refreshToken(refreshToken);
        return new ResponseEntity<>(tokenDto,HttpStatus.OK);
    }
}

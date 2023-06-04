package com.example.nexle.service;

import com.example.nexle.entity.User;
import com.example.nexle.exception.UserApiException;
import com.example.nexle.payload.SigninDto;
import com.example.nexle.payload.SignupDto;
import com.example.nexle.payload.UserDto;
import com.example.nexle.payload.UserResponseDto;
import com.example.nexle.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean existByEmail(String email){
        return userRepository.existsByEmail(email);
    }

    public UserDto createUser(SignupDto signupDto){
        User user = convertToUser(signupDto);
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        user = userRepository.save(user);
        return convertToUserDto(user);
    }

    public UserResponseDto login(SigninDto signinDto){
        String email = signinDto.getEmail();
        User user = userRepository.findByEmail(email);
        boolean isPasswordMatch = isPasswordMatch(signinDto.getPassword(),user.getPassword());
        if(user==null || isPasswordMatch == false){
            throw new UserApiException(HttpStatus.BAD_REQUEST, "Username and password are invalid");
        }else {
            UserDto userDto = convertToUserDto(user);
            Map<String, String> tokenResult = tokenService.createToken(user);
            String tokenStr = tokenResult.get("accessToken");
            String refreshToken = tokenResult.get("refreshToken");
            UserResponseDto signinResponse = new UserResponseDto(userDto,tokenStr,refreshToken);
            return signinResponse;
        }
    }
    public boolean isPasswordMatch(String rawPassword,String encodedPassword){
        return passwordEncoder.matches(rawPassword,encodedPassword);
    }
    public User convertToUser(SignupDto signupDto){
        User user = new User();
        user.setFirstName(signupDto.getFirstName());
        user.setLastName(signupDto.getLastName());
        user.setEmail(signupDto.getEmail());
        user.setPassword(signupDto.getPassword());
        return user;
    }
    public UserDto convertToUserDto(User user){
        String firstName = user.getFirstName();
        String lastName = user.getLastName();
        UserDto userDto = new UserDto();
        userDto.setEmail(user.getEmail());
        userDto.setFirstName(firstName);
        userDto.setLastName(lastName);
        StringBuilder sb = new StringBuilder();
        sb.append(firstName);
        sb.append(lastName);
        userDto.setFullName(sb.toString());
        return userDto;
    }
}

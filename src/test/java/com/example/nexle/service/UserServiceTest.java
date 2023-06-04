package com.example.nexle.service;

import com.example.nexle.entity.User;
import com.example.nexle.exception.UserApiException;
import com.example.nexle.payload.SigninDto;
import com.example.nexle.payload.SignupDto;
import com.example.nexle.payload.UserDto;
import com.example.nexle.payload.UserResponseDto;
import com.example.nexle.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenService tokenService;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    public void setUp(){
        MockitoAnnotations.openMocks(this);
    }
    @Test
    public void testExistByEmail(){
        String emailExist = "userexist@gmail.com";
        User user = new User();
        user.setEmail(emailExist);
        when(userRepository.existsByEmail(emailExist)).thenReturn(true);
        boolean rsExists = userService.existByEmail(emailExist);
        assertThat(rsExists).isTrue();

        String noEmailExist = "noExist@gmail.com";
        boolean rsNoExists = userService.existByEmail(noEmailExist);
        assertThat(rsNoExists).isFalse();
    }
    @Test
    public void testCreateUser(){
        String email = "user@gmail.com";

        SignupDto signupDto = new SignupDto();
        signupDto.setEmail(email);
        signupDto.setPassword("12345678");
        signupDto.setFirstName("Nguyen Viet");
        signupDto.setLastName("Thanh Toan");

        User user = new User();
        user.setEmail(email);
        when(userRepository.save(any(User.class))).thenReturn(user);
        UserDto result = userService.createUser(signupDto);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
    }
    @Test
    public void testLogin(){
        String email = "user@gmail.com";
        String password = "12345678";
        passwordEncoder = new BCryptPasswordEncoder();
        String expectedEncodedPassword = passwordEncoder.encode(password);

        User user = new User();
        user.setEmail(email);
        user.setPassword(expectedEncodedPassword);

        when(userRepository.findByEmail(email)).thenReturn(user);
        Mockito.when(userService.isPasswordMatch(password, expectedEncodedPassword)).thenReturn(true);
        SigninDto signinDto = new SigninDto();
        signinDto.setEmail(email);
        signinDto.setPassword(password);
        UserResponseDto userResponseDto = userService.login(signinDto);
        assertNotNull(userResponseDto);
        assertEquals(userResponseDto.getUser().getEmail(),user.getEmail());
    }
    @Test
    public void testLoginFailed(){
        String email = "user@gmail.com";
        String password = "12345678";
        passwordEncoder = new BCryptPasswordEncoder();
        String expectedEncodedPassword = passwordEncoder.encode(password);

        User user = new User();
        user.setEmail(email);
        user.setPassword(expectedEncodedPassword);
        SigninDto signinDto = new SigninDto();
        signinDto.setEmail(email);
        signinDto.setPassword(password);
        when(userRepository.findByEmail(email)).thenReturn(user);
        Mockito.when(userService.isPasswordMatch(password, expectedEncodedPassword)).thenReturn(false);
        UserApiException exception = Assertions.assertThrows(UserApiException.class, () -> {
            userService.login(signinDto);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());

    }
}

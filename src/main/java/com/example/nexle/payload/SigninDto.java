package com.example.nexle.payload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class SigninDto {
    @Email(message = "Email is invalidate ")
    private String email;
    @Size(min = 8, max = 20, message = "Password must be between 8-20 characters")
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
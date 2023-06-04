package com.example.nexle.security;

import com.example.nexle.entity.User;
import com.example.nexle.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService {
    @Autowired
    private UserRepository userRepository;

    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        Set<String> ROLE = new HashSet<>();
        ROLE.add("USER");
        User user =userRepository.findByEmail(usernameOrEmail);
        if (user==null){
            throw new UsernameNotFoundException("User not found with username or email:" + usernameOrEmail);
        }
        return new org.springframework.security.core.userdetails.User(user.getEmail(),user.getPassword(),mapRoleToAuthorities(ROLE));
    }
    private Collection<? extends GrantedAuthority> mapRoleToAuthorities(Set<String> roles){
        return roles.stream().map(role -> new SimpleGrantedAuthority(role)).collect(Collectors.toList());
    }
}

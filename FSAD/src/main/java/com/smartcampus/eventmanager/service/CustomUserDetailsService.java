package com.smartcampus.eventmanager.service;

import com.smartcampus.eventmanager.model.AppUser;
import com.smartcampus.eventmanager.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<AppUser> userOpt = userRepository.findByEmail(email);

        AppUser appUser;
        if (userOpt.isPresent()) {
            appUser = userOpt.get();
            // Ensure the main account is always an ADMIN
            if (email.equalsIgnoreCase("avinashyadavbayanaboina@gmail.com") && !"ADMIN".equals(appUser.getRole())) {
                appUser.setRole("ADMIN");
                userRepository.save(appUser);
            }
        } else {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        return User.withUsername(appUser.getEmail())
                .password(appUser.getPassword())
                .roles(appUser.getRole())
                .build();
    }
}

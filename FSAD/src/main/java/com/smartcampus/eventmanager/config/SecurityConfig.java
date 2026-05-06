package com.smartcampus.eventmanager.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import com.smartcampus.eventmanager.service.EmailService;

import java.io.IOException;
import java.util.Random;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private EmailService emailService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/verify-otp", "/signup", "/admin-login", "/css/**", "/images/**", "/js/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(login -> login
                .loginPage("/login")
                .successHandler(new AuthenticationSuccessHandler() {
                    @Override
                    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                        boolean isAdmin = authentication.getAuthorities().stream()
                                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

                        if (isAdmin) {
                            // Generate OTP for admin
                            String otp = String.format("%06d", new Random().nextInt(999999));
                            String email = authentication.getName();

                            // Store OTP and email in session
                            request.getSession().setAttribute("OTP", otp);
                            request.getSession().setAttribute("PRE_AUTH_USER", email);

                            // Send OTP email
                            emailService.sendOtpEmail(email, otp);

                            // Clear the security context so they aren't fully logged in yet
                            SecurityContextHolder.clearContext();

                            // Redirect to OTP verification page
                            response.sendRedirect("/verify-otp");
                        } else {
                            // Regular users bypass OTP and go straight to the dashboard (events)
                            response.sendRedirect("/events");
                        }
                    }
                })
                .permitAll()
            )
            .logout(logout -> logout.logoutSuccessUrl("/login?logout"))
            .csrf(csrf -> csrf.disable()); 
        
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

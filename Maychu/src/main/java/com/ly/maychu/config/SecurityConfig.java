package com.ly.maychu.config;

import com.ly.maychu.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private AuthService authService;
@Autowired // Inject Bean từ PasswordEncoderConfig vào đây
private PasswordEncoder passwordEncoder;
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http

                .authorizeHttpRequests(auth -> auth
                        // Cho phép truy cập tự do
                        .requestMatchers("/login", "/api/auth/student", "/ws-monitor/**").permitAll()
                        // Chỉ QUAN_TRI mới vào được trang admin
                        .requestMatchers("/admin/**").hasRole("QUAN_TRI")
                        // Dashboard chỉ GV và Admin
                        .requestMatchers("/dashboard/**").hasAnyRole("GIANG_VIEN", "QUAN_TRI")
                        // Các API lệnh chỉ GV và Admin
                        .requestMatchers("/api/admin/**").hasRole("QUAN_TRI")
                        .requestMatchers("/api/**").hasAnyRole("GIANG_VIEN", "QUAN_TRI")
                        .requestMatchers("/api/**").hasAnyRole("GIANG_VIEN", "QUAN_TRI")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")           // Trang login tự custom
                        .defaultSuccessUrl("/dashboard", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll()
                )

                // Tắt CSRF cho API (MayTram gọi HTTP POST)
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**", "/ws-monitor/**")
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        // Dùng biến đã được Inject thay vì gọi "new"
        builder.userDetailsService(authService).passwordEncoder(passwordEncoder);
        return builder.build();
    }
}
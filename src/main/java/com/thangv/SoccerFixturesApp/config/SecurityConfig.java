package com.thangv.SoccerFixturesApp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // disable for simple POST/PUT testing; re-enable later
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/**", "/leagues/**").permitAll()
                        .anyRequest().permitAll()
                )
                .headers(h -> h.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)) // H2 console frames
                .httpBasic(Customizer.withDefaults()); // or remove if you don't want any auth at all

        return http.build();
    }
}

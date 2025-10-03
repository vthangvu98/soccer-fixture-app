package com.thangv.SoccerFixturesApp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    @Profile("dev")
    SecurityFilterChain devSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/**",
                                "/leagues/**",
                                "/teams/**",
                                "/fixtures/**",
                                "/users/**",
                                "/rapidapi/**",
                                "/actuator/health"
                        ).permitAll()
                        .anyRequest().permitAll()
                )
                .headers(h -> h.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    @Profile("prod")
    SecurityFilterChain prodSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**")
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/actuator/health",
                                "/leagues",
                                "/teams",
                                "/fixtures/all"
                        ).permitAll()

                        .requestMatchers(
                                "/leagues/import/**",
                                "/teams/importByLeagueId/**",
                                "/fixtures/importByDate",
                                "/api/notifications/**",
                                "/api/subscriptions/**"
                        ).authenticated()  // TODO: Replace with .hasRole("ADMIN") when roles added

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());  // TODO: Replace with JWT

        return http.build();
    }
}
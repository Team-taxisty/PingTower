package taxisty.pingtower.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for PingTower backend.
 * Configures which endpoints require authentication and which are publicly accessible.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                // Public endpoints - no authentication required
                .requestMatchers("/api/health", "/api/info").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                // Swagger/OpenAPI endpoints - support context path /v1
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/swagger-ui/index.html").permitAll()
                .requestMatchers("/v3/api-docs/**", "/v3/api-docs").permitAll()
                .requestMatchers("/swagger-resources/**").permitAll()
                .requestMatchers("/webjars/**").permitAll()
                .requestMatchers("/favicon.ico").permitAll()
                // Additional SpringDoc OpenAPI endpoints
                .requestMatchers("/swagger-config").permitAll()
                .requestMatchers("/swagger-ui-init.js").permitAll()
                .requestMatchers("/swagger-ui-bundle.js").permitAll()
                .requestMatchers("/swagger-ui-standalone-preset.js").permitAll()
                .requestMatchers("/swagger-ui.css").permitAll()
                // Additional OpenAPI endpoints
                .requestMatchers("/configuration/ui", "/configuration/security").permitAll()
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            // Enable HTTP Basic authentication for other endpoints
            .httpBasic(httpBasic -> {})
            // Disable CSRF for API endpoints (typical for REST APIs)
            .csrf(csrf -> csrf.disable());
            
        return http.build();
    }
}
package tohear.hearo.admin.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;
import tohear.hearo.admin.auth.AdminJwtAuthenticationFilter;
import tohear.hearo.global.response.Result;

@Configuration
@RequiredArgsConstructor
public class AdminSecurityConfig {

    private final AdminJwtAuthenticationFilter adminJwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    @Bean
    SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .logout(logout -> logout.disable())
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/admin/login", "/actuator/health", "/actuator/health/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().denyAll())
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(this::writeUnauthorized)
                .accessDeniedHandler((request, response, exception) ->
                    writeError(response, HttpServletResponse.SC_FORBIDDEN, "관리자 권한이 필요합니다.")))
            .addFilterBefore(adminJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins:https://admin.hearo-hearo.com}") String allowedOrigins) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
            .map(String::trim)
            .filter(origin -> !origin.isEmpty())
            .toList());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Client-Request-ID"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/admin/**", configuration);
        return source;
    }

    private void writeUnauthorized(
            HttpServletRequest request,
            HttpServletResponse response,
            org.springframework.security.core.AuthenticationException exception) throws IOException {
        Object error = request.getAttribute(AdminJwtAuthenticationFilter.AUTHENTICATION_ERROR_ATTRIBUTE);
        String message = error instanceof String value
            ? value
            : "관리자 인증 토큰이 필요합니다.";
        writeError(response, HttpServletResponse.SC_UNAUTHORIZED, message);
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), new Result<>(String.valueOf(status), message, null));
    }
}

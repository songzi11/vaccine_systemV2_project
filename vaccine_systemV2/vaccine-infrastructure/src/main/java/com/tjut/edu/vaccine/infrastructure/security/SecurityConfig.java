package com.tjut.edu.vaccine.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 公开接口
                .requestMatchers("/api/v1/public/**").permitAll()
                // Swagger / Knife4j
                .requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/doc.html",
                    "/webjars/**",
                    "/favicon.ico"
                ).permitAll()
                // 业务主管只开放业务管理面：排班、窗口、疫苗、公告、统计；超级管理员保留全部后台能力
                .requestMatchers("/api/v1/schedule", "/api/v1/schedule/**")
                    .hasAnyRole("SUPER_ADMIN", "DOCTOR_BUSINESS_ADMIN")
                .requestMatchers("/api/v1/admin/windows", "/api/v1/admin/windows/**")
                    .hasAnyRole("SUPER_ADMIN", "DOCTOR_BUSINESS_ADMIN")
                .requestMatchers("/api/v1/admin/vaccines", "/api/v1/admin/vaccines/**")
                    .hasAnyRole("SUPER_ADMIN", "DOCTOR_BUSINESS_ADMIN")
                .requestMatchers("/api/v1/admin/notices", "/api/v1/admin/notices/**")
                    .hasAnyRole("SUPER_ADMIN", "DOCTOR_BUSINESS_ADMIN")
                .requestMatchers("/api/v1/admin/stats", "/api/v1/admin/stats/**")
                    .hasAnyRole("SUPER_ADMIN", "DOCTOR_BUSINESS_ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/admin/users")
                    .hasAnyRole("SUPER_ADMIN", "DOCTOR_BUSINESS_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/admin/users/*/assign-window")
                    .hasAnyRole("SUPER_ADMIN", "DOCTOR_BUSINESS_ADMIN")
                .requestMatchers("/api/v1/admin/**").hasRole("SUPER_ADMIN")
                // 其余接口需要认证
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":1001,\"message\":\"未认证或Token已失效\",\"data\":null}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":1003,\"message\":\"无权限访问该资源\",\"data\":null}");
                })
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("http://localhost:*", "https://localhost:*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager();
    }
}

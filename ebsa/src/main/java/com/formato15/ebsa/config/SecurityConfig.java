package com.formato15.ebsa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.formato15.ebsa.security.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()) // Deshabilita CSRF
            .cors() // Habilita CORS
            .and()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/**").permitAll() // Permite las rutas de API sin autenticación
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("http://formato15.ebsa.com.co:8080"); // Frontend en Apache
        configuration.addAllowedOrigin("http://localhost:8080"); // Para pruebas locales
        configuration.addAllowedOrigin("http://formato15.ebsa.com.co:8086"); // Backend en desarrollo
        configuration.addAllowedHeader("*"); // Permitir todos los encabezados
        configuration.addAllowedMethod("*"); // Permitir todos los métodos HTTP
        configuration.setAllowCredentials(true); // Permitir credenciales (como cookies o encabezados Authorization)
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // @Bean
    // public CorsFilter corsFilter() {
    //     UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    //     CorsConfiguration config = new CorsConfiguration();
    //     config.addAllowedOrigin("http://formato15.ebsa.com.co:8080"); 
    //     config.addAllowedOrigin("http://formato15.ebsa.com.co:8086"); 
    //     config.addAllowedOrigin("http://formato15.ebsa.com.co"); 
    //     config.addAllowedOrigin("https://formato15.ebsa.com.co:8082"); 
    //     config.addAllowedOrigin("http://localhost:8080"); 
    //     config.addAllowedOrigin("http://localhost:8086"); 
    //     config.addAllowedOriginPattern("http://*.ebsa.com.co");
    //     config.addAllowedOriginPattern("http://localhost:*");
    //     config.addAllowedHeader("*"); 
    //     config.addAllowedMethod("*"); 
    //     config.setAllowCredentials(true); 
    //     source.registerCorsConfiguration("/**", config);
    //     return new CorsFilter(source);
    // }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) 
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

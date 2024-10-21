package com.formato15.ebsa.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Esto permite CORS en todas las rutas.
                .allowedOrigins("http://localhost:8080") // Permitimos solo el origen del frontend.
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Métodos HTTP permitidos.
                .allowedHeaders("*") // Todos los encabezados permitidos.
                .allowCredentials(true); // Si necesitas enviar cookies o credenciales.
    }
}
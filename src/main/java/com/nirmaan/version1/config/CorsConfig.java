package com.nirmaan.version1.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Global CORS Configuration for the Spring Boot application.
 * This ensures that the Angular frontend (running on a different port/origin)
 * can successfully access the REST API endpoints.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/v1/**")
                .allowedOrigins("*") // Allow all origins for simplicity in development
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH") // Specify allowed methods
                .allowedHeaders("*") // Allow all headers
                .allowCredentials(false); // No credentials needed for this public API
    }
}
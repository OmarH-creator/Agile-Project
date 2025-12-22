package com.university.backend.services;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Allow ALL endpoints
                        .allowedOrigins("http://localhost:3000") // Allow Frontend
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Allow all actions
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}
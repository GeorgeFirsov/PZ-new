package ru.mtuci.coursemanagement.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedMethods("*")
                .allowedOrigins("*")
                .allowedHeaders("*");
    }

    @Bean
    public org.springframework.boot.web.servlet.FilterRegistrationBean<OncePerRequestFilter> securityHeadersFilter() {
        OncePerRequestFilter filter = new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                    throws ServletException, IOException {

                // Clickjacking
                response.setHeader("X-Frame-Options", "DENY");

                // CSP
                response.setHeader("Content-Security-Policy",
                        "default-src 'self'; " +
                        "object-src 'none'; " +
                        "base-uri 'self'; " +
                        "frame-ancestors 'none'; " +
                        "form-action 'self'");

                // MIME sniffing
                response.setHeader("X-Content-Type-Options", "nosniff");

                // Permissions-Policy
                response.setHeader("Permissions-Policy",
                        "geolocation=(), microphone=(), camera=(), payment=(), usb=(), " +
                        "magnetometer=(), gyroscope=(), accelerometer=(), fullscreen=()");

                // Защита от Spectre атак
                response.setHeader("Cross-Origin-Opener-Policy", "same-origin");
                response.setHeader("Cross-Origin-Embedder-Policy", "require-corp");
                response.setHeader("Cross-Origin-Resource-Policy", "same-origin");

                // Запрещаем кэширование
                response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
                response.setHeader("Pragma", "no-cache");
                response.setHeader("Expires", "0");

                chain.doFilter(request, response);
            }
        };

        org.springframework.boot.web.servlet.FilterRegistrationBean<OncePerRequestFilter> reg =
                new org.springframework.boot.web.servlet.FilterRegistrationBean<>(filter);
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return reg;
    }
}

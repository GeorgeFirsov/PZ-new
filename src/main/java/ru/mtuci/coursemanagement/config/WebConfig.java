package ru.mtuci.coursemanagement.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedOrigins("http://localhost:8080")
                .allowedHeaders("*");
    }

    @Bean
    public Filter securityHeadersFilter() {
        return new Filter() {
            @Override
            public void doFilter(ServletRequest request,
                                 ServletResponse response,
                                 FilterChain chain) throws IOException, ServletException {
                if (response instanceof HttpServletResponse) {
                    HttpServletResponse httpResp = (HttpServletResponse) response;

// Защита от clickjacking
                    httpResp.setHeader("X-Frame-Options", "DENY");
// Базовая CSP
                    httpResp.setHeader("Content-Security-Policy", "default-src 'self'");
// Запрет MIME-sniffing
                    httpResp.setHeader("X-Content-Type-Options", "nosniff");
// Ограничение возможностей браузера
                    httpResp.setHeader("Permissions-Policy","geolocation=(), microphone=(), camera=()");
                    httpResp.setHeader("Cross-Origin-Opener-Policy", "same-origin");
                    httpResp.setHeader("Cross-Origin-Embedder-Policy", "require-corp");
                    httpResp.setHeader("Cache-Control","no-store, no-cache, must-revalidate, max-age=0");
                    httpResp.setHeader("Pragma", "no-cache");
                    httpResp.setDateHeader("Expires", 0);
                }

                chain.doFilter(request, response);
            }
        };
    }
}

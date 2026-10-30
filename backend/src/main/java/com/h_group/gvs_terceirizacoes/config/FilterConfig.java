package com.h_group.gvs_terceirizacoes.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class FilterConfig {

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtService jwtService,
            ObjectMapper objectMapper
    ) {
        return new JwtAuthenticationFilter(jwtService, objectMapper);
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration =
                new FilterRegistrationBean<>(jwtAuthenticationFilter);

        registration.addUrlPatterns(
                "/api/clientes", "/api/clientes/*",
                "/api/prestadores", "/api/prestadores/*",
                "/api/contratos", "/api/contratos/*",
                "/api/ordens-servico", "/api/ordens-servico/*",
                "/api/historico", "/api/historico/*"
        );
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);

        return registration;
    }
}

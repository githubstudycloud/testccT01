package com.platform.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 认证全局过滤器
 */
@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private static final List<String> SKIP_AUTH_URLS = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh",
            "/actuator/**",
            "/config/**"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 跳过不需要认证的URL
        if (shouldSkipAuth(path)) {
            return chain.filter(exchange);
        }

        // 检查Authorization header
        String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (token == null || !token.startsWith("Bearer ")) {
            log.warn("访问路径 {} 缺少认证token", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // TODO: 验证JWT token
        // 这里可以添加JWT token验证逻辑

        return chain.filter(exchange);
    }

    private boolean shouldSkipAuth(String path) {
        return SKIP_AUTH_URLS.stream().anyMatch(pattern ->
            path.matches(pattern.replace("**", ".*")));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
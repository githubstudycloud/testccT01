package com.platform.http.client;

import com.platform.shared.web.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * HTTP客户端服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HttpClientService {

    private final RestTemplate restTemplate;
    private final WebClient webClient;

    /**
     * 同步GET请求
     */
    public <T> Result<T> get(String url, Class<T> responseType) {
        return get(url, null, responseType);
    }

    /**
     * 同步GET请求带请求头
     */
    public <T> Result<T> get(String url, HttpHeaders headers, Class<T> responseType) {
        try {
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.GET, entity, responseType);

            log.info("GET请求成功: {} -> {}", url, response.getStatusCode());
            return Result.success(response.getBody());
        } catch (Exception e) {
            log.error("GET请求失败: {}", url, e);
            return Result.error("HTTP_REQUEST_ERROR", e.getMessage());
        }
    }

    /**
     * 同步POST请求
     */
    public <T, R> Result<R> post(String url, T requestBody, Class<R> responseType) {
        return post(url, requestBody, null, responseType);
    }

    /**
     * 同步POST请求带请求头
     */
    public <T, R> Result<R> post(String url, T requestBody, HttpHeaders headers, Class<R> responseType) {
        try {
            HttpEntity<T> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<R> response = restTemplate.exchange(url, HttpMethod.POST, entity, responseType);

            log.info("POST请求成功: {} -> {}", url, response.getStatusCode());
            return Result.success(response.getBody());
        } catch (Exception e) {
            log.error("POST请求失败: {}", url, e);
            return Result.error("HTTP_REQUEST_ERROR", e.getMessage());
        }
    }

    /**
     * 异步GET请求
     */
    public <T> Mono<Result<T>> getAsync(String url, Class<T> responseType) {
        return webClient.get()
            .uri(url)
            .retrieve()
            .bodyToMono(responseType)
            .map(Result::success)
            .doOnSuccess(result -> log.info("异步GET请求成功: {}", url))
            .onErrorResume(error -> {
                log.error("异步GET请求失败: {}", url, error);
                return Mono.just(Result.error("HTTP_REQUEST_ERROR", error.getMessage()));
            });
    }

    /**
     * 异步POST请求
     */
    public <T, R> Mono<Result<R>> postAsync(String url, T requestBody, Class<R> responseType) {
        return webClient.post()
            .uri(url)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(responseType)
            .map(Result::success)
            .doOnSuccess(result -> log.info("异步POST请求成功: {}", url))
            .onErrorResume(error -> {
                log.error("异步POST请求失败: {}", url, error);
                return Mono.just(Result.error("HTTP_REQUEST_ERROR", error.getMessage()));
            });
    }

    /**
     * 通用HTTP请求
     */
    public <T, R> Result<R> request(String url, HttpMethod method, T requestBody,
                                   HttpHeaders headers, Class<R> responseType) {
        try {
            HttpEntity<T> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<R> response = restTemplate.exchange(url, method, entity, responseType);

            log.info("{}请求成功: {} -> {}", method, url, response.getStatusCode());
            return Result.success(response.getBody());
        } catch (Exception e) {
            log.error("{}请求失败: {}", method, url, e);
            return Result.error("HTTP_REQUEST_ERROR", e.getMessage());
        }
    }

    /**
     * 带URL参数的GET请求
     */
    public <T> Result<T> getWithParams(String url, Map<String, Object> params, Class<T> responseType) {
        try {
            ResponseEntity<T> response = restTemplate.getForEntity(url, responseType, params);

            log.info("GET请求成功: {} -> {}", url, response.getStatusCode());
            return Result.success(response.getBody());
        } catch (Exception e) {
            log.error("GET请求失败: {}", url, e);
            return Result.error("HTTP_REQUEST_ERROR", e.getMessage());
        }
    }
}
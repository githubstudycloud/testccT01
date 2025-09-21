package com.platform.http.controller;

import com.platform.http.client.HttpClientService;
import com.platform.shared.web.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * HTTP工具控制器
 */
@RestController
@RequestMapping("/api/http")
@RequiredArgsConstructor
public class HttpController {

    private final HttpClientService httpClientService;

    /**
     * GET请求代理
     */
    @GetMapping("/get")
    public Result<String> get(@RequestParam String url,
                             @RequestParam(required = false) Map<String, String> headers) {
        HttpHeaders httpHeaders = new HttpHeaders();
        if (headers != null) {
            headers.forEach(httpHeaders::add);
        }
        return httpClientService.get(url, httpHeaders, String.class);
    }

    /**
     * POST请求代理
     */
    @PostMapping("/post")
    public Result<String> post(@RequestParam String url,
                              @RequestBody(required = false) Object body,
                              @RequestParam(required = false) Map<String, String> headers) {
        HttpHeaders httpHeaders = new HttpHeaders();
        if (headers != null) {
            headers.forEach(httpHeaders::add);
        }
        return httpClientService.post(url, body, httpHeaders, String.class);
    }

    /**
     * 异步GET请求
     */
    @GetMapping("/async/get")
    public Mono<Result<String>> getAsync(@RequestParam String url) {
        return httpClientService.getAsync(url, String.class);
    }

    /**
     * 异步POST请求
     */
    @PostMapping("/async/post")
    public Mono<Result<String>> postAsync(@RequestParam String url,
                                         @RequestBody(required = false) Object body) {
        return httpClientService.postAsync(url, body, String.class);
    }

    /**
     * 通用HTTP请求
     */
    @RequestMapping("/request")
    public Result<String> request(@RequestParam String url,
                                 @RequestParam HttpMethod method,
                                 @RequestBody(required = false) Object body,
                                 @RequestParam(required = false) Map<String, String> headers) {
        HttpHeaders httpHeaders = new HttpHeaders();
        if (headers != null) {
            headers.forEach(httpHeaders::add);
        }
        return httpClientService.request(url, method, body, httpHeaders, String.class);
    }
}
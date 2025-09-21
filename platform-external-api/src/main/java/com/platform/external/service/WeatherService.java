package com.platform.external.service;

import com.platform.shared.web.Result;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 天气服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

    private final RestTemplate restTemplate;

    @Value("${external.weather.api.key:demo-key}")
    private String apiKey;

    @Value("${external.weather.api.url:https://api.openweathermap.org/data/2.5/weather}")
    private String weatherApiUrl;

    /**
     * 获取天气信息
     */
    @CircuitBreaker(name = "weather-service", fallbackMethod = "getWeatherFallback")
    @Retry(name = "weather-service")
    @Cacheable(value = "weather", key = "#city")
    public Result<Map<String, Object>> getWeather(String city) {
        try {
            String url = weatherApiUrl + "?q={city}&appid={apiKey}&units=metric";
            Map<String, String> params = new HashMap<>();
            params.put("city", city);
            params.put("apiKey", apiKey);

            Map<String, Object> response = restTemplate.getForObject(url, Map.class, params);
            log.info("获取天气信息成功: {}", city);
            return Result.success(response);
        } catch (Exception e) {
            log.error("获取天气信息失败: {}", city, e);
            return Result.error("WEATHER_API_ERROR", "获取天气信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取天气信息降级方法
     */
    public Result<Map<String, Object>> getWeatherFallback(String city, Exception ex) {
        log.warn("天气服务降级处理: {}", city, ex);
        Map<String, Object> fallbackData = new HashMap<>();
        fallbackData.put("city", city);
        fallbackData.put("message", "天气服务暂时不可用，请稍后重试");
        fallbackData.put("temperature", "N/A");
        return Result.success(fallbackData);
    }
}
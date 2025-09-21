package com.platform.external.controller;

import com.platform.external.service.SmsService;
import com.platform.external.service.WeatherService;
import com.platform.shared.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 外部API控制器
 */
@Tag(name = "外部API", description = "第三方服务集成接口")
@RestController
@RequestMapping("/api/external")
@RequiredArgsConstructor
public class ExternalController {

    private final WeatherService weatherService;
    private final SmsService smsService;

    /**
     * 获取天气信息
     */
    @Operation(summary = "获取天气信息", description = "根据城市名称获取当前天气信息")
    @GetMapping("/weather")
    public Result<Map<String, Object>> getWeather(@RequestParam String city) {
        return weatherService.getWeather(city);
    }

    /**
     * 发送短信
     */
    @Operation(summary = "发送短信", description = "向指定手机号发送短信")
    @PostMapping("/sms/send")
    public Result<String> sendSms(@RequestParam String phone, @RequestParam String message) {
        return smsService.sendSms(phone, message);
    }

    /**
     * 发送验证码
     */
    @Operation(summary = "发送验证码", description = "向指定手机号发送验证码")
    @PostMapping("/sms/verification-code")
    public Result<String> sendVerificationCode(@RequestParam String phone) {
        return smsService.sendVerificationCode(phone);
    }
}
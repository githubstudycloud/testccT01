package com.platform.external.service;

import com.platform.shared.web.Result;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 短信服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {

    @Value("${external.sms.provider:mock}")
    private String smsProvider;

    @Value("${external.sms.api.key:demo-key}")
    private String apiKey;

    /**
     * 发送短信
     */
    @CircuitBreaker(name = "sms-service", fallbackMethod = "sendSmsFallback")
    public Result<String> sendSms(String phone, String message) {
        try {
            // 模拟发送短信
            if ("mock".equals(smsProvider)) {
                log.info("模拟发送短信 - 手机号: {}, 内容: {}", phone, message);
                return Result.success("短信发送成功 (模拟)");
            }

            // TODO: 实际短信服务提供商集成
            // 这里可以集成阿里云、腾讯云等短信服务

            log.info("短信发送成功: {}", phone);
            return Result.success("短信发送成功");
        } catch (Exception e) {
            log.error("短信发送失败: {}", phone, e);
            return Result.error("SMS_SEND_ERROR", "短信发送失败: " + e.getMessage());
        }
    }

    /**
     * 发送验证码
     */
    public Result<String> sendVerificationCode(String phone) {
        String code = generateVerificationCode();
        String message = "您的验证码是: " + code + "，5分钟内有效";
        return sendSms(phone, message);
    }

    /**
     * 短信发送降级方法
     */
    public Result<String> sendSmsFallback(String phone, String message, Exception ex) {
        log.warn("短信服务降级处理: {}", phone, ex);
        // 可以考虑使用备用短信通道或者记录到队列中稍后重试
        return Result.error("SMS_SERVICE_UNAVAILABLE", "短信服务暂时不可用，请稍后重试");
    }

    /**
     * 生成验证码
     */
    private String generateVerificationCode() {
        return String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
    }
}
package com.platform.user.application.command;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

/**
 * 创建用户命令
 */
@Data
public class CreateUserCommand {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50字符之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6-100字符之间")
    private String password;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Size(max = 50, message = "昵称长度不能超过50字符")
    private String nickname;

    public CreateUserCommand() {}

    public CreateUserCommand(String username, String password, String email, String phone, String nickname) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.nickname = nickname;
    }
}
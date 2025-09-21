package com.platform.user.application.command;

import lombok.Data;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

/**
 * 更新用户命令
 */
@Data
public class UpdateUserCommand {

    @Email(message = "邮箱格式不正确")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Size(max = 50, message = "昵称长度不能超过50字符")
    private String nickname;

    @Size(max = 200, message = "头像URL长度不能超过200字符")
    private String avatar;

    public UpdateUserCommand() {}

    public UpdateUserCommand(String email, String phone, String nickname, String avatar) {
        this.email = email;
        this.phone = phone;
        this.nickname = nickname;
        this.avatar = avatar;
    }
}
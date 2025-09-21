package com.platform.user.interfaces.rest;

import com.platform.shared.web.Result;
import com.platform.user.application.command.CreateUserCommand;
import com.platform.user.application.command.UpdateUserCommand;
import com.platform.user.application.service.UserApplicationService;
import com.platform.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 用户控制器
 */
@Tag(name = "用户管理", description = "用户相关接口")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserApplicationService userApplicationService;

    /**
     * 创建用户
     */
    @PostMapping
    @Operation(summary = "创建用户", description = "创建新用户")
    public Result<User> createUser(@Valid @RequestBody CreateUserCommand command) {
        return userApplicationService.createUser(command);
    }

    /**
     * 根据ID查询用户
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询用户", description = "根据用户ID查询用户信息")
    public Result<User> getUserById(@PathVariable Long id) {
        return userApplicationService.getUserById(id);
    }

    /**
     * 根据用户名查询用户
     */
    @GetMapping("/username/{username}")
    @Operation(summary = "根据用户名查询用户", description = "根据用户名查询用户信息")
    public Result<User> getUserByUsername(@PathVariable String username) {
        return userApplicationService.getUserByUsername(username);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新用户信息", description = "更新指定用户的信息")
    public Result<User> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserCommand command) {
        return userApplicationService.updateUser(id, command);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户", description = "删除指定用户")
    public Result<Void> deleteUser(@PathVariable Long id) {
        return userApplicationService.deleteUser(id);
    }

    /**
     * 激活用户
     */
    @PostMapping("/{id}/activate")
    @Operation(summary = "激活用户", description = "激活指定用户")
    public Result<Void> activateUser(@PathVariable Long id) {
        return userApplicationService.activateUser(id);
    }

    /**
     * 禁用用户
     */
    @PostMapping("/{id}/disable")
    @Operation(summary = "禁用用户", description = "禁用指定用户")
    public Result<Void> disableUser(@PathVariable Long id) {
        return userApplicationService.disableUser(id);
    }

    /**
     * 查询激活用户列表
     */
    @GetMapping("/active")
    @Operation(summary = "查询激活用户列表", description = "查询所有激活状态的用户")
    public Result<List<User>> getActiveUsers() {
        return userApplicationService.getActiveUsers();
    }
}
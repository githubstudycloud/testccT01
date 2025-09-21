package com.platform.user.application.service;

import com.platform.shared.exception.BusinessException;
import com.platform.shared.exception.ResourceNotFoundException;
import com.platform.shared.web.PageQuery;
import com.platform.shared.web.PageResult;
import com.platform.shared.web.Result;
import com.platform.user.application.command.CreateUserCommand;
import com.platform.user.application.command.UpdateUserCommand;
import com.platform.user.domain.User;
import com.platform.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户应用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserApplicationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 创建用户
     */
    @Transactional
    public Result<User> createUser(CreateUserCommand command) {
        try {
            // 检查用户名是否已存在
            if (userRepository.existsByUsername(command.getUsername())) {
                return Result.error("USER_USERNAME_EXISTS", "用户名已存在");
            }

            // 检查邮箱是否已存在
            if (command.getEmail() != null && userRepository.existsByEmail(command.getEmail())) {
                return Result.error("USER_EMAIL_EXISTS", "邮箱已存在");
            }

            // 检查手机号是否已存在
            if (command.getPhone() != null && userRepository.existsByPhone(command.getPhone())) {
                return Result.error("USER_PHONE_EXISTS", "手机号已存在");
            }

            // 加密密码
            String encodedPassword = passwordEncoder.encode(command.getPassword());

            // 创建用户
            User user = User.create(
                command.getUsername(),
                encodedPassword,
                command.getEmail(),
                command.getPhone(),
                command.getNickname()
            );

            // 保存用户
            User savedUser = userRepository.save(user);

            log.info("用户创建成功: userId={}, username={}", savedUser.getId(), savedUser.getUsername());
            return Result.success(savedUser);

        } catch (Exception e) {
            log.error("用户创建失败: username={}", command.getUsername(), e);
            return Result.error("USER_CREATE_ERROR", "用户创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户信息
     */
    @Transactional
    public Result<User> updateUser(Long userId, UpdateUserCommand command) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

            // 检查邮箱唯一性
            if (command.getEmail() != null && !command.getEmail().equals(user.getEmail())) {
                if (userRepository.existsByEmail(command.getEmail())) {
                    return Result.error("USER_EMAIL_EXISTS", "邮箱已存在");
                }
            }

            // 检查手机号唯一性
            if (command.getPhone() != null && !command.getPhone().equals(user.getPhone())) {
                if (userRepository.existsByPhone(command.getPhone())) {
                    return Result.error("USER_PHONE_EXISTS", "手机号已存在");
                }
            }

            // 更新用户信息
            user.updateProfile(
                command.getEmail(),
                command.getPhone(),
                command.getNickname(),
                command.getAvatar()
            );

            User savedUser = userRepository.save(user);

            log.info("用户信息更新成功: userId={}", userId);
            return Result.success(savedUser);

        } catch (ResourceNotFoundException e) {
            return Result.error("USER_NOT_FOUND", e.getMessage());
        } catch (Exception e) {
            log.error("用户信息更新失败: userId={}", userId, e);
            return Result.error("USER_UPDATE_ERROR", "用户信息更新失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询用户
     */
    public Result<User> getUserById(Long userId) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

            return Result.success(user);
        } catch (ResourceNotFoundException e) {
            return Result.error("USER_NOT_FOUND", e.getMessage());
        }
    }

    /**
     * 根据用户名查询用户
     */
    public Result<User> getUserByUsername(String username) {
        try {
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User with username", username));

            return Result.success(user);
        } catch (ResourceNotFoundException e) {
            return Result.error("USER_NOT_FOUND", e.getMessage());
        }
    }

    /**
     * 删除用户
     */
    @Transactional
    public Result<Void> deleteUser(Long userId) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

            user.delete();
            userRepository.save(user);

            log.info("用户删除成功: userId={}", userId);
            return Result.success();

        } catch (ResourceNotFoundException e) {
            return Result.error("USER_NOT_FOUND", e.getMessage());
        } catch (Exception e) {
            log.error("用户删除失败: userId={}", userId, e);
            return Result.error("USER_DELETE_ERROR", "用户删除失败: " + e.getMessage());
        }
    }

    /**
     * 激活用户
     */
    @Transactional
    public Result<Void> activateUser(Long userId) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

            user.activate();
            userRepository.save(user);

            log.info("用户激活成功: userId={}", userId);
            return Result.success();

        } catch (ResourceNotFoundException e) {
            return Result.error("USER_NOT_FOUND", e.getMessage());
        } catch (Exception e) {
            log.error("用户激活失败: userId={}", userId, e);
            return Result.error("USER_ACTIVATE_ERROR", "用户激活失败: " + e.getMessage());
        }
    }

    /**
     * 禁用用户
     */
    @Transactional
    public Result<Void> disableUser(Long userId) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

            user.disable();
            userRepository.save(user);

            log.info("用户禁用成功: userId={}", userId);
            return Result.success();

        } catch (ResourceNotFoundException e) {
            return Result.error("USER_NOT_FOUND", e.getMessage());
        } catch (Exception e) {
            log.error("用户禁用失败: userId={}", userId, e);
            return Result.error("USER_DISABLE_ERROR", "用户禁用失败: " + e.getMessage());
        }
    }

    /**
     * 查询激活用户列表
     */
    public Result<List<User>> getActiveUsers() {
        try {
            List<User> users = userRepository.findActiveUsers();
            return Result.success(users);
        } catch (Exception e) {
            log.error("查询激活用户列表失败", e);
            return Result.error("USER_QUERY_ERROR", "查询用户列表失败: " + e.getMessage());
        }
    }
}
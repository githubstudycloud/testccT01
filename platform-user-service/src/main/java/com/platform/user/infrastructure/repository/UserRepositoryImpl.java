package com.platform.user.infrastructure.repository;

import com.platform.user.domain.User;
import com.platform.user.domain.UserStatus;
import com.platform.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 用户仓储实现类
 */
@Component
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final JpaUserRepository jpaUserRepository;

    @Override
    public Optional<User> findById(Long id) {
        return jpaUserRepository.findById(id);
    }

    @Override
    public User save(User aggregate) {
        return jpaUserRepository.save(aggregate);
    }

    @Override
    public void delete(User aggregate) {
        jpaUserRepository.delete(aggregate);
    }

    @Override
    public void deleteById(Long id) {
        jpaUserRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaUserRepository.existsById(id);
    }

    @Override
    public Long nextId() {
        // 在JPA中，ID通常由数据库自动生成
        // 这里返回null，让JPA处理ID生成
        return null;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaUserRepository.findByUsernameAndDeletedFalse(username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaUserRepository.findByEmailAndDeletedFalse(email);
    }

    @Override
    public Optional<User> findByPhone(String phone) {
        return jpaUserRepository.findByPhoneAndDeletedFalse(phone);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaUserRepository.existsByUsernameAndDeletedFalse(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaUserRepository.existsByEmailAndDeletedFalse(email);
    }

    @Override
    public boolean existsByPhone(String phone) {
        return jpaUserRepository.existsByPhoneAndDeletedFalse(phone);
    }

    @Override
    public List<User> findActiveUsers() {
        return jpaUserRepository.findByStatusAndDeletedFalse(UserStatus.ACTIVE);
    }

    @Override
    public List<User> findByTenantId(String tenantId) {
        return jpaUserRepository.findByTenantIdAndDeletedFalse(tenantId);
    }
}
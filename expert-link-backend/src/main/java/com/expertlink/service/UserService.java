package com.expertlink.service;

import com.expertlink.domain.User;
import com.expertlink.repository.UserRepository;
import com.expertlink.security.UserRole;
import com.expertlink.security.UserRoleValidation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 获取所有用户（分页可选）
     */
    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * 根据ID查找用户
     */
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + id));
    }

    /**
     * 根据邮箱查找用户
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User requireByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + email));
    }

    /**
     * 根据用户名查找用户
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User requireByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
    }

    /**
     * 创建新用户
     */
    @Transactional
    public User create(User user) {
        // 验证邮箱是否已存在
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DataIntegrityViolationException("邮箱已被注册: " + user.getEmail());
        }

        // 验证用户名是否已存在
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new DataIntegrityViolationException("用户名已被使用: " + user.getUsername());
        }

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 设置默认值
        if (user.getIsActive() == null) {
            user.setIsActive(true);
        }
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.getRoles().add(UserRole.REGULAR_USER.name());
        } else {
            UserRoleValidation.validateRoleSet(user.getRoles());
            Set<String> normalized = UserRoleValidation.normalizeRoleSet(new HashSet<>(user.getRoles()));
            user.getRoles().clear();
            user.getRoles().addAll(normalized);
        }
        if (user.getTokenEpoch() == null) {
            user.setTokenEpoch(0);
        }

        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    /**
     * 更新用户信息
     */
    @Transactional
    public User update(Long id, User userDetails) {
        User existingUser = findById(id);
        Set<String> rolesBefore = new HashSet<>(existingUser.getRoles());
        boolean wasActive = !Boolean.FALSE.equals(existingUser.getIsActive());

        // 检查邮箱是否被其他用户占用
        if (userDetails.getEmail() != null && !userDetails.getEmail().equals(existingUser.getEmail())) {
            if (userRepository.existsByEmail(userDetails.getEmail())) {
                throw new DataIntegrityViolationException("邮箱已被其他用户注册: " + userDetails.getEmail());
            }
            existingUser.setEmail(userDetails.getEmail());
        }

        // 检查用户名是否被其他用户占用
        if (userDetails.getUsername() != null && !userDetails.getUsername().equals(existingUser.getUsername())) {
            if (userRepository.existsByUsername(userDetails.getUsername())) {
                throw new DataIntegrityViolationException("用户名已被其他用户使用: " + userDetails.getUsername());
            }
            existingUser.setUsername(userDetails.getUsername());
        }

        // 更新基本信息
        if (userDetails.getFullName() != null) {
            existingUser.setFullName(userDetails.getFullName());
        }
        if (userDetails.getPhoneNumber() != null) {
            existingUser.setPhoneNumber(userDetails.getPhoneNumber());
        }
        if (userDetails.getAvatar() != null) {
            existingUser.setAvatar(userDetails.getAvatar());
        }
        if (userDetails.getJobTitle() != null) {
            existingUser.setJobTitle(userDetails.getJobTitle());
        }
        if (userDetails.getDepartment() != null) {
            existingUser.setDepartment(userDetails.getDepartment());
        }
        if (userDetails.getBio() != null) {
            existingUser.setBio(userDetails.getBio());
        }
        if (userDetails.getIsActive() != null) {
            existingUser.setIsActive(userDetails.getIsActive());
        }

        // 更新角色
        if (userDetails.getRoles() != null && !userDetails.getRoles().isEmpty()) {
            UserRoleValidation.validateRoleSet(userDetails.getRoles());
            Set<String> next = UserRoleValidation.normalizeRoleSet(new HashSet<>(userDetails.getRoles()));
            boolean wasSuper = existingUser.getRoles().contains(UserRole.SUPER_ADMIN.name());
            if (wasSuper && !next.contains(UserRole.SUPER_ADMIN.name())
                    && userRepository.countByRole(UserRole.SUPER_ADMIN.name()) <= 1) {
                throw new IllegalArgumentException("不能移除最后一个超级管理员的角色");
            }
            existingUser.getRoles().clear();
            existingUser.getRoles().addAll(next);
        }

        // 更新密码（如果提供）
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        boolean invalidateSessions = false;
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            invalidateSessions = true;
        }
        if (userDetails.getRoles() != null && !userDetails.getRoles().isEmpty()) {
            Set<String> nextRoles = UserRoleValidation.normalizeRoleSet(new HashSet<>(userDetails.getRoles()));
            if (!rolesBefore.equals(nextRoles)) {
                invalidateSessions = true;
            }
        }
        if (userDetails.getIsActive() != null && Boolean.FALSE.equals(userDetails.getIsActive()) && wasActive) {
            invalidateSessions = true;
        }
        if (invalidateSessions) {
            bumpTokenEpoch(existingUser);
        }

        existingUser.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(existingUser);
    }

    /**
     * 删除用户
     */
    @Transactional
    public void delete(Long id) {
        User user = findById(id);
        if (user.getRoles().contains(UserRole.SUPER_ADMIN.name())
                && userRepository.countByRole(UserRole.SUPER_ADMIN.name()) <= 1) {
            throw new IllegalArgumentException("不能删除最后一个超级管理员");
        }

        userRepository.delete(user);
        log.info("删除用户，ID: {}", id);
    }

    /**
     * 搜索用户
     */
    public List<User> searchByKeyword(String keyword) {
        return userRepository.searchByKeyword(keyword);
    }

    /**
     * 根据状态筛选用户（ACTIVE / INACTIVE）
     */
    public List<User> findByStatus(String status) {
        if (!StringUtils.hasText(status) || "ACTIVE".equalsIgnoreCase(status)) {
            return userRepository.findByIsActiveTrue();
        }
        return userRepository.findByIsActiveFalse();
    }

    public List<User> findByUserType(String userType) {
        return userRepository.findByUserType(userType);
    }

    @Transactional
    public User updateUserStatus(Long id, String status) {
        User user = findById(id);
        boolean nextActive = "ACTIVE".equalsIgnoreCase(status);
        if (!nextActive) {
            bumpTokenEpoch(user);
        }
        user.setIsActive(nextActive);
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Transactional
    public User updateUserRole(Long id, String role) {
        return replaceRoles(id, Set.of(role));
    }

    /**
     * 全量替换用户角色集合
     */
    @Transactional
    public User replaceRoles(Long id, Set<String> roles) {
        UserRoleValidation.validateRoleSet(roles);
        Set<String> next = UserRoleValidation.normalizeRoleSet(new HashSet<>(roles));
        User user = findById(id);
        boolean wasSuper = user.getRoles().contains(UserRole.SUPER_ADMIN.name());
        if (wasSuper && !next.contains(UserRole.SUPER_ADMIN.name())
                && userRepository.countByRole(UserRole.SUPER_ADMIN.name()) <= 1) {
            throw new IllegalArgumentException("不能移除最后一个超级管理员的角色");
        }
        user.getRoles().clear();
        user.getRoles().addAll(next);
        bumpTokenEpoch(user);
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public User authenticate(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        return user;
    }

    public Page<User> searchUsers(String keyword, String role, Pageable pageable) {
        String kw = StringUtils.hasText(keyword) ? keyword.trim() : "";
        if (StringUtils.hasText(role)) {
            return userRepository.searchByKeywordAndRole(kw, role.trim(), pageable);
        }
        if (!StringUtils.hasText(kw)) {
            return userRepository.findAll(pageable);
        }
        return userRepository.searchByKeyword(kw, pageable);
    }

    public boolean checkEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean checkUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public long countUsers() {
        return userRepository.count();
    }

    public List<User> findActiveUsers() {
        return userRepository.findByIsActiveTrue();
    }

    /**
     * 根据角色筛选用户
     */
    public List<User> findByRole(String role) {
        return userRepository.findByRole(role);
    }

    /**
     * 统计角色用户数量
     */
    public long countByRole(String role) {
        return userRepository.countByRole(role);
    }

    /**
     * 更新用户最后登录时间
     */
    @Transactional
    public void updateLastLogin(Long userId) {
        User user = findById(userId);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * 激活/禁用用户
     */
    @Transactional
    public User toggleActiveStatus(Long id, boolean isActive) {
        User user = findById(id);
        if (!isActive) {
            bumpTokenEpoch(user);
        }
        user.setIsActive(isActive);
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    private static void bumpTokenEpoch(User user) {
        int cur = user.getTokenEpoch() == null ? 0 : user.getTokenEpoch();
        user.setTokenEpoch(cur + 1);
    }
}
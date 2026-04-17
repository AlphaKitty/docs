package com.expertlink.controller;

import com.expertlink.dto.ApiResponse;
import com.expertlink.dto.ApiResponses;
import com.expertlink.dto.PaginatedResponse;
import com.expertlink.dto.user.CreateUserRequest;
import com.expertlink.dto.user.UpdateUserRolesRequest;
import com.expertlink.domain.User;
import com.expertlink.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取所有用户（分页）
     * GET /api/users
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<User>>> getAllUsers(Pageable pageable) {
        Page<User> users = userService.findAll(pageable);
        return ApiResponses.okPage(users);
    }

    /**
     * 根据ID获取用户
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
        User user = userService.findById(id);
        return ApiResponses.ok(user);
    }

    /**
     * 根据邮箱获取用户
     * GET /api/users/email/{email}
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<User>> getUserByEmail(@PathVariable String email) {
        User user = userService.requireByEmail(email);
        return ApiResponses.ok(user);
    }

    /**
     * 根据用户名获取用户
     * GET /api/users/username/{username}
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponse<User>> getUserByUsername(@PathVariable String username) {
        User user = userService.requireByUsername(username);
        return ApiResponses.ok(user);
    }

    /**
     * 根据用户类型获取用户
     * GET /api/users/type/{userType}
     */
    @GetMapping("/type/{userType}")
    public ResponseEntity<ApiResponse<List<User>>> getUsersByType(@PathVariable String userType) {
        List<User> users = userService.findByUserType(userType);
        return ApiResponses.ok(users);
    }

    /**
     * 创建用户
     * POST /api/users
     */
    @PostMapping
    public ResponseEntity<ApiResponse<User>> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword())
                .fullName(request.getFullName())
                .isActive(true)
                .roles(new HashSet<>(request.getRoles()))
                .build();
        User createdUser = userService.create(user);
        return ApiResponses.created(createdUser);
    }

    /**
     * 全量替换用户角色
     * PUT /api/users/{id}/roles
     */
    @PutMapping("/{id}/roles")
    public ResponseEntity<ApiResponse<User>> replaceUserRoles(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRolesRequest body) {
        User updated = userService.replaceRoles(id, new HashSet<>(body.getRoles()));
        return ApiResponses.ok(updated);
    }

    /**
     * 更新用户
     * PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        User updatedUser = userService.update(id, userDetails);
        return ApiResponses.ok(updatedUser);
    }

    /**
     * 删除用户
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ApiResponses.noContent("用户删除成功");
    }

    /**
     * 更新用户状态
     * PUT /api/users/{id}/status
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<User>> updateUserStatus(@PathVariable Long id, @RequestParam String status) {
        User user = userService.updateUserStatus(id, status);
        return ApiResponses.ok(user);
    }

    /**
     * 更新用户角色
     * PUT /api/users/{id}/role
     */
    @PutMapping("/{id}/role")
    public ResponseEntity<ApiResponse<User>> updateUserRole(@PathVariable Long id, @RequestParam String role) {
        User user = userService.updateUserRole(id, role);
        return ApiResponses.ok(user);
    }

    /**
     * 验证用户凭证
     * POST /api/users/authenticate
     */
    @PostMapping("/authenticate")
    public ResponseEntity<ApiResponse<User>> authenticateUser(@RequestParam String email, @RequestParam String password) {
        User user = userService.authenticate(email, password);
        return ApiResponses.ok(user);
    }

    /**
     * 搜索用户
     * GET /api/users/search
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PaginatedResponse<User>>> searchUsers(
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        Page<User> users = userService.searchUsers(keyword, pageable);
        return ApiResponses.okPage(users);
    }

    /**
     * 检查邮箱是否存在
     * GET /api/users/check-email
     */
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmailExists(@RequestParam String email) {
        boolean exists = userService.checkEmailExists(email);
        return ApiResponses.ok(exists);
    }

    /**
     * 检查用户名是否存在
     * GET /api/users/check-username
     */
    @GetMapping("/check-username")
    public ResponseEntity<ApiResponse<Boolean>> checkUsernameExists(@RequestParam String username) {
        boolean exists = userService.checkUsernameExists(username);
        return ApiResponses.ok(exists);
    }

    /**
     * 获取用户数量
     * GET /api/users/count
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> countUsers() {
        long count = userService.countUsers();
        return ApiResponses.ok(count);
    }

    /**
     * 获取活跃用户
     * GET /api/users/active
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<User>>> getActiveUsers() {
        List<User> users = userService.findActiveUsers();
        return ApiResponses.ok(users);
    }
}
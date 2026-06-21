package com.wuhan.novel.user.controller;

import com.wuhan.novel.user.dto.ApiResponse;
import com.wuhan.novel.user.dto.LoginRequest;
import com.wuhan.novel.user.dto.LoginResult;
import com.wuhan.novel.user.dto.RegisterRequest;
import com.wuhan.novel.user.dto.UserUpdateRequest;
import com.wuhan.novel.user.model.User;
import com.wuhan.novel.user.service.UserService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ApiResponse<User> register(@RequestBody RegisterRequest request) {
        try {
            return ApiResponse.ok(userService.register(request));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ApiResponse<LoginResult> login(@RequestBody LoginRequest request) {
        try {
            return ApiResponse.ok(userService.login(request));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    @GetMapping("/profile")
    public ApiResponse<User> profile(@RequestParam(defaultValue = "1") Long userId) {
        try {
            return ApiResponse.ok(userService.findById(userId));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ApiResponse<User> getUser(@PathVariable Long id) {
        try {
            return ApiResponse.ok(userService.findById(id));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    @GetMapping
    public ApiResponse<List<User>> listUsers() {
        return ApiResponse.ok(userService.listUsers());
    }

    @PutMapping("/profile/{id}")
    public ApiResponse<User> updateProfile(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        try {
            return ApiResponse.ok(userService.updateProfile(id, request));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }
}

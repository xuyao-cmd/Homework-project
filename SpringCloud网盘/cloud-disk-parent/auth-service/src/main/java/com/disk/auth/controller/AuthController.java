package com.disk.auth.controller;

import com.disk.common.Result;
import com.disk.common.dto.LoginDto;
import com.disk.common.dto.LoginResponseDto;
import com.disk.common.dto.RegisterDto;
import com.disk.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/auth/register")
    public Result<String> register(@RequestBody RegisterDto dto) {
        return userService.register(dto);
    }

    /**
     * 用户登录
     */
    @PostMapping("/auth/login")
    public Result<LoginResponseDto> login(@RequestBody LoginDto dto) {
        return userService.login(dto);
    }

    // ========== 第三方登录 - 授权跳转 ==========

    /**
     * GitHub OAuth 授权跳转
     * GET /oauth/github/authorize
     */
    @GetMapping("/oauth/github/authorize")
    public void githubAuthorize(HttpServletResponse response) throws IOException {
        String clientId = System.getProperty("github.client.id", "your_github_client_id");
        String redirectUri = "http://localhost:8080/oauth/github/callback";
        String url = "https://github.com/login/oauth/authorize"
                + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&scope=user:email";
        response.sendRedirect(url);
    }

    /**
     * 微信 OAuth 授权跳转（演示模式）
     * GET /oauth/wechat/authorize
     */
    @GetMapping("/oauth/wechat/authorize")
    public void wechatAuthorize(HttpServletResponse response) throws IOException {
        String code = "demo_wechat_" + System.currentTimeMillis();
        String frontendCallback = "http://localhost:5173/oauth/callback?platform=wechat&code=" + code;
        response.sendRedirect("/wechat-oauth.html?callback="
                + java.net.URLEncoder.encode(frontendCallback, "UTF-8"));
    }

    /**
     * QQ OAuth 授权跳转（演示模式）
     * GET /oauth/qq/authorize
     */
    @GetMapping("/oauth/qq/authorize")
    public void qqAuthorize(HttpServletResponse response) throws IOException {
        String code = "demo_qq_" + System.currentTimeMillis();
        String frontendCallback = "http://localhost:5173/oauth/callback?platform=qq&code=" + code;
        response.sendRedirect("/qq-oauth.html?callback="
                + java.net.URLEncoder.encode(frontendCallback, "UTF-8"));
    }

    // ========== 第三方登录 - 回调处理 ==========

    /**
     * GitHub OAuth 回调
     * GET /oauth/github/callback?code=xxx
     */
    @GetMapping("/oauth/github/callback")
    public Result<LoginResponseDto> githubCallback(@RequestParam String code) {
        return userService.githubLogin(code);
    }

    /**
     * 微信 OAuth 回调（演示模式）
     * GET /oauth/wechat/callback?code=xxx
     */
    @GetMapping("/oauth/wechat/callback")
    public Result<LoginResponseDto> wechatCallback(@RequestParam String code) {
        return userService.demoOAuthLogin("wechat", code);
    }

    /**
     * QQ OAuth 回调（演示模式）
     * GET /oauth/qq/callback?code=xxx
     */
    @GetMapping("/oauth/qq/callback")
    public Result<LoginResponseDto> qqCallback(@RequestParam String code) {
        return userService.demoOAuthLogin("qq", code);
    }

    // ========== 管理员接口 ==========

    /**
     * 用户列表（分页）
     */
    @GetMapping("/auth/admin/users")
    public Result<Map<String, Object>> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        return userService.getUserList(page, size, keyword);
    }

    /**
     * 禁用/启用用户
     */
    @PutMapping("/auth/admin/user/{userId}/status")
    public Result<String> toggleUserStatus(
            @PathVariable Long userId,
            @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        return userService.toggleUserStatus(userId, status);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/auth/admin/user/{userId}")
    public Result<String> deleteUser(@PathVariable Long userId) {
        return userService.deleteUser(userId);
    }

    /**
     * 用户统计
     */
    @GetMapping("/auth/admin/stats")
    public Result<Map<String, Object>> getUserStats() {
        return userService.getUserStats();
    }

    /**
     * 健康检查
     */
    @GetMapping("/auth/health")
    public Result<String> health() {
        return Result.success("auth-service is running");
    }
}

package com.disk.auth.service;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.disk.auth.mapper.UserMapper;
import com.disk.common.JwtUtil;
import com.disk.common.Result;
import com.disk.common.dto.LoginDto;
import com.disk.common.dto.LoginResponseDto;
import com.disk.common.dto.RegisterDto;
import com.disk.common.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    /**
     * 用户注册
     */
    public Result<String> register(RegisterDto dto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        User existUser = this.getOne(wrapper);

        if (existUser != null) {
            return Result.error("用户名已存在");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(DigestUtils.md5DigestAsHex(dto.getPassword().getBytes(StandardCharsets.UTF_8)));
        user.setEmail(dto.getEmail());
        user.setRole("user");
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        boolean save = this.save(user);
        if (save) {
            log.info("用户注册成功: {}", dto.getUsername());
            return Result.success("注册成功");
        }
        return Result.error("注册失败");
    }

    /**
     * 用户登录
     */
    public Result<LoginResponseDto> login(LoginDto dto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        User user = this.getOne(wrapper);

        if (user == null) {
            return Result.error("用户名不存在");
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            return Result.error("账号已被禁用，请联系管理员");
        }

        String encryptPassword = DigestUtils.md5DigestAsHex(dto.getPassword().getBytes(StandardCharsets.UTF_8));
        if (!encryptPassword.equals(user.getPassword())) {
            return Result.error("密码错误");
        }

        String token = JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        log.info("用户登录成功: {}", dto.getUsername());

        LoginResponseDto response = new LoginResponseDto(token, user.getId(), user.getUsername(), user.getRole());
        return Result.success(response);
    }

    // ========== 第三方登录 ==========

    /**
     * GitHub OAuth 登录回调
     * @param code GitHub 返回的授权码
     */
    public Result<LoginResponseDto> githubLogin(String code) {
        try {
            // 1. 用 code 换取 access_token
            String tokenUrl = "https://github.com/login/oauth/access_token";
            Map<String, Object> tokenParams = new HashMap<>();
            tokenParams.put("client_id", System.getProperty("github.client.id", "your_client_id"));
            tokenParams.put("client_secret", System.getProperty("github.client.secret", "your_client_secret"));
            tokenParams.put("code", code);

            String tokenResponse = HttpUtil.post(tokenUrl, tokenParams);
            // GitHub 返回格式: access_token=xxx&scope=xxx&token_type=bearer
            String accessToken = null;
            if (tokenResponse != null && tokenResponse.contains("access_token=")) {
                accessToken = tokenResponse.split("access_token=")[1].split("&")[0];
            }

            if (accessToken == null) {
                return Result.error("获取 GitHub access_token 失败");
            }

            // 2. 用 access_token 获取用户信息
            String userInfoUrl = "https://api.github.com/user";
            String userInfoStr = HttpUtil.createGet(userInfoUrl)
                    .header("Authorization", "token " + accessToken)
                    .header("Accept", "application/json")
                    .execute()
                    .body();

            JSONObject githubUser = JSONUtil.parseObj(userInfoStr);
            String openId = githubUser.getStr("id");
            String login = githubUser.getStr("login");
            String avatarUrl = githubUser.getStr("avatar_url");
            String email = githubUser.getStr("email");

            if (openId == null) {
                return Result.error("获取 GitHub 用户信息失败");
            }

            // 3. 查找或创建用户
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getOauthPlatform, "github")
                    .eq(User::getOauthOpenId, openId);
            User user = this.getOne(wrapper);

            if (user == null) {
                // 新用户：创建账号
                user = new User();
                user.setUsername("github_" + (login != null ? login : RandomUtil.randomString(8)));
                user.setPassword(DigestUtils.md5DigestAsHex(RandomUtil.randomString(16).getBytes(StandardCharsets.UTF_8)));
                user.setEmail(email);
                user.setAvatar(avatarUrl);
                user.setOauthPlatform("github");
                user.setOauthOpenId(openId);
                user.setRole("user");
                user.setStatus(1);
                user.setCreateTime(LocalDateTime.now());
                user.setUpdateTime(LocalDateTime.now());
                this.save(user);
                log.info("GitHub 第三方登录，创建新用户: {}", user.getUsername());
            } else {
                // 老用户：更新头像
                user.setAvatar(avatarUrl);
                user.setUpdateTime(LocalDateTime.now());
                this.updateById(user);
                log.info("GitHub 第三方登录: {}", user.getUsername());
            }

            if (user.getStatus() != null && user.getStatus() == 0) {
                return Result.error("账号已被禁用，请联系管理员");
            }

            String token = JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
            LoginResponseDto response = new LoginResponseDto(token, user.getId(), user.getUsername(), user.getRole());
            return Result.success(response);

        } catch (Exception e) {
            log.error("GitHub 登录异常: {}", e.getMessage(), e);
            return Result.error("第三方登录失败: " + e.getMessage());
        }
    }

    /**
     * 演示模式 OAuth 登录（微信/QQ）
     * 由于微信/QQ OAuth 需要企业资质，这里用演示模式模拟
     */
    public Result<LoginResponseDto> demoOAuthLogin(String platform, String code) {
        if (code == null || code.isEmpty()) {
            return Result.error("授权码为空");
        }

        // 模拟用户信息
        String openId = platform + "_" + RandomUtil.randomString(12);
        String nickname = platform + "_user_" + RandomUtil.randomString(6);
        String avatarUrl = "https://api.dicebear.com/7.x/avataaars/svg?seed=" + nickname;

        // 查找或创建用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOauthPlatform, platform)
                .eq(User::getOauthOpenId, openId);
        User user = this.getOne(wrapper);

        if (user == null) {
            user = new User();
            user.setUsername(nickname);
            user.setPassword(DigestUtils.md5DigestAsHex(RandomUtil.randomString(16).getBytes(StandardCharsets.UTF_8)));
            user.setAvatar(avatarUrl);
            user.setOauthPlatform(platform);
            user.setOauthOpenId(openId);
            user.setRole("user");
            user.setStatus(1);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            this.save(user);
            log.info("{} 演示登录，创建新用户: {}", platform, user.getUsername());
        } else {
            user.setAvatar(avatarUrl);
            user.setUpdateTime(LocalDateTime.now());
            this.updateById(user);
            log.info("{} 演示登录: {}", platform, user.getUsername());
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            return Result.error("账号已被禁用，请联系管理员");
        }

        String token = JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        LoginResponseDto response = new LoginResponseDto(token, user.getId(), user.getUsername(), user.getRole());
        return Result.success(response);
    }

    // ========== 管理员 - 用户管理 ==========

    /**
     * 分页查询用户列表
     */
    public Result<Map<String, Object>> getUserList(Integer page, Integer size, String keyword) {
        Page<User> pageParam = new Page<>(page != null ? page : 1, size != null ? size : 10);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(User::getUsername, keyword).or().like(User::getEmail, keyword);
        }
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> result = this.page(pageParam, wrapper);

        Map<String, Object> map = new HashMap<>();
        map.put("total", result.getTotal());
        map.put("list", result.getRecords());
        return Result.success(map);
    }

    /**
     * 禁用/启用用户
     */
    public Result<String> toggleUserStatus(Long userId, Integer status) {
        User user = this.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        this.updateById(user);
        log.info("管理员 {} 用户: userId={}", status == 1 ? "启用" : "禁用", userId);
        return Result.success(status == 1 ? "已启用" : "已禁用");
    }

    /**
     * 删除用户
     */
    public Result<String> deleteUser(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if ("admin".equals(user.getRole())) {
            return Result.error("不能删除管理员账号");
        }
        this.removeById(userId);
        log.info("管理员删除用户: userId={}", userId);
        return Result.success("删除成功");
    }

    /**
     * 获取用户统计信息
     */
    public Result<Map<String, Object>> getUserStats() {
        long totalUsers = this.count();
        long activeUsers = this.count(new LambdaQueryWrapper<User>().eq(User::getStatus, 1));
        long disabledUsers = this.count(new LambdaQueryWrapper<User>().eq(User::getStatus, 0));

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("disabledUsers", disabledUsers);
        return Result.success(stats);
    }
}

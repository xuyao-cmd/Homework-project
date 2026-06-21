package com.wuhan.novel.user.service;

import com.wuhan.novel.user.dto.LoginRequest;
import com.wuhan.novel.user.dto.LoginResult;
import com.wuhan.novel.user.dto.RegisterRequest;
import com.wuhan.novel.user.dto.UserUpdateRequest;
import com.wuhan.novel.user.model.User;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserService {
    private final Map<Long, User> users = new LinkedHashMap<Long, User>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @PostConstruct
    public void init() {
        saveSeedUser("admin", "123456", "平台管理员", "负责平台审核、内容推荐和用户服务。", "ADMIN", 3, 4);
        saveSeedUser("author", "123456", "青衫烟雨", "网络小说作者，擅长玄幻与都市题材。", "USER", 12, 2);
    }

    private void saveSeedUser(String username, String password, String nickname, String intro, String role, int followCount, int publishCount) {
        Long id = idGenerator.getAndIncrement();
        User user = new User(id, username, password, nickname, "https://dummyimage.com/120x120/409eff/ffffff&text=" + nickname.substring(0, 1), intro);
        user.setRole(role);
        user.setFollowCount(followCount);
        user.setPublishCount(publishCount);
        users.put(id, user);
    }

    public synchronized User register(RegisterRequest request) {
        if (request == null || isBlank(request.getUsername()) || isBlank(request.getPassword())) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }
        String username = request.getUsername().trim();
        if ("admin".equalsIgnoreCase(username)) {
            throw new IllegalArgumentException("admin 是平台唯一管理员账号，不能注册");
        }
        for (User user : users.values()) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                throw new IllegalArgumentException("用户名已存在");
            }
        }
        Long id = idGenerator.getAndIncrement();
        String nickname = isBlank(request.getNickname()) ? request.getUsername() : request.getNickname();
        User user = new User(id, username, request.getPassword(), nickname,
                "https://dummyimage.com/120x120/67c23a/ffffff&text=" + nickname.substring(0, 1),
                "这个人很低调，还没有填写个人简介。 ");
        user.setRole("USER");
        users.put(id, user);
        return hidePassword(user);
    }

    public LoginResult login(LoginRequest request) {
        if (request == null || isBlank(request.getUsername()) || isBlank(request.getPassword())) {
            throw new IllegalArgumentException("请输入用户名和密码");
        }
        for (User user : users.values()) {
            if (user.getUsername().equals(request.getUsername()) && user.getPassword().equals(request.getPassword())) {
                String token = "novel-token-" + user.getId() + "-" + System.currentTimeMillis();
                return new LoginResult(token, hidePassword(user));
            }
        }
        throw new IllegalArgumentException("用户名或密码错误");
    }

    public User findById(Long id) {
        User user = users.get(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return hidePassword(user);
    }

    public List<User> listUsers() {
        List<User> result = new ArrayList<User>();
        for (User user : users.values()) {
            result.add(hidePassword(user));
        }
        return Collections.unmodifiableList(result);
    }

    public synchronized User updateProfile(Long id, UserUpdateRequest request) {
        User user = users.get(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (request != null) {
            if (!isBlank(request.getNickname())) {
                user.setNickname(request.getNickname());
            }
            if (!isBlank(request.getIntroduction())) {
                user.setIntroduction(request.getIntroduction());
            }
            if (!isBlank(request.getAvatar())) {
                user.setAvatar(request.getAvatar());
            }
        }
        return hidePassword(user);
    }

    private User hidePassword(User source) {
        User user = new User(source.getId(), source.getUsername(), null, source.getNickname(), source.getAvatar(), source.getIntroduction());
        user.setRole(source.getRole());
        user.setFollowCount(source.getFollowCount());
        user.setPublishCount(source.getPublishCount());
        return user;
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().length() == 0;
    }
}

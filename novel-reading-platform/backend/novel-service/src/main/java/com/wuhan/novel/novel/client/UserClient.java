package com.wuhan.novel.novel.client;

import com.wuhan.novel.novel.dto.ApiResponse;
import com.wuhan.novel.novel.dto.UserView;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", path = "/api/users")
public interface UserClient {
    @GetMapping("/{id}")
    ApiResponse<UserView> getUser(@PathVariable("id") Long id);
}

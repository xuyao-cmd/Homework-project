package com.wuhan.novel.novel.controller;

import com.wuhan.novel.novel.dto.ApiResponse;
import com.wuhan.novel.novel.dto.PublishNovelRequest;
import com.wuhan.novel.novel.model.Chapter;
import com.wuhan.novel.novel.model.Novel;
import com.wuhan.novel.novel.service.NovelService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/novels")
public class NovelController {
    private final NovelService novelService;

    public NovelController(NovelService novelService) {
        this.novelService = novelService;
    }

    @GetMapping
    public ApiResponse<List<Novel>> list(@RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) String category,
                                         @RequestParam(required = false) String subCategory,
                                         @RequestParam(required = false) Integer limit) {
        return ApiResponse.ok(novelService.list(keyword, category, subCategory, limit));
    }

    @GetMapping("/recommend")
    public ApiResponse<List<Novel>> recommend() {
        return ApiResponse.ok(novelService.recommend());
    }

    @GetMapping("/{id}")
    public ApiResponse<Novel> detail(@PathVariable Long id) {
        try {
            return ApiResponse.ok(novelService.detail(id));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    @GetMapping("/{id}/chapters")
    public ApiResponse<List<Chapter>> chapters(@PathVariable Long id) {
        try {
            return ApiResponse.ok(novelService.chapters(id));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    @GetMapping("/{novelId}/chapters/{chapterId}")
    public ApiResponse<Chapter> read(@PathVariable Long novelId, @PathVariable Long chapterId) {
        try {
            return ApiResponse.ok(novelService.read(novelId, chapterId));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    @PostMapping("/publish")
    public ApiResponse<Novel> publish(@RequestBody PublishNovelRequest request, @RequestParam String adminUsername) {
        try {
            return ApiResponse.ok(novelService.publish(request, adminUsername));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    @PostMapping("/submit")
    public ApiResponse<Novel> submit(@RequestBody PublishNovelRequest request) {
        try {
            return ApiResponse.ok(novelService.submitForReview(request));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    @GetMapping("/pending")
    public ApiResponse<List<Novel>> pending(@RequestParam String adminUsername) {
        try {
            return ApiResponse.ok(novelService.pending(adminUsername));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<Novel> approve(@PathVariable Long id, @RequestParam String adminUsername) {
        try {
            return ApiResponse.ok(novelService.approve(id, adminUsername));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }
}

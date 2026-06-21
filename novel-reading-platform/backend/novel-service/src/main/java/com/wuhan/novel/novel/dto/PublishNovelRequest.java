package com.wuhan.novel.novel.dto;

import java.util.List;

public class PublishNovelRequest {
    private String title;
    private Long authorId;
    private String category;
    private String subCategory;
    private String cover;
    private String summary;
    private String firstChapterTitle;
    private String firstChapterContent;
    private List<ChapterDraft> chapters;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getFirstChapterTitle() {
        return firstChapterTitle;
    }

    public void setFirstChapterTitle(String firstChapterTitle) {
        this.firstChapterTitle = firstChapterTitle;
    }

    public String getFirstChapterContent() {
        return firstChapterContent;
    }

    public void setFirstChapterContent(String firstChapterContent) {
        this.firstChapterContent = firstChapterContent;
    }

    public List<ChapterDraft> getChapters() {
        return chapters;
    }

    public void setChapters(List<ChapterDraft> chapters) {
        this.chapters = chapters;
    }

    public static class ChapterDraft {
        private String title;
        private String content;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}

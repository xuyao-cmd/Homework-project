package com.wuhan.novel.novel.model;

import java.util.ArrayList;
import java.util.List;

public class Novel {
    private Long id;
    private String title;
    private Long authorId;
    private String authorName;
    private String category;
    private String subCategory;
    private String cover;
    private String summary;
    private Integer wordCount;
    private Integer clickCount;
    private Double score;
    private String status;
    private List<String> tags = new ArrayList<String>();

    public Novel() {
    }

    public Novel(Long id, String title, Long authorId, String category, String cover, String summary) {
        this.id = id;
        this.title = title;
        this.authorId = authorId;
        this.category = category;
        this.cover = cover;
        this.summary = summary;
        this.wordCount = 0;
        this.clickCount = 0;
        this.score = 9.1;
        this.status = "连载中";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
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

    public Integer getWordCount() {
        return wordCount;
    }

    public void setWordCount(Integer wordCount) {
        this.wordCount = wordCount;
    }

    public Integer getClickCount() {
        return clickCount;
    }

    public void setClickCount(Integer clickCount) {
        this.clickCount = clickCount;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}

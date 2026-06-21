package com.wuhan.novel.novel.model;

public class Chapter {
    private Long id;
    private Long novelId;
    private Integer chapterNo;
    private String title;
    private String content;
    private String updateTime;

    public Chapter() {
    }

    public Chapter(Long id, Long novelId, Integer chapterNo, String title, String content, String updateTime) {
        this.id = id;
        this.novelId = novelId;
        this.chapterNo = chapterNo;
        this.title = title;
        this.content = content;
        this.updateTime = updateTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getNovelId() {
        return novelId;
    }

    public void setNovelId(Long novelId) {
        this.novelId = novelId;
    }

    public Integer getChapterNo() {
        return chapterNo;
    }

    public void setChapterNo(Integer chapterNo) {
        this.chapterNo = chapterNo;
    }

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

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
}

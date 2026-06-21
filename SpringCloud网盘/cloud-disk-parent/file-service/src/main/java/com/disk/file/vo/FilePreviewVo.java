package com.disk.file.vo;

import lombok.Data;

@Data
public class FilePreviewVo {
    private String fileName;
    private Long fileSize;
    private String fileType;
    private Boolean supportPreview;
    private String previewUrl;
}
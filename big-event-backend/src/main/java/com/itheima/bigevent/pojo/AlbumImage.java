package com.itheima.bigevent.pojo;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AlbumImage {
    private Integer id; // 创建时不需要，由数据库自动生成
    @NotNull
    private Integer albumId;
    @NotEmpty
    private String imageUrl;
    private String imageName;
    private String imageDesc;
    private Integer sortOrder;
    private LocalDateTime createTime;
}


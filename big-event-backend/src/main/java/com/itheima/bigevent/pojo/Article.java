package com.itheima.bigevent.pojo;

import com.itheima.bigevent.anno.State;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.Default;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;

@Data
public class Article {
    @NotNull(groups = { Update.class })
    private Integer id;// 主键ID

    @NotEmpty(groups = { Add.class, Update.class })
    private String title;// 文章标题

    @NotEmpty(groups = { Add.class, Update.class })
    private String content;// 文章内容

    @NotEmpty(groups = { Add.class, Update.class })
    @URL(groups = { Add.class, Update.class }, message = "封面图片URL格式不正确")
    private String coverImg;// 封面图像

    @State(groups = { Add.class, Update.class })
    private String state;// 发布状态 已发布|草稿

    @NotNull(groups = { Add.class, Update.class })
    private Integer categoryId;// 文章分类id
    private Integer createUser;// 创建人ID
    private LocalDateTime createTime;// 创建时间
    private LocalDateTime updateTime;// 更新时间

    // SEO相关字段
    private String seoTitle;// SEO标题
    private String seoDescription;// SEO描述
    private String seoKeywords;// SEO关键词

    // 统计字段
    private Integer likeCount;// 点赞数
    private Integer favoriteCount;// 收藏数

    public interface Add extends Default {

    }

    public interface Update extends Default {

    }
}

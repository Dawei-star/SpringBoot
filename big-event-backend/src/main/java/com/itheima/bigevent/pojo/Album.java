package com.itheima.bigevent.pojo;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.Default;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Album {
  @NotNull(groups = { Update.class })
  private Integer id;
  @NotEmpty(groups = { Add.class, Update.class })
  private String title;
  private String description;
  private String coverImg;
  private Integer createUser; // 由后端设置，不需要前端传递
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
  private List<AlbumImage> images;

  public interface Add extends Default {
  }

  public interface Update extends Default {
  }
}

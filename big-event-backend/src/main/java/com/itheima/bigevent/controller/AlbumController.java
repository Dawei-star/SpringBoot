package com.itheima.bigevent.controller;

import com.itheima.bigevent.anno.OpLog;
import com.itheima.bigevent.pojo.Album;
import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.pojo.ResultCode;
import com.itheima.bigevent.service.AlbumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/album")
@Validated
public class AlbumController {

    @Autowired
    private AlbumService albumService;

    @OpLog(module = "相册管理", operation = "获取相册列表")
    @GetMapping
    public Result<List<Album>> list() {
        List<Album> albums = albumService.list();
        return Result.success(albums);
    }

    @OpLog(module = "相册管理", operation = "获取相册详情")
    @GetMapping("/{id}")
    public Result<Album> getById(@PathVariable Integer id) {
        Album album = albumService.findById(id);
        return Result.success(album);
    }

    @OpLog(module = "相册管理", operation = "创建相册")
    @PostMapping
    public Result<String> add(@RequestBody @Validated(Album.Add.class) Album album) {
        albumService.add(album);
        return Result.success(ResultCode.CREATED, "相册创建成功");
    }

    @OpLog(module = "相册管理", operation = "更新相册")
    @PutMapping
    public Result<String> update(@RequestBody @Validated(Album.Update.class) Album album) {
        albumService.update(album);
        return Result.success(ResultCode.UPDATED, "相册更新成功");
    }

    @OpLog(module = "相册管理", operation = "删除相册")
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Integer id) {
        albumService.delete(id);
        return Result.success(ResultCode.DELETED, "相册删除成功");
    }
}


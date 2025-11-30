package com.itheima.bigevent.controller;

import com.itheima.bigevent.anno.OpLog;
import com.itheima.bigevent.pojo.Category;
import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.pojo.ResultCode;
import com.itheima.bigevent.service.CategoryService;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("/category")
@Validated
public class CategoryController {

    @Autowired
    private CategoryService categoryService;


    @OpLog(module = "文章分类", operation = "新增分类")
    @PostMapping
    public Result<String> add(@RequestBody @Validated(Category.Add.class) Category category) {
        categoryService.add(category);
        return Result.success(ResultCode.CREATED, "分类添加成功");
    }

    @GetMapping
    public Result<List<Category>> list() {
        final List<Category> categoryList = categoryService.list();
        return Result.success(categoryList);
    }

    @GetMapping("/detail")
    public Result<Category> detail(@NotNull @RequestParam("id") final Integer id) {
        final Category category = categoryService.findById(id);
        return Result.success(category);
    }


    @OpLog(module = "文章分类", operation = "修改分类")
    @PutMapping
    public Result<String> update(@RequestBody @Validated(Category.Update.class) Category category) {
        categoryService.update(category);
        return Result.success(ResultCode.UPDATED, "分类更新成功");
    }

    @OpLog(module = "文章分类", operation = "删除分类")
    @DeleteMapping
    public Result<String> delete(@NotNull @RequestParam("id") final Integer id) {
        categoryService.delete(id);
        return Result.success(ResultCode.DELETED, "分类删除成功");
    }
}

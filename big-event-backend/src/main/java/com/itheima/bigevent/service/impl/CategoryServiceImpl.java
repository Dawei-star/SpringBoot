package com.itheima.bigevent.service.impl;

import com.itheima.bigevent.mapper.CategoryMapper;
import com.itheima.bigevent.pojo.Category;
import com.itheima.bigevent.service.CategoryService;
import com.itheima.bigevent.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public void add(final Category category) {
        category.setCreateUser(ThreadLocalUtil.getCurrentUserIdRequired());
        categoryMapper.add(category);
    }

    @Override
    public List<Category> list() {
        return categoryMapper.list(ThreadLocalUtil.getCurrentUserId());
    }

    @Override
    public Category findById(final Integer id) {

        return categoryMapper.findById(id);
    }

    @Override
    public void update(final Category category) {
        categoryMapper.update(category);
    }

    @Override
    public void delete(final Integer id) {
        categoryMapper.delete(id);
    }
}

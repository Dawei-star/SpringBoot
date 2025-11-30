package com.itheima.bigevent.service;

import com.itheima.bigevent.pojo.Album;

import java.util.List;

public interface AlbumService {
    void add(Album album);
    List<Album> list();
    Album findById(Integer id);
    void update(Album album);
    void delete(Integer id);
}


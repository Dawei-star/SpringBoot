package com.itheima.bigevent.service.impl;

import com.itheima.bigevent.mapper.AlbumImageMapper;
import com.itheima.bigevent.mapper.AlbumMapper;
import com.itheima.bigevent.pojo.Album;
import com.itheima.bigevent.pojo.AlbumImage;
import com.itheima.bigevent.service.AlbumService;
import com.itheima.bigevent.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AlbumServiceImpl implements AlbumService {

    @Autowired
    private AlbumMapper albumMapper;

    @Autowired
    private AlbumImageMapper albumImageMapper;

    @Override
    @Transactional
    public void add(Album album) {
        album.setCreateUser(ThreadLocalUtil.getCurrentUserIdRequired());
        albumMapper.add(album);
        
        if (album.getImages() != null && !album.getImages().isEmpty()) {
            for (AlbumImage image : album.getImages()) {
                image.setAlbumId(album.getId());
                albumImageMapper.add(image);
            }
        }
    }

    @Override
    public List<Album> list() {
        List<Album> albums = albumMapper.list();
        for (Album album : albums) {
            List<AlbumImage> images = albumImageMapper.findByAlbumId(album.getId());
            album.setImages(images);
        }
        return albums;
    }

    @Override
    public Album findById(Integer id) {
        Album album = albumMapper.findById(id);
        if (album != null) {
            List<AlbumImage> images = albumImageMapper.findByAlbumId(id);
            album.setImages(images);
        }
        return album;
    }

    @Override
    @Transactional
    public void update(Album album) {
        albumMapper.update(album);
        
        // 删除旧图片
        albumImageMapper.deleteByAlbumId(album.getId());
        
        // 添加新图片
        if (album.getImages() != null && !album.getImages().isEmpty()) {
            for (AlbumImage image : album.getImages()) {
                image.setAlbumId(album.getId());
                albumImageMapper.add(image);
            }
        }
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        albumImageMapper.deleteByAlbumId(id);
        albumMapper.delete(id);
    }
}


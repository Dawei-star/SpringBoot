package com.itheima.bigevent.controller;

import com.itheima.bigevent.anno.OpLog;
import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.pojo.ResultCode;
import com.itheima.bigevent.utils.AliOssUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
public class FileUploadController {

    @OpLog(module = "文件管理", operation = "上传文件")
    @PostMapping("/upload")
    public Result<String> upload(final MultipartFile file) throws IOException {
        final String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            return Result.error(ResultCode.INVALID_PARAMETER, "文件名无效");
        }
        final String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        final String filename = UUID.randomUUID().toString() + ext;
        final String url = AliOssUtil.uploadFile(filename, file.getInputStream());
        return Result.success(ResultCode.SUCCESS, "文件上传成功", url);
    }
}

package cn.itcast.controller;

import cn.itcast.common.OSSUtils;
import cn.itcast.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器，处理图片上传接口
 */
@RestController
public class UploadController {

    @Autowired
    private OSSUtils ossUtils;

    /**
     * 上传图片到OSS
     */
    @PostMapping("/upload")
    public Result upload(MultipartFile image) throws Exception {
        ossUtils.upload(image);
        return Result.success();
    }

}

package com.njupt.swg.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @Author swg.
 * @Date 2019/1/3 19:12
 * @CONTACT 317758022@qq.com
 * @DESC
 */
public interface IFileService {
    String upload(MultipartFile file, String path);
}

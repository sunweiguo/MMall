package com.njupt.swg.service;

import com.google.common.collect.Lists;
import com.njupt.swg.common.utils.FtpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * @Author swg.
 * @Date 2019/1/3 19:13
 * @CONTACT 317758022@qq.com
 * @DESC
 */
@Service
@Slf4j
public class FileServiceImpl implements IFileService{

    @Override
    public String upload(MultipartFile file, String path) {
        String fileName = file.getOriginalFilename();
        //扩展名
        //abc.jpg
        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".")+1);
        String uploadFileName = UUID.randomUUID().toString()+"."+fileExtensionName;
        log.info("开始上传文件,上传文件的文件名:{},上传的路径:{},新文件名:{}",fileName,path,uploadFileName);

        File fileDir = new File(path);
        if(!fileDir.exists()){
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }
        log.info("【文件上传路径为：{}】",fileDir);
        File targetFile = new File(path,uploadFileName);

        try {
            file.transferTo(targetFile);
            //文件已经上传成功了
            log.info("【文件上传本地服务器成功】");


            FtpUtil.uploadFile(Lists.newArrayList(targetFile));
            //已经上传到ftp服务器上
            log.info("【文件上传到文件服务器成功】");

            targetFile.delete();
            log.info("【删除本地文件】");
        } catch (IOException e) {
            log.error("上传文件异常",e);
            return null;
        }
        //A:abc.jpg
        //B:abc.jpg
        return targetFile.getName();
    }
}

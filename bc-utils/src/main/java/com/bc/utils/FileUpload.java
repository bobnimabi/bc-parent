package com.bc.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by mrt on 2019/4/29 0029 下午 7:24
 */
public class FileUpload {
    //上传文件
    public static String fileUploadOne(MultipartFile file, String upfilePath) {
        // 获得原始文件名+格式
        String fileName = file.getOriginalFilename();
        //截取文件名
        String fname = fileName.substring(0, fileName.lastIndexOf("."));
        //截取文件格式
        String format = fileName.substring(fileName.lastIndexOf(".") + 1);
        //获取当前时间(精确到毫秒)
        long MS = System.currentTimeMillis();
        String timeMS = String.valueOf(MS);
        //原文件名+当前时间戳作为新文件名
        String videoName = fname + "_" + timeMS + "." + format;
        String filelocalPath = "";
        char pathChar = upfilePath.charAt(upfilePath.length() - 1);
        Date date = new Date();
        String dateOne = new SimpleDateFormat("yyyy-MM-dd/").format(date);
        if (pathChar == '/') {
            filelocalPath = upfilePath + dateOne;
        } else {
            filelocalPath = upfilePath + "/" + dateOne;
        }
        File f = new File(filelocalPath);
        if (!f.exists())
            f.mkdirs();
        if (!file.isEmpty()) {
            try {
                FileOutputStream fos = new FileOutputStream(filelocalPath + videoName);
                InputStream in = file.getInputStream();
                //InputStream in = request.getInputStream();
                byte[] bytes = new byte[1024];
                int len = 0;
                while ((len = in.read(bytes)) != -1) {
                    fos.write(bytes, 0, len);
                }
                fos.close();
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "/"+dateOne+videoName;
    }

    //拼接完整路径
    public static String completePath(String relativePath,String upfilePath){
        char pathChar = upfilePath.charAt(upfilePath.length() - 1);
        if (pathChar == '/') {
            return upfilePath.substring(0,upfilePath.length() - 1) + relativePath;
        } else {
            return upfilePath + relativePath;
        }
    }
}

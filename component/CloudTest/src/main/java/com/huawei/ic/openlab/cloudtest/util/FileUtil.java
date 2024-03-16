/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2023. All rights reserved.
 */

package com.huawei.ic.openlab.cloudtest.util;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * FileUtil
 *
 * @author kongcaizhi
 * @since 2023-03-23
 */
@Slf4j
public class FileUtil {
    /**
     * fileToMultipartFile
     *
     * @param fileName fileName
     * @return MultipartFile
     */
    public static MultipartFile fileToMultipartFile(String fileName) {
        FileItem fileItem = createFileItem(new File(fileName));
        return new CommonsMultipartFile(fileItem);
    }

    private static FileItem createFileItem(File file) {
        FileItemFactory factory = new DiskFileItemFactory(16, null);
        FileItem item = factory.createItem("textField", "text/plain", true, file.getName());
        int bytesRead = 0;
        byte[] buffer = new byte[8192];
        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = item.getOutputStream()) {
            while ((bytesRead = fis.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            log.error("Exception in transforming File to multipartFile {}", e.getLocalizedMessage());
        }
        return item;
    }
}

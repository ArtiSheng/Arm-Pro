/*
 * Copyright (c) 2021. Armadillo
 */

package armadillo.studio.common.utils;


import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;

public class FileSize {
    @NotNull
    public static String getAutoFileOrFileSize(File file) {
        long blockSize = 0;
        try {
            blockSize = getFileSize(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return FormetFileSize(blockSize);
    }

    @NotNull
    public static String getAutoFileOrFileSize(String filePath) {
        if (filePath == null) return "文件读取异常";
        File file = new File(filePath);
        long blockSize = 0;
        try {
            blockSize = getFileSize(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return FormetFileSize(blockSize);
    }

    public static long getFileSize(@NotNull File file) {
        if (file.isDirectory())
            return 0;
        long size = 0;
        if (file.exists()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                size = fis.available();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            return 0;
        }
        return size;
    }

    @NotNull
    private static String FormetFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (fileS == 0) {
            return wrongSize;
        }
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }

}


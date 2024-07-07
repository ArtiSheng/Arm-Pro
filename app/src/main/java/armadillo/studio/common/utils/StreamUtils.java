/*
 * Copyright (c) 2021. Armadillo
 */

package armadillo.studio.common.utils;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamUtils {
    @NotNull
    public static byte[] toByte(@NotNull InputStream inputStream) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] bs = new byte[1024 * 1024 * 20];
        int len = 0;
        while((len = inputStream.read(bs)) != -1){
            os.write(bs,0,len);
            os.flush();
        }
        inputStream.close();
        os.close();
        return os.toByteArray();
    }

    public static int toSize(@NotNull FileInputStream inputStream) throws IOException {
        return inputStream.available();
    }

    @NotNull
    public static byte[] ReadZipEntry(@NotNull InputStream inputStream) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] bs = new byte[1024 * 1024 * 20];
        int len = 0;
        while((len = inputStream.read(bs)) != -1){
            os.write(bs,0,len);
            os.flush();
        }
        os.close();
        return os.toByteArray();
    }
}

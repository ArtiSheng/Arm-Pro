/*
 * Copyright (c) 2021. Armadillo
 */

package armadillo.studio.common.utils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import armadillo.studio.common.base.callback.DowCallBack;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadUtil {
    private static DownloadUtil downloadUtil;
    private final OkHttpClient okHttpClient;

    public static DownloadUtil get() {
        if (downloadUtil == null) {
            downloadUtil = new DownloadUtil();
        }
        return downloadUtil;
    }

    private DownloadUtil() {
        okHttpClient = new OkHttpClient();
    }

    public void download(final String url, File out, final DowCallBack listener) {
        Request request = new Request.Builder().url(url).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                listener.onFail(e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                try {
                    FileOutputStream fos = new FileOutputStream(out);
                    InputStream is = Objects.requireNonNull(response.body()).byteStream();
                    long total = Objects.requireNonNull(response.body()).contentLength();
                    byte[] buf = new byte[2048];
                    int len = 0;
                    long sum = 0;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        int progress = (int) (sum * 1.0f / total * 100);
                        listener.onProgress(progress);
                    }
                    fos.flush();
                    fos.close();
                    listener.onFinish(null);
                } catch (Exception e) {
                    e.printStackTrace();
                    listener.onFail(e.getMessage());
                }
            }
        });
    }

}

/*
 * Copyright (c) 2021. Armadillo
 */

package armadillo.studio.common.utils;

import android.annotation.SuppressLint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class TimeUtils {
    @SuppressLint("SimpleDateFormat")
    public static long strtolong(String time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return Objects.requireNonNull(simpleDateFormat.parse(time)).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return System.currentTimeMillis();
    }

    public static long strtolong(long time) {
        return new Date(time).getTime();
    }
}

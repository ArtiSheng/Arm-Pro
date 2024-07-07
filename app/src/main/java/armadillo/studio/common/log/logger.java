/*
 * Copyright (c) 2021. Armadillo
 */

package armadillo.studio.common.log;

import android.util.Log;

import armadillo.studio.BuildConfig;

public class logger {
    private final static String TAG = logger.class.getSimpleName();
    private final static boolean debug = BuildConfig.DEBUG;

    public static void e(String msg) {
        if (debug)
            Log.e(TAG, msg);
    }

    public static void d(String msg) {
        if (debug)
            Log.d(TAG, msg);
    }

    public static void i(String msg) {
        if (debug)
            Log.i(TAG, msg);
    }
}

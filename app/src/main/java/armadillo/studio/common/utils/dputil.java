/*
 * Copyright (c) 2021. Armadillo
 */

package armadillo.studio.common.utils;

import android.content.Context;
import android.util.TypedValue;

import org.jetbrains.annotations.NotNull;

public class dputil {
    public static int dip2px(@NotNull Context context, float dipValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, context.getResources().getDisplayMetrics());
    }

    public static int sp2px(@NotNull Context context, float spValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, context.getResources().getDisplayMetrics());
    }
}

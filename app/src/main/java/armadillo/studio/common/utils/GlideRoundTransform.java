/*
 * Copyright (c) 2021. Armadillo
 */

package armadillo.studio.common.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.security.MessageDigest;

public class GlideRoundTransform extends BitmapTransformation {
    private static final String ID = GlideRoundTransform.class.getName();
    private static final byte[] ID_BYTES = ID.getBytes(CHARSET);

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        int size = Math.min(toTransform.getWidth(), toTransform.getHeight());
        int x = (toTransform.getWidth() - size) / 2;
        int y = (toTransform.getHeight() - size) / 2;

        Bitmap square = Bitmap.createBitmap(toTransform, x, y, size, size);
        Bitmap circle = pool.get(size, size, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(circle);
        Paint paint = new Paint();
        paint.setShader(new BitmapShader(square, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
        paint.setAntiAlias(true);
        float r = size / 2f;
        canvas.drawCircle(r, r, r, paint);
        return circle;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof GlideRoundTransform;
    }

    @Override
    public int hashCode() {
        return ID.hashCode();
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update(ID_BYTES);
    }
}
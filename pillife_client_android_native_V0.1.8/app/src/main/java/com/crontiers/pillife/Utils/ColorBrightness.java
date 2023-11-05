package com.crontiers.pillife.Utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

import com.squareup.picasso.Transformation;

public class ColorBrightness implements Transformation {

    float contrast = 1;
    float brightness = -80;
    /**
     * @param source input bitmap
     * param contrast 0..10 1 is default
     * param brightness -255..255 0 is default
     * @return new bitmap
     */
    @Override
    public Bitmap transform(Bitmap source) {
        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        contrast, 0, 0, 0, brightness,
                        0, contrast, 0, 0, brightness,
                        0, 0, contrast, 0, brightness,
                        0, 0, 0, 1, 0
                });

        Bitmap ret = Bitmap.createBitmap(source.getWidth(), source.getHeight(), source.getConfig());

        Canvas canvas = new Canvas(ret);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(source, 0, 0, paint);
        source.recycle();
        return ret;
    }

    @Override
    public String key() {
        return "ColorBrightness()";
    }
}

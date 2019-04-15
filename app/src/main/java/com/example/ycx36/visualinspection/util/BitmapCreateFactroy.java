package com.example.ycx36.visualinspection.util;

import android.graphics.Bitmap;

public class BitmapCreateFactroy {

    /**
     * Use color create a bitmap
     *
     * @param values byte array
     * @param picW
     *            bitmap width
     * @param picH
     *            bitmap height
     * @return Bitmap
     */
    public static Bitmap createBitmap(byte[] values, int picW, int picH) {
        if(values == null || picW <= 0 || picH <= 0)
            return null;
        //使用8位来保存图片
        Bitmap bitmap = Bitmap
                .createBitmap(picW, picH, Bitmap.Config.ARGB_8888);
        int pixels[] = new int[picW * picH];
        for (int i = 0; i < pixels.length; ++i) {
            //关键代码，生产灰度图
            pixels[i] = values[i] * 256 * 256 + values[i] * 256 + values[i] + 0xFF000000;
        }
        bitmap.setPixels(pixels, 0, picW, 0, 0, picW, picH);
        values = null;
        pixels = null;
        return bitmap;
    }

}

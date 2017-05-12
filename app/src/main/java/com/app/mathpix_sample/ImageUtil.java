package com.app.mathpix_sample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Base64;

import java.io.ByteArrayOutputStream;


public class ImageUtil {
    private static byte[] bitmapToBytes(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    public static String bitmapToBase64(Bitmap image) {
        byte[] buffer = bitmapToBytes(image);
        return Base64.encodeToString(buffer, Base64.DEFAULT);
    }

    public static Bitmap toBitmap(byte[] data) {
        return BitmapFactory.decodeByteArray(data , 0, data.length);
    }

    public static Bitmap rotate(Bitmap in, int angle) {
        Matrix mat = new Matrix();
        mat.postRotate(angle);
        return Bitmap.createBitmap(in, 0, 0, in.getWidth(), in.getHeight(), mat, true);
    }
}

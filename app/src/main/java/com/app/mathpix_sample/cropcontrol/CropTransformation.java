package com.app.mathpix_sample.cropcontrol;

import android.graphics.Bitmap;
import android.util.Log;

import com.squareup.picasso.Transformation;

public class CropTransformation implements Transformation {

    final int screenWidth;
    final int screenHeight;

    final int width;
    final int height;

    public CropTransformation(int screenWidth, int screenHeight, int width, int height) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.width = width;
        this.height = height;
    }

    @Override
    public Bitmap transform(Bitmap source) {

        Log.e("BITMAP", "Source Width :" + source.getWidth() + "Source Height: " + source.getHeight());
        Log.e("BITMAP", "Screen Width :" + screenWidth + "Screen Height: " + screenHeight);
        float scaleX = (float)source.getWidth() / screenWidth;

        int resultWidth = (int) (width * scaleX);
        int resultHeight = (int) (height * scaleX);

        int resultX = source.getWidth()/2 - resultWidth /2;
        int resultY = source.getHeight()/2 - resultHeight /2;
        Bitmap result = Bitmap.createBitmap(source, resultX, resultY, resultWidth, resultHeight);
        if (result != source) {
            source.recycle();
        }
        return result;
    }

    @Override
    public String key() { return "crop()"; }
}
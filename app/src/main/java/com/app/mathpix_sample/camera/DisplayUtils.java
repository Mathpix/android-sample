package com.app.mathpix_sample.camera;

import android.content.Context;
import android.content.res.Configuration;
import android.view.Display;
import android.view.WindowManager;

public class DisplayUtils {

    public static int getScreenOrientation(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int orientation = Configuration.ORIENTATION_UNDEFINED;
        if (display.getWidth() == display.getHeight()) {
            orientation = Configuration.ORIENTATION_SQUARE;
        } else {
            if (display.getWidth() < display.getHeight()) {
                orientation = Configuration.ORIENTATION_PORTRAIT;
            } else {
                orientation = Configuration.ORIENTATION_LANDSCAPE;
            }
        }
        return orientation;
    }

}
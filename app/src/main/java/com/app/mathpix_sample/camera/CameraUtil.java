package com.app.mathpix_sample.camera;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import java.util.List;

/**
 * Created by admin on 5/11/17.
 */

public class CameraUtil {
    public static Camera getCameraInstance() {
        Camera camera = null;
        int numCams = Camera.getNumberOfCameras();
        if (numCams > 0) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            try {
                for (int i = 0; i < numCams; i++) {
                    Camera.getCameraInfo(i, info);
                    if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                        camera = Camera.open(i);
                        camera.setDisplayOrientation(90);
                        // also set the camera's output orientation
                        Camera.Parameters params = camera.getParameters();
                        params.setRotation(90);
                        camera.setParameters(params);
                        break;
                    }
                }
            } catch (RuntimeException ex) {
                ex.printStackTrace();
            }
        }

        return camera;
    }


    public static int getCameraOrientation(Context context) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        int rotation = display.getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        return (info.orientation - degrees + 360) % 360;
    }

    public static Camera.Size getOptimalPreviewSize(Context context, Camera camera, int w, int h) {
        if (camera == null) {
            return null;
        }

        List<Camera.Size> sizes = camera.getParameters().getSupportedPreviewSizes();
        if (DisplayUtils.getScreenOrientation(context) == Configuration.ORIENTATION_PORTRAIT) {
            int portraitWidth = h;
            h = w;
            w = portraitWidth;
        }

        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }
}

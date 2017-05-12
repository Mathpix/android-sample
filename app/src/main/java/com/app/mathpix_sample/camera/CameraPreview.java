package com.app.mathpix_sample.camera;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

import static android.hardware.Camera.Parameters.FOCUS_MODE_AUTO;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = CameraPreview.class.getSimpleName();
    boolean isFocusing = false;
    private Context mContext;
    private Camera mCamera;
    private SurfaceHolder mHolder;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;
        mContext = context;
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        //Setup Camera Parameters
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setFocusMode(FOCUS_MODE_AUTO);
        parameters.setPictureFormat(PixelFormat.JPEG);
        mCamera.setParameters(parameters);

        isFocusing = false;
    }


    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (mHolder.getSurface() == null) {
            return;
        }
        try {
            mCamera.stopPreview();
            mCamera.setPreviewDisplay(mHolder);

            Camera.Parameters parameters = mCamera.getParameters();
            Camera.Size optimalSize = CameraUtil.getOptimalPreviewSize(mContext, mCamera, w, h);
            parameters.setPreviewSize(optimalSize.width, optimalSize.height);
            parameters.setPictureSize(optimalSize.width, optimalSize.height);
            mCamera.setParameters(parameters);
            mCamera.setDisplayOrientation(CameraUtil.getCameraOrientation(mContext));
            mCamera.startPreview();

            isFocusing = false;
        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    public void autoFocus() {
        if (!isFocusing) {
            isFocusing = true;
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    isFocusing = false;
                }
            });
        }
    }
}

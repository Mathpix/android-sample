package com.app.mathpix_sample.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class PhotoTaker {

    private static final String TAG = PhotoTaker.class.getSimpleName();
    private PhotoTakenListener listener;
    private Bitmap bitmap;

    public void takePhoto(Camera camera, Context context, final PhotoTakenListener listener) {
        this.listener = listener;
        final String filePath = dateName("jpg", context);
        camera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                new SaveImageTask(filePath).execute(data);
            }
        });
    }

    private static String dateName(String extension, Context context) {
        File files = context.getExternalFilesDir(Environment.DIRECTORY_DCIM);
        File picFile = new File(files, sdf.format(new Date()).concat(".").concat(extension));
        return picFile.getAbsolutePath();
    }

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US);
    static {
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public interface PhotoTakenListener {
        void photoTaken(String filePath);
    }

    private class SaveImageTask extends AsyncTask<byte[], Void, Void> {

        final String filePath;
        private SaveImageTask(String filePath) {
            this.filePath = filePath;
        }

        @Override
        protected Void doInBackground(byte[]... data) {
            FileOutputStream outStream = null;
            try {
                File outFile = new File(filePath);
                outStream = new FileOutputStream(outFile);
                outStream.write(data[0]);
                outStream.flush();
                outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            boolean success = new File(filePath).exists();
            Log.i(TAG, "Saved photo success: " + Boolean.toString(success));
            listener.photoTaken(filePath);
        }
    }
}

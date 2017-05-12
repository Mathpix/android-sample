package com.app.mathpix_sample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.mathpix_sample.camera.CameraPreview;
import com.app.mathpix_sample.camera.CameraUtil;
import com.app.mathpix_sample.cropcontrol.CropController;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CameraFragment extends Fragment {
    private static final String TAG = CameraFragment.class.getSimpleName();

    //region UI
    @BindView(R.id.takePhotoButton)
    ImageView mTakePhotoButton;
    @BindView(R.id.crop_status_text_view)
    TextView mCropStatusTextView;
    @BindView(R.id.crop_control)
    RelativeLayout mCropControl;
    @BindView(R.id.image_view)
    ImageView mCropImageView;
    @BindView(R.id.camera_preview)
    FrameLayout mCameraPreview;
    @BindView(R.id.view_scan_line)
    View mScanLine;
    @BindView(R.id.camera_snapshot)
    ImageView mCameraSnapshot;

    @BindView(R.id.webview_container)
    ViewGroup mWebViewContainer;

    @BindView(R.id.webView)
    WebView mWebView;

    @OnClick(R.id.nextPhoto)
    void onNextPhotoClicked() {
        if(mWebViewContainer.getVisibility() == View.VISIBLE) {
            mWebViewContainer.setVisibility(View.INVISIBLE);


            mTakePhotoButton.setEnabled(true);
            startPreview();
        }
    }

    @OnClick(R.id.crop_control)
    void onTapCropView() {
        if(mPreview != null) {
            try {
                Log.e(TAG, "Start auto-focusing");
                mPreview.autoFocus();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @OnClick(R.id.takePhotoButton)
    void onTakePhotoButtonClicked() {
        if (mCamera == null) return;

        mCropStatusTextView.setText(R.string.taking_picture);
        Log.e(TAG, "Taking picture");
        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(final byte[] data, Camera camera) {
                stopPreview();

                Bitmap bm= ImageUtil.toBitmap(data);
                if(bm.getWidth() > bm.getHeight()) {
                    bm = ImageUtil.rotate(bm, 90);
                }
                Log.e(TAG, "got bitmap size = " + bm.getWidth() + ", " + bm.getHeight());

                Rect cropFrame = new Rect(mCropControl.getLeft(), mCropControl.getTop(), mCropControl.getRight(), mCropControl.getBottom());

                int inset = (int) getResources().getDimension(R.dimen.crop_corner_width_halved);
                int viewWidth = getView().getWidth();
                int viewHeight = getView().getHeight();
                int cropWidth = cropFrame.width() - inset * 2;
                int cropHeight = cropFrame.height() - inset * 2;

                int centerX = bm.getWidth() / 2;
                int centerY = bm.getHeight() / 2;
                int targetWidth = (cropWidth * bm.getWidth()) / viewWidth;
                int targetHeight = (cropHeight * bm.getHeight()) / viewHeight;

                Log.e(TAG, "screen size = " + viewWidth + ", " + viewHeight);
                Log.e(TAG, "target size = " + targetWidth + ", " + targetHeight);

                Bitmap result = Bitmap.createBitmap(bm, centerX - targetWidth / 2, centerY - targetHeight / 2, targetWidth, targetHeight);
                result = Bitmap.createScaledBitmap(result, targetWidth / 2, targetHeight / 2, false);

                imageCropped(result);

                //region avoid black camera issue - create snapshot and load to imageview overlay
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US);

                        File files = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                        File outFile = new File(files, sdf.format(new Date()).concat(".").concat("jpg"));
                        FileOutputStream outStream = null;
                        try {
                            outStream = new FileOutputStream(outFile);
                            outStream.write(data);
                            outStream.flush();
                            outStream.close();
                            Log.e(TAG, "image saved to file " + outFile.getAbsolutePath());

                            Picasso.with(getContext()).load(outFile.getAbsolutePath()).into(mCameraSnapshot);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                //endregion
            }
        });

        mTakePhotoButton.setEnabled(false);
    }
    //endregion


    private CropController mCropController;
    Camera mCamera;
    private CameraPreview mPreview;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_camera, container, false);
        ButterKnife.bind(this, rootView);

        setupCropControl(rootView);
        return rootView;
    }

    //region Camera Preview
    @Override
    public void onResume() {
        super.onResume();
        startPreview();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopPreview();
    }

    private void startPreview() {
        mCropImageView.setImageBitmap(null);
        mCameraSnapshot.setImageBitmap(null);
        resetDragControl();

        if (mCamera != null) {
            try {
                mCamera.startPreview();
                return;
            } catch (Exception e) {
//                e.printStackTrace();
                //jump to below
            }
        }

        try {
            mCamera = CameraUtil.getCameraInstance();

            if(mCamera == null) {
//                showAlert("Can not connect to camera.");
            } else {
                mPreview = new CameraPreview(getContext(), mCamera);
                mCameraPreview.removeAllViews();
                mCameraPreview.addView(mPreview);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopPreview() {
        if (mCamera != null) {
            try {
                mCamera.setPreviewCallback(null);
                mPreview.getHolder().removeCallback(mPreview);
                mCamera.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    //endregion

    private void imageCropped(final Bitmap bitmap) {
//        mCropImageView.setImageBitmap(bitmap);
        mCropStatusTextView.setText(R.string.processing_image);
        uploadImage(bitmap);
    }

    private void uploadImage(Bitmap bitmap) {
        startScanAnimation();


        UUID deviceUID = MathpixUUID.uuid(getContext());
        UploadImageTask.UploadParams params = new UploadImageTask.UploadParams(bitmap,deviceUID);
        UploadImageTask task = new UploadImageTask(new UploadImageTask.ResultListener() {
            @Override
            public void onError(String message) {
                stopScanAnimation();
                showErrorAndReset(message);
                mTakePhotoButton.setEnabled(true);
            }

            @Override
            public void onSuccess(String latex) {
                stopScanAnimation();
                mLatestLatex = latex;
                Log.d("Latex_NEW", mLatestLatex);
                loadLocalContent();
            }
        });
        task.execute(params);
    }

    private String mLatestLatex;

    private void loadLocalContent() {
        mWebViewContainer.setVisibility(View.VISIBLE);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                final String js = "javascript:setResultJson(" + mLatestLatex + ")";
                if (mWebView != null) {
                    mWebView.loadUrl(js);
                }
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return true;
            }
        });
        WebSettings settings = mWebView.getSettings();
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        if (Build.VERSION.SDK_INT >= 16) {
            settings.setAllowFileAccessFromFileURLs(true);
            settings.setAllowUniversalAccessFromFileURLs(true);
        }
        String localURL = "file:///android_asset/";
        String htmlString = localHTML(getContext());
        mWebView.loadDataWithBaseURL(localURL, htmlString, "text/html", "UTF-8", null);

    }

    public String localHTML(Context context){
        StringBuilder stringBuilder = new StringBuilder();
        InputStream json;
        try {
            json = context.getAssets().open("latex.html");
            BufferedReader in = new BufferedReader(new InputStreamReader(json));
            String str;

            while ((str = in.readLine()) != null) {
                stringBuilder.append(str);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    private void showErrorAndReset(String errMessage) {
        Toast.makeText(getContext(), errMessage, Toast.LENGTH_LONG).show();
        startPreview();
        resetDragControl();
    }

    //region CropControl
    public void setupCropControl(View view) {
        mCropStatusTextView.setText(R.string.start_dragging_crop);
        mCropController = new CropController(mCropControl, new CropController.TouchStateListener() {
            @Override
            public void onDragBegan() {
                mCropStatusTextView.setText(R.string.release_to_take_photo);
            }

            @Override
            public void onDragEnded() {
            }
        });
    }

    public void resetDragControl() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mCropImageView.setImageBitmap(null);
                mCropStatusTextView.setText(R.string.start_dragging_crop);
                ViewGroup.LayoutParams layoutParams = mCropControl.getLayoutParams();
                layoutParams.width = (int) getResources().getDimension(R.dimen.crop_control_width);
                layoutParams.height = (int) getResources().getDimension(R.dimen.crop_control_height);
                mCropControl.setLayoutParams(layoutParams);
            }
        }, 500);

        mTakePhotoButton.setEnabled(true);

        stopScanAnimation();
    }

    private void startScanAnimation() {
        int inset = (int) getResources().getDimension(R.dimen.crop_corner_width_halved);
        Rect cropFrame = new Rect(mCropControl.getLeft(), mCropControl.getTop(), mCropControl.getRight(), mCropControl.getBottom());
        final TranslateAnimation animation = new TranslateAnimation(inset, cropFrame.width() - inset, 0, 0);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(1000);
        animation.setRepeatMode(Animation.REVERSE);
        animation.setDuration(1000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mScanLine.setVisibility(View.VISIBLE);
                mScanLine.startAnimation(animation);
            }
        }, 100);

    }

    private void stopScanAnimation() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mScanLine.setAnimation(null);
                mScanLine.setVisibility(View.GONE);
            }
        }, 500);
    }
    //endregion
}

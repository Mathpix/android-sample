package com.app.mathpix_sample;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.app.mathpix_sample.camera.CameraPreview;
import com.app.mathpix_sample.camera.PhotoTaker;
import com.app.mathpix_sample.cropcontrol.CropController;
import com.app.mathpix_sample.cropcontrol.CropTransformation;
import com.squareup.otto.Produce;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

public class CameraFragment extends Fragment {
    public static final String TAG = CameraFragment.class.getSimpleName();
    private boolean hasSubmitted = false;
    private UUID deviceUID;
    private Button takePhotoButton;
    private TextView cropStatusTextView;
    private CameraPreview preview;
    private PhotoTaker photoTaker;
    private RelativeLayout cropControl;
    private ImageView cropImageView;
    private RelativeLayout cameraLayout;
    private CropController cropController;
    private RelativeLayout webview_container;
    private WebView webView;
    private Button nextPhoto;
    private String latestLatex = "";
    Camera camera;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        deviceUID = MathpixUUID.uuid(getContext());
        webview_container=(RelativeLayout) view.findViewById(R.id.webview_container);
        webView = (WebView) view.findViewById(R.id.webView);
        setupButton(view);
        setupCropControl(view);
        setupNewPhotoButton(view);
        if (!MarshmallowPermissions.checkPermissionForCamera(getActivity())) {
            MarshmallowPermissions.requestPermissionForCamera(getActivity());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    }

    @Override
    public void onResume() {
        super.onResume();
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
                        setupPreview((RelativeLayout) getView());
                        break;
                    }
                }
            } catch (RuntimeException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void onPause() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
        FrameLayout cameraContainer = (FrameLayout) getView().findViewById(R.id.camera_container);
        cameraContainer.removeView(cameraLayout);
        super.onPause();
    }

    private void setupNewPhotoButton(View view) {
        nextPhoto= (Button) view.findViewById(R.id.nextPhoto);
        nextPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webview_container.setVisibility(View.INVISIBLE);
                hasSubmitted = false;
                resetDragControl();
                cropControl.setVisibility(View.VISIBLE);
                takePhotoButton.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setupButton(View view) {
        takePhotoButton = (Button) view.findViewById(R.id.takePhotoButton);
        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });
    }

    private void setupPreview(RelativeLayout view) {
        preview = new CameraPreview(getContext(), camera);

        cameraLayout = new RelativeLayout(getContext());
        cameraLayout.setGravity(Gravity.CENTER);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        cameraLayout.setLayoutParams(params);
        cameraLayout.setBackgroundColor(Color.BLACK);
        cameraLayout.addView(preview);
        FrameLayout cameraContainer = (FrameLayout) view.findViewById(R.id.camera_container);
        cameraContainer.setBackgroundColor(getResources().getColor(R.color.black));
        cameraContainer.addView(cameraLayout);
        view.bringChildToFront(view.findViewById(R.id.crop_control_container));
    }

    public void setupCropControl(View view) {
        cropImageView = (ImageView) view.findViewById(R.id.image_view);
        cropStatusTextView = (TextView) view.findViewById(R.id.crop_status_text_view);
        cropStatusTextView.setText(R.string.start_dragging_crop);
        cropControl = (RelativeLayout) view.findViewById(R.id.crop_control);
        cropController = new CropController(cropControl, new CropController.TouchStateListener() {
            @Override
            public void onDragBegan() {
                cropStatusTextView.setText(R.string.release_to_take_photo);
            }
            @Override
            public void onDragEnded() {}
        });
    }

    public void takePhoto() {
        cropStatusTextView.setText(R.string.taking_picture);
        photoTaker = new PhotoTaker();
        photoTaker.takePhoto(camera, getContext(), new PhotoTaker.PhotoTakenListener() {
            @Override
            public void photoTaken(String filePath) {
                displayImage(filePath);
            }
        });
    }

    private void displayImage(String filePath) {
        Rect cropFrame = new Rect(cropControl.getLeft(), cropControl.getTop(), cropControl.getRight(), cropControl.getBottom());
        int inset = (int) getResources().getDimension(R.dimen.crop_corner_width_halved);
        cropFrame.inset(inset, inset);

        int width = getView().getWidth();
        int height = getView().getHeight();
        final CropTransformation transformation = new CropTransformation(
                width,
                height,
                cropFrame.width(),
                cropFrame.height());
        File file = new File(filePath);
        Picasso.with(getContext()).invalidate(file);
        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                imageCropped(bitmap);
                restartCamera();
            }
            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                restartCamera();
                try {
                    Log.e(TAG, "Error : " + errorDrawable.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                Log.e(TAG, "Prepare ");
            }
        };
        cropImageView.setTag(target);
        Picasso.with(getContext())
                .load(file)
                .transform(transformation)
                .into(target);
    }

    void restartCamera(){
        camera.startPreview();
        takePhotoButton.setEnabled(true);
        cropControl.setEnabled(true);
    }

    private void imageCropped(final Bitmap bitmap) {
        cropImageView.setImageBitmap(bitmap);
        submitPhoto(bitmap);
    }

    private void submitPhoto(Bitmap bitmap) {
        hasSubmitted = true;
        cropStatusTextView.setText(R.string.processing_image);
        uploadImage(bitmap);
    }

    private void uploadImage(Bitmap bitmap) {
        UploadImageTask.UploadParams params = new UploadImageTask.UploadParams(bitmap,deviceUID);
        UploadImageTask task = new UploadImageTask(new UploadImageTask.ResultListener() {
            @Override
            public void onError(String message) {
                receiveResultsError(message);
                resetDragControl();
            }

            @Override
            public void onSuccess(String latex) {
                latestLatex = latex;
                Log.d("Latex_NEW", latestLatex);
                cropControl.setVisibility(View.GONE);
                takePhotoButton.setVisibility(View.GONE);
                loadLocalContent();
            }
        });
        task.execute(params);
    }

    private void loadLocalContent() {
        webview_container.setVisibility(View.VISIBLE);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                final String js = "javascript:setResultJson(" + latestLatex + ")";
                if (webView != null) {
                    webView.loadUrl(js);
                }
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return true;
            }
        });
        WebSettings settings = webView.getSettings();
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        if (Build.VERSION.SDK_INT >= 16) {
            settings.setAllowFileAccessFromFileURLs(true);
            settings.setAllowUniversalAccessFromFileURLs(true);
        }
        String localURL = "file:///android_asset/";
        String htmlString = localHTML(getContext());
        webView.loadDataWithBaseURL(localURL, htmlString, "text/html", "UTF-8", null);

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

    private void resetDragControl() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                cropImageView.setImageBitmap(null);
                cropStatusTextView.setText(R.string.start_dragging_crop);

                ViewGroup.LayoutParams layoutParams = cropControl.getLayoutParams();
                layoutParams.width = (int) getResources().getDimension(R.dimen.crop_control_width);
                layoutParams.height = (int) getResources().getDimension(R.dimen.crop_control_height);
                cropControl.setLayoutParams(layoutParams);
            }
        }, 500);
    }

    public void receiveResultsError(String message) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Error")
                .setMessage(message)
                .setCancelable(true);

        AlertDialog alert = builder.create();
        alert.show();
    }

}

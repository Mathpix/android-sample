package com.app.mathpix_sample;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class UploadImageTask extends AsyncTask<UploadImageTask.UploadParams, Void, UploadImageTask.Result> {

    private final ResultListener listener;
    public UploadImageTask(ResultListener listener) {
        this.listener = listener;
    }

    interface ResultListener{
        void onError(String message);
        void onSuccess(String url);
    }

    private static final String FORM_BOUNDARY = "********";
    private static final String DASHES = "--";
    private static final String CLRF = "\r\n";
    private static final String FORM_NAME = "file";
    private static final String FORM_FILENAME = "image.jpg";


    public static class UploadParams {
        private Bitmap image;
        private UUID deviceUID;

        public UploadParams(Bitmap image, UUID deviceUID) {
            this.image = image;
            this.deviceUID = deviceUID;
        }
    }

    public static class Result {
    }

    public static class ResultSuccessful extends Result {
        private String resultURL;
        protected String latex;
    }

    public static class ResultFailed extends Result {
        private Exception exception;
        public String message;
    }

    @Override
    protected Result doInBackground(UploadParams... arr) {

        UploadParams params = arr[0];

        Result result;
        try {
            URL url = new URL(Constant.base_Url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(7000);
            conn.setRequestMethod("POST");

            setupAndDoUpload(conn, params);
            String response = getResponse(conn);
            Log.d("Image Upload", response);

            JSONObject jsonResponse = new JSONObject(response);
            conn.disconnect();

            ResultFailed failed = new ResultFailed();
            failed.message = "Math not found";

            if (jsonResponse.has("latex")){
                String latex = jsonResponse.getString("latex");
                if (latex.length() > 0){
                    ResultSuccessful successful = new ResultSuccessful();
                    successful.resultURL = response;
                    successful.latex = latex;
                    result = successful;
                }else{
                    result = failed;
                }
            }else{
                result = failed;
            }

        } catch (Exception e) {
            ResultFailed failed = new ResultFailed();
            failed.exception = e;
            failed.message = "Failed to send to server. Check your connection and try again";
            result = failed;
        }
        return result;
    }

    private void setupAndDoUpload(HttpURLConnection conn, UploadParams params) throws IOException {
        conn.setRequestProperty("DeviceId", params.deviceUID.toString());
        conn.setRequestProperty("app_id",Constant.app_id);
        conn.setRequestProperty("app_key",Constant.app_key);
        String contentType = "multipart/form-data; boundary=" + FORM_BOUNDARY;
        conn.setRequestProperty("Content-Type", contentType);
        DataOutputStream body = new DataOutputStream(conn.getOutputStream());
        body.writeBytes(DASHES + FORM_BOUNDARY + CLRF);
        String bytes = "Content-Disposition: form-data; name=\"" + FORM_NAME + "\"; filename=\"" +
                FORM_FILENAME + "\"" + CLRF;
        body.writeBytes(bytes);
        body.writeBytes("Content-Type: image/jpeg" + CLRF + CLRF);
        body.write(bitmapToBytes(params.image));
        body.writeBytes(CLRF);
        body.writeBytes(DASHES + FORM_BOUNDARY + DASHES + CLRF);
        body.flush();
        body.close();
    }

    // Convert to JPEG and return byte array
    private byte[] bitmapToBytes(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    private String getResponse(HttpURLConnection conn) throws IOException {
        InputStream responseStream = new
                BufferedInputStream(conn.getInputStream());

        BufferedReader responseStreamReader =
                new BufferedReader(new InputStreamReader(responseStream));

        String line;
        StringBuilder stringBuilder = new StringBuilder();

        while ((line = responseStreamReader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
        responseStreamReader.close();

        return stringBuilder.toString();
    }


    @Override
    protected void onPostExecute(Result result) {
        if (result instanceof ResultSuccessful) {
            ResultSuccessful successful = (ResultSuccessful) result;
            listener.onSuccess(successful.resultURL);
        } else if (result instanceof ResultFailed) {
            ResultFailed failed = (ResultFailed) result;
            listener.onError(failed.message);
        }
    }
}

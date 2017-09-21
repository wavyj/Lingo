package com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;

import com.fullsail.dvp6.jc.colemanjustin_dvp6project.R;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.ImageSource;
import com.google.cloud.Identity;
import com.google.inject.util.Types;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageAnalyzeUtil {
    private static final String TAG = "ImageAnalyzeUtil";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
    private static String API_KEY = "";

    public static void setup(Context context, Uri path){
        API_KEY = context.getString(R.string.apiKey);
        cloudVision(path, context);
    }

    private static void cloudVision(final Uri path, final Context context){
        // Call api via async task and retrieve response

        new AsyncTask<Object, Void, String>(){
            @Override
            protected String doInBackground(Object... objects) {
                List<AnnotateImageRequest> requests = new ArrayList<>();

                ImageSource imgSource = new ImageSource().setGcsImageUri(path.getPath());
                Image img = new Image().setSource(imgSource);
                Feature feature = new Feature().setType("Text Detection");
                List<Feature> features = new ArrayList<>();
                features.add(feature);
                AnnotateImageRequest request = new AnnotateImageRequest().setFeatures(features);
                requests.add(request);

                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
            }
        };
    }
}

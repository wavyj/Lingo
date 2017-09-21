package com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

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
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
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

    public static void setup(Context context, String path){
        API_KEY = context.getString(R.string.apiKey);
        cloudVision(path, context);
        Log.d(TAG, "Starting...");
    }

    private static void cloudVision(final String path, final Context context){
        // Call api via async task and retrieve response

        new AsyncTask<Object, Void, List<EntityAnnotation>>(){
            @Override
            protected List<EntityAnnotation> doInBackground(Object... objects) {
                Log.d(TAG, "Analyze Method");
                try {
                VisionRequestInitializer requestInitializer = new VisionRequestInitializer(API_KEY);
                BatchAnnotateImagesRequest batchAnnotateImagesRequest = new BatchAnnotateImagesRequest();

                List<AnnotateImageRequest> requests = new ArrayList<>();

                Log.d(TAG, "Setting up");

                // Set source uri for image
                ImageSource imgSource = new ImageSource().setGcsImageUri(path);
                Image img = new Image().setSource(imgSource);
                // Set Text Detection
                Feature feature = new Feature().setType("TEXT_DETECTION");
                List<Feature> features = new ArrayList<>();
                features.add(feature);
                // Add feature and image to request
                AnnotateImageRequest request = new AnnotateImageRequest().setFeatures(features).setImage(img);
                requests.add(request);

                // Add request list to batch annotate request
                batchAnnotateImagesRequest.setRequests(requests);


                    // Execute Image annotate request
                    Vision.Images.Annotate annotateRequest = new Vision.Builder(
                            AndroidHttp.newCompatibleTransport(), GsonFactory.getDefaultInstance(), null
                    ).setApplicationName(context.getString(R.string.app_name)).
                            setVisionRequestInitializer(requestInitializer).build().images().annotate
                            (batchAnnotateImagesRequest);
                    annotateRequest.setDisableGZipContent(true);

                    Log.d(TAG, "Getting Response");

                    // Receive response
                    BatchAnnotateImagesResponse response =  annotateRequest.execute();
                    Log.d(TAG, response.getResponses().toString());
                    if (response.getResponses().get(0).getTextAnnotations() != null){
                        return response.getResponses().get(0).getTextAnnotations();
                    }else {
                        return null;
                    }
                } catch (GoogleJsonResponseException e){
                    e.printStackTrace();
                    return null;
                } catch (IOException e){
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(List<EntityAnnotation> response) {
                super.onPostExecute(response);

                if (response != null ) {
                    for (EntityAnnotation res : response) {
                        Log.d(TAG, res.getDescription());
                    }
                }else {
                    Log.d(TAG, "EMPTY");
                }
            }
        }.execute();
    }
}

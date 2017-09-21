package com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.fullsail.dvp6.jc.colemanjustin_dvp6project.R;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.main.BaseApplication;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageAnalyzeUtil{
    private static final String TAG = "ImageAnalyzeUtil";

    private static onDetectComplete mOnDetectComplete;
    private static ImageMessage mImage;
    private static String API_KEY = "";

    public interface onDetectComplete{
        void detectionComplete(String text);
    }

    public static void setup(Context context, String path, onDetectComplete detectComplete, ImageMessage m){
        API_KEY = context.getString(R.string.apiKey);
        mOnDetectComplete = detectComplete;
        mImage = m;

        cloudVision(path, context);
        Log.d(TAG, "Starting...");
    }

    private static void cloudVision(final String path, final Context context){
        // Call api via async task and retrieve response

        new AsyncTask<Object, Void, String>(){
            @Override
            protected String doInBackground(Object... objects) {
                Log.d(TAG, "Analyze Method");
                try {
                    List<String> response = detectText(getImage(path, context), context);
                    if (response != null) {
                        return translateDetectedText(context, response.get(0), response.get(1));
                    } else {
                        return "";
                    }
                } catch (GoogleJsonResponseException e){
                    e.printStackTrace();
                    return "";
                } catch (IOException e){
                    e.printStackTrace();
                    return "";
                }
            }

            @Override
            protected void onPostExecute(String text) {
                super.onPostExecute(text);

                handleText(text);
            }
        }.execute();
    }


    private static Bitmap getImage(String path, Context context) throws IOException{
        return Picasso.with(context).load(path).get();
    }

    private static List<String> detectText(Bitmap bmp, Context context) throws GoogleJsonResponseException, IOException{
        VisionRequestInitializer requestInitializer = new VisionRequestInitializer(API_KEY);
        BatchAnnotateImagesRequest batchAnnotateImagesRequest = new BatchAnnotateImagesRequest();

        List<AnnotateImageRequest> requests = new ArrayList<>();

        Log.d(TAG, "Setting up");

        // Convert bmp to JPEG
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        byte[] imageBytes = baos.toByteArray();

        Image img = new Image().encodeContent(imageBytes);

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
        if (response.getResponses().get(0).getTextAnnotations() != null){
            List<EntityAnnotation> responses = response.getResponses().get(0).getTextAnnotations();

            if (responses != null ) {
                List<String> r = new ArrayList<>();
                r.add(responses.get(0).getDescription());
                r.add(responses.get(0).getLocale());
                return r;
            }else {
                Log.d(TAG, "EMPTY");
                return null;
            }
        }else {
            return null;
        }
    }

    private static String translateDetectedText(Context context, String text, String lang){
        if (!lang.equals(PreferencesUtil.getLanguage(context))){
            // Translate
            return translateText(text, context, lang);
        }else {
            return text;
        }
    }

    private static String translateText(String text, Context context, String lang){
        Translate translate = TranslateOptions.newBuilder().setApiKey(context.getString
                (R.string.apiKey)).build().getService();

        // Translation
        Translation translation = translate.translate(text,
                Translate.TranslateOption.sourceLanguage(lang),
                Translate.TranslateOption.targetLanguage(PreferencesUtil.getLanguage(context)));

        //Log.d(TAG, translation.getTranslatedText());
        return translation.getTranslatedText();
    }

    private static void handleText(String text){
        // Save text to image Message
        if (mImage != null && !text.equals("")){
            mImage.setText(text);
        }

        // Show Dialog
        mOnDetectComplete.detectionComplete(text);
    }

}

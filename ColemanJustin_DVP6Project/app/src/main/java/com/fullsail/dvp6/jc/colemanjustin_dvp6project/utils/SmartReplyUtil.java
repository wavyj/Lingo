package com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.fullsail.dvp6.jc.colemanjustin_dvp6project.R;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

import java.util.ArrayList;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;

public class SmartReplyUtil {
    private static final String TAG = "SmartReplyUtil";

    public interface onSuggestionsCompleteListener{
        void onSuggestionsComplete(String suggestions);
    }

    public SmartReplyUtil(Context context, String query, onSuggestionsCompleteListener onSuggestionsComplete){
        getSuggestions(context, query, onSuggestionsComplete);
    }

    private void getSuggestions(final Context context, final String query, final onSuggestionsCompleteListener onSuggestionsComplete){
        // Implements API.ai to query for responses
        new AsyncTask<String, Void, String>(){
            @Override
            protected String doInBackground(String... params) {

                // Configuration
                AIConfiguration config = new AIConfiguration(context.getString(R.string.apiaiDevKey),
                        ai.api.AIConfiguration.SupportedLanguages.English);

                // Used to execute request
                AIDataService dataService = new AIDataService(config);

                // Request
                AIRequest aiRequest = new AIRequest();
                aiRequest.setQuery(query);

                try{
                    AIResponse response = dataService.request(aiRequest);
                    Result result = response.getResult();
                    Log.d(TAG, result.getFulfillment().getSpeech());
                    return translateReturn(context, result.getFulfillment().getSpeech());
                } catch (AIServiceException e){
                    e.printStackTrace();
                    return "";
                }
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                onSuggestionsComplete.onSuggestionsComplete(s);
            }
        }.execute();
    }

    private String translateReturn(Context context, String text){
        if (!PreferencesUtil.getLanguage(context).equals("en")) {
            Translate translate = TranslateOptions.newBuilder().setApiKey(context.getString
                    (R.string.apiKey)).build().getService();

            // Translation
            Translation translation = translate.translate(text,
                    Translate.TranslateOption.sourceLanguage("en"),
                    Translate.TranslateOption.targetLanguage(PreferencesUtil.getLanguage(context)));

            //Log.d(TAG, translation.getTranslatedText());
            return translation.getTranslatedText();
        }

        return text;
    }
}

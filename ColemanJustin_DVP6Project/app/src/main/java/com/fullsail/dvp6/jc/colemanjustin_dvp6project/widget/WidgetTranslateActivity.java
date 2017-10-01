package com.fullsail.dvp6.jc.colemanjustin_dvp6project.widget;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.speech.RecognizerIntent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.fullsail.dvp6.jc.colemanjustin_dvp6project.R;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils.PreferencesUtil;
import com.google.cloud.translate.Detection;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

import java.util.ArrayList;
import java.util.Locale;

public class WidgetTranslateActivity extends AppCompatActivity {

    private static final int SPEECH_CODE = 0x0001;

    private ProgressDialog dialog;
    private String mReceivedText;
    private String mTranslatedText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_translate);

        Intent translateIntent = getIntent();

        if (translateIntent != null){

            // Voice
            Intent speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            speechIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, R.string.voice_prompt);

            try {
                startActivityForResult(speechIntent, SPEECH_CODE);
            } catch (ActivityNotFoundException e){
                e.printStackTrace();
                Toast.makeText(this, getString(R.string.voiceunavailable), Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SPEECH_CODE && resultCode == RESULT_OK && data != null){
            dialog = new ProgressDialog(this, R.style.dialog);
            dialog.setMessage("Translating");
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.show();

            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            mReceivedText = result.get(0);

            translateText();
        }
    }

    private void translateText(){
        new AsyncTask<String, Void, Void>(){
            @Override
            protected Void doInBackground(String... params) {
                Translate translate = TranslateOptions.newBuilder().setApiKey(getString
                        (R.string.apiKey)).build().getService();

                // Detect
                Detection detection = translate.detect(mReceivedText);

                // Translate
                Translation translation = translate.translate(mReceivedText, Translate.TranslateOption
                        .sourceLanguage(detection.getLanguage()), Translate.TranslateOption.
                        targetLanguage(PreferencesUtil.getLanguage(WidgetTranslateActivity.this)));

                mTranslatedText = translation.getTranslatedText();

                return null;
            }
        }.execute();
    }

    private void updateDisplay(){
        TextView q = (TextView) findViewById(R.id.queryText);
        TextView r = (TextView) findViewById(R.id.resultText);

        if (q != null && r != null){
            q.setText(mReceivedText);
            r.setText(mTranslatedText);
        }

        dialog.cancel();
    }
}

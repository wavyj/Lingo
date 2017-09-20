package com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.fullsail.dvp6.jc.colemanjustin_dvp6project.R;
import com.google.cloud.translate.Detection;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateFactory;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

public class TranslationUtil extends AsyncTask<String, Void, String> {
    private static final String TAG = "TranslationUtil";

    private Context mContext;
    private onTranslateCompleteListener mOnTranslationListener;
    private Message m;
    private boolean b;
    private int i;

    public interface onTranslateCompleteListener{
        void translationComplete(Message m, int i, boolean b);
    }

    public TranslationUtil(Context context, Message message, boolean isLast, int num, onTranslateCompleteListener onTranslationComplete){
        mContext = context;
        b = isLast;
        i = num;
        m = message;
        mOnTranslationListener = onTranslationComplete;
    }

    @Override
    protected String doInBackground(String... strings) {
        Translate translate = TranslateOptions.newBuilder().setApiKey(mContext.getString
                (R.string.apiKey)).build().getService();

        // Translation
        Translation translation = translate.translate(strings[0],
                Translate.TranslateOption.sourceLanguage(m.getLang()),
                Translate.TranslateOption.targetLanguage(PreferencesUtil.getLanguage(mContext)));

        //Log.d(TAG, translation.getTranslatedText());
        return translation.getTranslatedText();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        m.setTranslated(s);
        mOnTranslationListener.translationComplete(m, i, b);
    }
}

package com.fullsail.dvp6.jc.colemanjustin_dvp6project.utils;

import android.content.Context;
import android.util.Log;

import com.fullsail.dvp6.jc.colemanjustin_dvp6project.R;
import com.stfalcon.chatkit.utils.DateFormatter;

import java.util.Date;

public class TimeUtil {
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;

    private static final String TAG = "TimeUtil";

    public static String getTimeAgo(Context context, Date date){
        long time = date.getTime();
        if (time < 1000000000000L){
            time *=1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0){
            return DateFormatter.format(date, DateFormatter.Template.TIME);
        }

        final long diff = now - time;
        //Log.d(TAG, String.valueOf(diff));
        if (diff < MINUTE_MILLIS){
            return context.getString(R.string.date_header_now);
        }else if (diff < 2 * MINUTE_MILLIS){
            return context.getString(R.string.date_header_minute);
        }else if (diff < 50 * MINUTE_MILLIS){
            return diff / MINUTE_MILLIS + " " + context.getString(R.string.date_header_minutes);
        }else if (diff < 90 * MINUTE_MILLIS){
            return context.getString(R.string.date_header_hour);
        }else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " " + context.getString(R.string.date_header_hours);
        }
        return  DateFormatter.format(date, DateFormatter.Template.TIME);
    }
}

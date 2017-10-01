package com.fullsail.dvp6.jc.colemanjustin_dvp6project.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.fullsail.dvp6.jc.colemanjustin_dvp6project.R;

public class WidgetUtil {

    static void updateWidget(Context context, AppWidgetManager manager, int appWidgetId){
        // Remote Views
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_translate);

        // Setup Button PendingIntent
        Intent translateIntent = new Intent(context, WidgetTranslateActivity.class);

        PendingIntent translatePendingIntent = PendingIntent.getActivity(context, appWidgetId,
                translateIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews.setOnClickPendingIntent(R.id.micButton, translatePendingIntent);

        manager.updateAppWidget(appWidgetId, remoteViews);
    }

    static void updateWidgets(Context context, AppWidgetManager manager, int[] appWidgetIds){
        // Update each instance of the widget
        for (int appwidgetId: appWidgetIds){
            updateWidget(context, manager, appwidgetId);
        }
    }
}

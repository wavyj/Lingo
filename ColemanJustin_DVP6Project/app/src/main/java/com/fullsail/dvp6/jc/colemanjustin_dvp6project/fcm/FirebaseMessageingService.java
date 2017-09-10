package com.fullsail.dvp6.jc.colemanjustin_dvp6project.fcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.fullsail.dvp6.jc.colemanjustin_dvp6project.R;
import com.fullsail.dvp6.jc.colemanjustin_dvp6project.main.MainActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sendbird.android.shadow.com.google.gson.JsonElement;
import com.sendbird.android.shadow.com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class FirebaseMessageingService extends FirebaseMessagingService {
    private static final String TAG = "FirebaseMessageingServi";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String message = remoteMessage.getData().get("message");

        String channelUrl = null;
        String senderName = null;
        String senderPhoto = null;

        // Parse Data
        try{
            JSONObject sendBird = new JSONObject(remoteMessage.getData().get("sendbird"));

            // Channel
            JSONObject channel = sendBird.getJSONObject("channel");
            channelUrl = channel.getString("channel_url");

            // Sender
            JSONObject sender = sendBird.getJSONObject("sender");
            senderName = sender.getString("name");
            senderPhoto = sender.getString("profile_url");

            sendNotification(this, message, senderName, senderPhoto, channelUrl);

        }catch (JSONException e){
            e.printStackTrace();
        }

    }

    private void sendNotification(Context context, String message, String senderName,
                                  String senderPhoto, String channelUrl){

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("channelUrl", channelUrl);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);

        Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.notication_icon);
        try {
            builder.setLargeIcon(Picasso.with(context).load(senderPhoto).get());
        }catch (IOException e){
            e.printStackTrace();
        }
        builder.setContentTitle(senderName);
        builder.setContentText(message);
        builder.setSound(notificationSound);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0x01010, builder.build());

    }
}

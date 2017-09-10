package com.fullsail.dvp6.jc.colemanjustin_dvp6project.fcm;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;

public class FirebaseInstanceIDService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        // Update User Token
        SendBird.registerPushTokenForCurrentUser(refreshedToken, new SendBird.RegisterPushTokenWithStatusHandler() {
            @Override
            public void onRegistered(SendBird.PushTokenRegistrationStatus pushTokenRegistrationStatus, SendBirdException e) {
                if (e != null){
                    // Error
                    e.printStackTrace();
                }

                if (pushTokenRegistrationStatus == SendBird.PushTokenRegistrationStatus.PENDING){
                    // Try to register after a connection has been made
                }
            }
        });
    }
}

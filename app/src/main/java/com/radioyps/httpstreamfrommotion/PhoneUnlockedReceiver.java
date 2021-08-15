package com.radioyps.httpstreamfrommotion;

/**
 * Created by yep on 30/06/21.
 */

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

/**
 * Created by yep on 30/06/21.
 */

public class PhoneUnlockedReceiver extends BroadcastReceiver {
    private final static String TAG = PhoneUnlockedReceiver.class.getName();

    private void playNotification(Context context){
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(context, notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        KeyguardManager keyguardManager = (KeyguardManager)context.getSystemService(Context.KEYGUARD_SERVICE);
        if (keyguardManager.isKeyguardSecure()) {
            //phone was unlocked, do stuff here
            Log.i(TAG, "keyguardManager.isKeyguardSecure()>> ");
        }
        if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)){
            playNotification(context);
            Log.d(TAG, "User Present");
        }else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
            Log.d(TAG, "Screen off");
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)){
            Log.d(TAG, "Screen ON");
        }
    }
}
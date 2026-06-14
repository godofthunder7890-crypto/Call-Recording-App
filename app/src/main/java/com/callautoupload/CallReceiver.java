package com.callautoupload;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;

public class CallReceiver extends BroadcastReceiver {

    private static boolean isRecording = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences("CallUploadPrefs", Context.MODE_PRIVATE);
        if (!prefs.getBoolean("enabled", false)) return;

        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

        if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state)) {
            if (!isRecording) {
                isRecording = true;
                Intent i = new Intent(context, RecordingService.class);
                i.setAction("START");
                context.startForegroundService(i);
            }
        } else if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
            if (isRecording) {
                isRecording = false;
                Intent i = new Intent(context, RecordingService.class);
                i.setAction("STOP");
                context.startService(i);
            }
        }
    }
}

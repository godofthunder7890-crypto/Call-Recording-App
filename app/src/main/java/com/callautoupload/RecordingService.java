package com.callautoupload;

import android.app.*;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.*;
import androidx.core.app.NotificationCompat;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RecordingService extends Service {

    private static final String CHANNEL_ID = "CallRecordingChannel";
    private MediaRecorder mediaRecorder;
    private String currentFilePath;
    private boolean isRecording = false;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;
        if ("START".equals(intent.getAction())) {
            startForegroundNotification();
            startRecording();
        } else if ("STOP".equals(intent.getAction())) {
            stopRecording();
        }
        return START_NOT_STICKY;
    }

    private void startRecording() {
        try {
            File dir = new File(getExternalFilesDir(null), "CallRecordings");
            if (!dir.exists()) dir.mkdirs();

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            currentFilePath = dir.getAbsolutePath() + "/CALL_" + timestamp + ".mp3";

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setAudioSamplingRate(44100);
            mediaRecorder.setAudioEncodingBitRate(128000);
            mediaRecorder.setOutputFile(currentFilePath);
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
        } catch (Exception e) {
            e.printStackTrace();
            isRecording = false;
        }
    }

    private void stopRecording() {
        if (isRecording && mediaRecorder != null) {
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
                isRecording = false;
                if (currentFilePath != null) {
                    Intent i = new Intent(this, UploadService.class);
                    i.putExtra("file_path", currentFilePath);
                    startService(i);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        stopForeground(true);
        stopSelf();
    }

    private void startForegroundNotification() {
        Notification n = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("📞 Recording...")
            .setContentText("Call recording in progress")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build();
        startForeground(1, n);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                CHANNEL_ID, "Call Recording", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(ch);
        }
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}

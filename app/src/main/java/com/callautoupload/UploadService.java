package com.callautoupload;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UploadService extends Service {

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;

        String filePath = intent.getStringExtra("file_path");
        if (filePath == null) return START_NOT_STICKY;

        SharedPreferences prefs = getSharedPreferences("CallUploadPrefs", MODE_PRIVATE);
        if (!prefs.getBoolean("enabled", false)) return START_NOT_STICKY;

        boolean uploadTelegram = prefs.getBoolean("upload_telegram", false);
        boolean uploadDrive    = prefs.getBoolean("upload_drive", false);
        String  token          = prefs.getString("telegram_token", "");
        String  chatId         = prefs.getString("telegram_chat_id", "");

        File file = new File(filePath);
        if (!file.exists()) return START_NOT_STICKY;

        executor.execute(() -> {
            if (uploadTelegram && !token.isEmpty() && !chatId.isEmpty()) {
                TelegramUploader.uploadFile(token, chatId, file, null);
            }
            if (uploadDrive) {
                DriveUploader.uploadFile(getApplicationContext(), file, null);
            }
            stopSelf();
        });

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}

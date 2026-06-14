package com.callautoupload;

import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import okhttp3.*;

public class TelegramUploader {

    private static final OkHttpClient client = new OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build();

    public interface UploadCallback {
        void onResult(boolean success);
    }

    public static void uploadFile(String token, String chatId, File file, UploadCallback callback) {
        String timestamp = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(new Date());
        String caption = "📞 Call Recording\n🕐 " + timestamp + "\n📁 " + file.getName();

        RequestBody body = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("chat_id", chatId)
            .addFormDataPart("caption", caption)
            .addFormDataPart("document", file.getName(),
                RequestBody.create(file, MediaType.parse("audio/mpeg")))
            .build();

        Request request = new Request.Builder()
            .url("https://api.telegram.org/bot" + token + "/sendDocument")
            .post(body)
            .build();

        try {
            Response response = client.newCall(request).execute();
            if (callback != null) callback.onResult(response.isSuccessful());
        } catch (IOException e) {
            Log.e("TelegramUploader", e.getMessage());
            if (callback != null) callback.onResult(false);
        }
    }

    public static void sendTestMessage(String token, String chatId, UploadCallback callback) {
        RequestBody body = new FormBody.Builder()
            .add("chat_id", chatId)
            .add("text", "✅ Call Auto Upload connected!\n\nAb teri har call recording yahan aayegi!")
            .build();

        Request request = new Request.Builder()
            .url("https://api.telegram.org/bot" + token + "/sendMessage")
            .post(body)
            .build();

        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                if (callback != null) callback.onResult(response.isSuccessful());
            } catch (IOException e) {
                if (callback != null) callback.onResult(false);
            }
        }).start();
    }
}

package com.callautoupload;

import android.content.Context;
import android.util.Log;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.client.http.FileContent;
import java.util.Collections;

public class DriveUploader {

    public interface UploadCallback {
        void onResult(boolean success);
    }

    public static void uploadFile(Context context, java.io.File file, UploadCallback callback) {
        new Thread(() -> {
            try {
                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
                if (account == null) {
                    if (callback != null) callback.onResult(false);
                    return;
                }

                GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                    context, Collections.singleton(DriveScopes.DRIVE_FILE));
                credential.setSelectedAccount(account.getAccount());

                Drive drive = new Drive.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    new GsonFactory(),
                    credential)
                    .setApplicationName("CallAutoUpload")
                    .build();

                String folderId = getOrCreateFolder(drive);

                File metadata = new File();
                metadata.setName(file.getName());
                if (folderId != null)
                    metadata.setParents(Collections.singletonList(folderId));

                FileContent content = new FileContent("audio/mpeg", file);
                drive.files().create(metadata, content).setFields("id").execute();

                if (callback != null) callback.onResult(true);

            } catch (Exception e) {
                Log.e("DriveUploader", e.getMessage());
                if (callback != null) callback.onResult(false);
            }
        }).start();
    }

    private static String getOrCreateFolder(Drive drive) {
        try {
            String q = "mimeType='application/vnd.google-apps.folder' and name='CallRecordings' and trashed=false";
            var result = drive.files().list().setQ(q).setFields("files(id)").execute();
            if (!result.getFiles().isEmpty())
                return result.getFiles().get(0).getId();

            File folder = new File();
            folder.setName("CallRecordings");
            folder.setMimeType("application/vnd.google-apps.folder");
            return drive.files().create(folder).setFields("id").execute().getId();
        } catch (Exception e) {
            return null;
        }
    }
}

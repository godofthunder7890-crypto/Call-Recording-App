package com.callautoupload;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.Scope;
import com.google.api.services.drive.DriveScopes;

public class DriveSignInActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 200;
    private GoogleSignInClient signInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
            .build();

        signInClient = GoogleSignIn.getClient(this, options);
        startActivityForResult(signInClient.getSignInIntent(), RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignIn.getSignedInAccountFromIntent(data)
                .addOnSuccessListener(account -> {
                    Toast.makeText(this, "✅ Google Drive Connected: " + account.getEmail(), Toast.LENGTH_LONG).show();
                    getSharedPreferences("CallUploadPrefs", MODE_PRIVATE)
                        .edit().putBoolean("drive_signed_in", true).apply();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "❌ Google Sign In Failed", Toast.LENGTH_LONG).show();
                    finish();
                });
        }
    }
}

package com.callautoupload;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private SharedPreferences prefs;

    EditText etTelegramToken, etTelegramChatId;
    SwitchMaterial switchTelegram, switchDrive, switchEnabled;
    Button btnSave, btnSignInDrive, btnTestTelegram;
    TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences("CallUploadPrefs", MODE_PRIVATE);
        initViews();
        loadSavedSettings();
        requestPermissions();
    }

    void initViews() {
        etTelegramToken  = findViewById(R.id.etTelegramToken);
        etTelegramChatId = findViewById(R.id.etTelegramChatId);
        switchTelegram   = findViewById(R.id.switchTelegram);
        switchDrive      = findViewById(R.id.switchDrive);
        switchEnabled    = findViewById(R.id.switchEnabled);
        btnSave          = findViewById(R.id.btnSave);
        btnSignInDrive   = findViewById(R.id.btnSignInDrive);
        btnTestTelegram  = findViewById(R.id.btnTestTelegram);
        tvStatus         = findViewById(R.id.tvStatus);

        btnSave.setOnClickListener(v -> saveSettings());
        btnSignInDrive.setOnClickListener(v -> startActivity(new Intent(this, DriveSignInActivity.class)));
        btnTestTelegram.setOnClickListener(v -> testTelegram());

        switchEnabled.setOnCheckedChangeListener((btn, checked) -> {
            prefs.edit().putBoolean("enabled", checked).apply();
            tvStatus.setText(checked ? "✅ App Active" : "⏸ App Paused");
        });
    }

    void loadSavedSettings() {
        etTelegramToken.setText(prefs.getString("telegram_token", ""));
        etTelegramChatId.setText(prefs.getString("telegram_chat_id", ""));
        switchTelegram.setChecked(prefs.getBoolean("upload_telegram", false));
        switchDrive.setChecked(prefs.getBoolean("upload_drive", false));
        switchEnabled.setChecked(prefs.getBoolean("enabled", false));
        tvStatus.setText(prefs.getBoolean("enabled", false) ? "✅ App Active" : "⏸ App Paused");
    }

    void saveSettings() {
        String token  = etTelegramToken.getText().toString().trim();
        String chatId = etTelegramChatId.getText().toString().trim();

        if (switchTelegram.isChecked() && (token.isEmpty() || chatId.isEmpty())) {
            Toast.makeText(this, "❌ Telegram Token aur Chat ID daalo!", Toast.LENGTH_LONG).show();
            return;
        }

        prefs.edit()
            .putString("telegram_token", token)
            .putString("telegram_chat_id", chatId)
            .putBoolean("upload_telegram", switchTelegram.isChecked())
            .putBoolean("upload_drive", switchDrive.isChecked())
            .apply();

        Toast.makeText(this, "✅ Settings Save Ho Gayi!", Toast.LENGTH_SHORT).show();
    }

    void testTelegram() {
        String token  = etTelegramToken.getText().toString().trim();
        String chatId = etTelegramChatId.getText().toString().trim();
        if (token.isEmpty() || chatId.isEmpty()) {
            Toast.makeText(this, "Pehle Token aur Chat ID daalo!", Toast.LENGTH_SHORT).show();
            return;
        }
        TelegramUploader.sendTestMessage(token, chatId, result ->
            runOnUiThread(() -> Toast.makeText(this,
                result ? "✅ Telegram Connected!" : "❌ Error — Token check karo",
                Toast.LENGTH_LONG).show()));
    }

    void requestPermissions() {
        String[] perms = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.PROCESS_OUTGOING_CALLS
        };
        boolean allGranted = true;
        for (String p : perms)
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED)
                { allGranted = false; break; }
        if (!allGranted)
            ActivityCompat.requestPermissions(this, perms, PERMISSION_REQUEST_CODE);
    }
}

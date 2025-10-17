package com.example.d_book;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class SplashActivity extends AppCompatActivity {

    private LinearLayout linearLayout;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 🔹 전체 화면 모드 설정
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);
        linearLayout = findViewById(R.id.main);

        // 🔹 Firebase Remote Config 초기화
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0)
                .build();

        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.default_config);

        // 🔹 Remote Config fetch & activate
        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        displayMessage();
                    }
                });
    }

    private void displayMessage() {
        // 🔹 원격 설정에서 UI 및 메시지 값 불러오기
        String splashBackground = mFirebaseRemoteConfig.getString("splash_background");
        if (splashBackground == null || splashBackground.isEmpty()) {
            splashBackground = "#1565C0"; // 기본 배경색
        }
        boolean caps = mFirebaseRemoteConfig.getBoolean("splash_message_caps");
        String splashMessage = mFirebaseRemoteConfig.getString("splash_message");

        // 🔹 배경색 적용
        linearLayout.setBackgroundColor(Color.parseColor(splashBackground));

        // 🔹 경고 메시지 표시 여부
        if (caps) {
            runOnUiThread(() -> {
                AlertDialog dialog = new AlertDialog.Builder(SplashActivity.this)
                        .setCancelable(false)
                        .setMessage(splashMessage)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (!isFinishing()) {
                                    dialogInterface.dismiss();
                                    finish();
                                }
                            }
                        })
                        .create();
                dialog.show();
            });
        } else {
            // 🔹 0.5초 지연 후 로그인 화면으로 이동 (UI 안정화 목적)
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }, 500);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 🔹 불필요한 메모리 참조 해제
        linearLayout = null;
    }
}

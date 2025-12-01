package com.example.d_book;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.app.NotificationChannel;
import android.app.NotificationManager;

public class ChangePasswordActivity extends AppCompatActivity {

    private TextInputEditText editCurrentPw, editNewPw, editConfirmPw;
    private MaterialButton buttonChangePw;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference usersRef;

    private static final String CHANNEL_ID = "default";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // 🔹 Android 13 이상 알림 권한 요청
        requestNotificationPermissionIfNeeded();

        // 🔹 UI 연결
        editCurrentPw = findViewById(R.id.editCurrentPw);
        editNewPw = findViewById(R.id.editNewPw);
        editConfirmPw = findViewById(R.id.editConfirmPw);
        buttonChangePw = findViewById(R.id.buttonChangePw);

        // 🔹 Firebase 초기화
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // 🔹 버튼 클릭 처리
        buttonChangePw.setOnClickListener(v -> handlePasswordChange());

        // 🔹 NotificationChannel 생성
        createNotificationChannel();
    }

    // -----------------------------
    // 🔹 Android 13 이상 알림 권한 요청
    // -----------------------------
    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    // -----------------------------
    // 🔹 NotificationChannel 생성
    // -----------------------------
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager == null) return;

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "기본 알림",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            manager.createNotificationChannel(channel);
        }
    }

    // -----------------------------
    // 🔹 비밀번호 변경 처리
    // -----------------------------
    private void handlePasswordChange() {
        String currentPw = editCurrentPw.getText() != null ? editCurrentPw.getText().toString() : "";
        String newPw = editNewPw.getText() != null ? editNewPw.getText().toString() : "";
        String confirmPw = editConfirmPw.getText() != null ? editConfirmPw.getText().toString() : "";

        // --- 입력값 검증 ---
        if (TextUtils.isEmpty(currentPw)) { showToast("현재 비밀번호를 입력해주세요."); return; }
        if (TextUtils.isEmpty(newPw)) { showToast("새 비밀번호를 입력해주세요."); return; }
        if (newPw.length() < 6) { showToast("새 비밀번호는 최소 6자 이상이어야 합니다."); return; }
        if (!newPw.equals(confirmPw)) { showToast("새 비밀번호가 일치하지 않습니다."); return; }
        if (newPw.equals(currentPw)) { showToast("현재 비밀번호와 새 비밀번호가 같습니다."); return; }
        if (currentUser == null || currentUser.getEmail() == null) { showToast("사용자 정보를 가져올 수 없습니다. 다시 로그인해주세요."); return; }

        // --- Firebase 재인증 ---
        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), currentPw);
        currentUser.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                currentUser.updatePassword(newPw).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        showToast("비밀번호가 성공적으로 변경되었습니다.");

                        // --- DB에 변경 시간 기록 ---
                        usersRef.child(currentUser.getUid())
                                .child("lastPasswordChange")
                                .setValue(System.currentTimeMillis());

                        // 🔹 알림 발송 (설정 연동 + 권한 체크)
                        sendPasswordChangedNotification();

                        finish();
                    } else {
                        showToast("비밀번호 변경 실패: " + updateTask.getException().getMessage());
                    }
                });
            } else {
                if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                    showToast("현재 비밀번호가 올바르지 않습니다.");
                } else {
                    showToast("인증 실패: " + task.getException().getMessage());
                }
            }
        });
    }

    // -----------------------------
    // 🔹 비밀번호 변경 알림 (설정 연동 + 권한 체크)
    // -----------------------------
    private void sendPasswordChangedNotification() {
        // 1️⃣ 설정 확인
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String notificationPref = prefs.getString("notifications", "on");
        Log.d("ChangePasswordActivity", "notifications setting: " + notificationPref);

        if (!"on".equals(notificationPref)) return;

        // 2️⃣ Android 13 이상 권한 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d("ChangePasswordActivity", "알림 권한 없음, 알림 발송 안함");
                return;
            }
        }

        // 3️⃣ 알림 빌드 & 발송
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // 실제 존재하는 drawable 확인
                .setContentTitle("비밀번호 변경 완료")
                .setContentText("회원님의 비밀번호가 성공적으로 변경되었습니다.")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify(2001, builder.build());
    }

    // -----------------------------
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

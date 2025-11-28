package com.example.d_book;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChangePasswordActivity extends AppCompatActivity {

    private TextInputEditText editCurrentPw, editNewPw, editConfirmPw;
    private MaterialButton buttonChangePw;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // --- UI 연결 ---
        editCurrentPw = findViewById(R.id.editCurrentPw);
        editNewPw = findViewById(R.id.editNewPw);
        editConfirmPw = findViewById(R.id.editConfirmPw);
        buttonChangePw = findViewById(R.id.buttonChangePw);

        // --- Firebase 초기화 ---
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // --- 버튼 클릭 처리 ---
        buttonChangePw.setOnClickListener(v -> handlePasswordChange());
    }

    private void handlePasswordChange() {
        String currentPw = editCurrentPw.getText() != null ? editCurrentPw.getText().toString() : "";
        String newPw = editNewPw.getText() != null ? editNewPw.getText().toString() : "";
        String confirmPw = editConfirmPw.getText() != null ? editConfirmPw.getText().toString() : "";

        // --- 입력값 검증 ---
        if (TextUtils.isEmpty(currentPw)) {
            showToast("현재 비밀번호를 입력해주세요.");
            return;
        }
        if (TextUtils.isEmpty(newPw)) {
            showToast("새 비밀번호를 입력해주세요.");
            return;
        }
        if (newPw.length() < 6) {
            showToast("새 비밀번호는 최소 6자 이상이어야 합니다.");
            return;
        }
        if (!newPw.equals(confirmPw)) {
            showToast("새 비밀번호가 일치하지 않습니다.");
            return;
        }
        if (newPw.equals(currentPw)) {
            showToast("현재 비밀번호와 새 비밀번호가 같습니다.");
            return;
        }
        if (currentUser == null || currentUser.getEmail() == null) {
            showToast("사용자 정보를 가져올 수 없습니다. 다시 로그인해주세요.");
            return;
        }

        // --- Firebase 재인증 ---
        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), currentPw);

        currentUser.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // --- 비밀번호 업데이트 ---
                currentUser.updatePassword(newPw).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        showToast("비밀번호가 성공적으로 변경되었습니다.");

                        // --- DB에 변경 시간 기록 ---
                        usersRef.child(currentUser.getUid())
                                .child("lastPasswordChange")
                                .setValue(System.currentTimeMillis());

                        finish(); // 이전 화면으로 돌아가기
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

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

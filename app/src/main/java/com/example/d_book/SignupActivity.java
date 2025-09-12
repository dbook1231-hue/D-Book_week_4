package com.example.d_book;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.text.TextUtils;
import android.widget.Toast;

import com.example.d_book.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.storage.FirebaseStorage;

public class SignupActivity extends AppCompatActivity {

    private static final int PICK_FROM_ALBUM = 10;
    private EditText email, name, password;
    private Button signup;
    private String splash_background;
    private ImageView profile;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Remote Config 색상 설정
        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        splash_background = mFirebaseRemoteConfig.getString(getString(R.string.rc_color));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor(splash_background));
        }

        // 뷰 초기화
        profile = findViewById(R.id.signupActivity_imageview_profile);
        email = findViewById(R.id.signupActivity_edittext_email);
        name = findViewById(R.id.signupActivity_edittext_name);
        password = findViewById(R.id.signupActivity_edittext_password);
        signup = findViewById(R.id.signupActivity_button_signup);
        signup.setBackgroundColor(Color.parseColor(splash_background));

        // 🔽 로그인 버튼 초기화 및 이동 처리
        Button loginButton = findViewById(R.id.signupActivity_button_login);
        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // 프로필 이미지 선택
        profile.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
            startActivityForResult(intent, PICK_FROM_ALBUM);
        });

        // 회원가입 버튼 클릭
        signup.setOnClickListener(view -> {
            if (TextUtils.isEmpty(email.getText().toString()) ||
                    TextUtils.isEmpty(name.getText().toString()) ||
                    TextUtils.isEmpty(password.getText().toString()) ||
                    imageUri == null) {
                Toast.makeText(SignupActivity.this, "모든 정보를 입력하세요", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d("Signup", "회원가입 시작 - imageUri: " + imageUri);

            FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                    .addOnCompleteListener(SignupActivity.this, task -> {
                        if (!task.isSuccessful()) {
                            Log.e("SignupError", "회원가입 실패: ", task.getException());
                            Toast.makeText(SignupActivity.this, "회원가입 실패: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            return;
                        }

                        final String uid = task.getResult().getUser().getUid();
                        Log.d("Signup", "회원가입 성공 - uid: " + uid);

                        // Storage에 이미지 업로드
                        FirebaseStorage.getInstance().getReference()
                                .child("userImages")
                                .child(uid)
                                .putFile(imageUri)
                                .addOnSuccessListener(taskSnapshot -> {
                                    // 업로드 성공 후 URL 가져오기
                                    taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                                        String imageUrl = uri.toString();
                                        Log.d("Signup", "이미지 업로드 성공 - URL: " + imageUrl);

                                        UserModel userModel = new UserModel();
                                        userModel.userName = name.getText().toString();
                                        userModel.profileImageUrl = imageUrl;
                                        userModel.uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                                        Log.d("Signup", "유저 정보: 이름=" + userModel.userName + ", 이미지=" + userModel.profileImageUrl);

                                        FirebaseDatabase.getInstance().getReference()
                                                .child("users")
                                                .child(uid)
                                                .setValue(userModel)
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d("Signup", "유저 정보 저장 성공");
                                                    Toast.makeText(SignupActivity.this, "회원가입 완료!", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                    startActivity(intent);
                                                    finish();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("Signup", "유저 정보 저장 실패", e);
                                                    Toast.makeText(SignupActivity.this, "유저 저장 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                });
                                    });
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Signup", "프로필 이미지 업로드 실패", e);
                                    Toast.makeText(SignupActivity.this, "이미지 업로드 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    });
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FROM_ALBUM && resultCode == RESULT_OK) {
            imageUri = data.getData();
            profile.setImageURI(imageUri);
            Log.d("Signup", "이미지 선택됨: " + imageUri);
        }
    }
}
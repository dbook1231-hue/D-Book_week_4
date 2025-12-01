package com.example.d_book.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.example.d_book.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import android.app.NotificationChannel;
import android.app.NotificationManager;

public class ProfileSettingsFragment extends Fragment {

    private ImageView imageProfile;
    private ImageButton buttonChangeProfileImage;
    private MaterialButton buttonChangeNickname;
    private TextInputEditText editNickname;

    private FirebaseUser currentUser;
    private DatabaseReference usersRef;
    private Uri imageUri;

    private static final String CHANNEL_ID = "default";

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    uploadProfileImage();
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_settings, container, false);

        // --- 뷰 초기화 ---
        imageProfile = view.findViewById(R.id.imageProfile);
        buttonChangeProfileImage = view.findViewById(R.id.buttonChangeProfileImage);
        buttonChangeNickname = view.findViewById(R.id.buttonChangeNickname);
        editNickname = view.findViewById(R.id.editNickname);

        // --- Firebase ---
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // --- NotificationChannel 생성 ---
        createNotificationChannel();

        loadUserProfile();

        // --- 클릭 이벤트 ---
        buttonChangeProfileImage.setOnClickListener(v -> chooseProfileImage());
        buttonChangeNickname.setOnClickListener(v -> {
            String newNickname = editNickname.getText().toString().trim();
            updateNickname(newNickname);
        });

        return view;
    }

    // --- 사용자 정보 불러오기 ---
    private void loadUserProfile() {
        if (currentUser == null) return;

        usersRef.child(currentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String nickname = snapshot.child("userName").getValue(String.class);
                            String profileUrl = snapshot.child("profileImageUrl").getValue(String.class);

                            editNickname.setText(nickname);

                            if (profileUrl != null && !profileUrl.isEmpty()) {
                                Glide.with(getContext()).load(profileUrl).into(imageProfile);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(getContext(), "사용자 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // --- 닉네임 변경 ---
    private void updateNickname(String newNickname) {
        if (TextUtils.isEmpty(newNickname)) {
            Toast.makeText(getContext(), "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentUser == null) return;

        usersRef.child(currentUser.getUid()).child("userName").setValue(newNickname)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "닉네임이 변경되었습니다.", Toast.LENGTH_SHORT).show();
                    // 🔥 닉네임 변경 알림
                    sendNotification("닉네임 변경 완료", "회원님의 닉네임이 변경되었습니다.");
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "닉네임 변경 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // --- 프로필 사진 선택 ---
    private void chooseProfileImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        imagePickerLauncher.launch(Intent.createChooser(intent, "프로필 사진 선택"));
    }

    // --- 프로필 사진 업로드 ---
    private void uploadProfileImage() {
        if (imageUri == null || currentUser == null) return;

        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("userImages/" + currentUser.getUid() + ".jpg");

        storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    usersRef.child(currentUser.getUid()).child("profileImageUrl")
                            .setValue(uri.toString())
                            .addOnSuccessListener(aVoid -> {
                                Glide.with(getContext()).load(uri).into(imageProfile);
                                Toast.makeText(getContext(), "프로필 사진이 변경되었습니다.", Toast.LENGTH_SHORT).show();
                                // 🔥 프로필 사진 변경 알림
                                sendNotification("프로필 사진 변경 완료", "회원님의 프로필 사진이 변경되었습니다.");
                            })
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "DB 업데이트 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
        ).addOnFailureListener(e -> Toast.makeText(getContext(), "이미지 업로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // -----------------------------
    // 🔹 NotificationChannel 생성
    // -----------------------------
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
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
    // 🔹 알림 발송 (SharedPreferences + 권한 체크)
    // -----------------------------
    private void sendNotification(String title, String message) {
        if (getContext() == null) return;

        // 1️⃣ 설정 확인
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String notificationPref = prefs.getString("notifications", "on");
        if (!"on".equals(notificationPref)) return;

        // 2️⃣ Android 13 이상 권한 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (getContext().checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d("ProfileSettingsFragment", "알림 권한 없음, 알림 발송 안함");
                return;
            }
        }

        // 3️⃣ 알림 발송
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // drawable 확인 필수
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat manager = NotificationManagerCompat.from(getContext());
        manager.notify((int) System.currentTimeMillis(), builder.build()); // 고유 ID 사용
    }
}

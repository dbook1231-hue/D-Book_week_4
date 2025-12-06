package com.example.d_book.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.d_book.R;
import com.example.d_book.model.UserModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

    private TextView tvReviewsCount, tvLikesCount, tvRepliesCount, tvFavoritesCount;

    private Button btnWrite, btnAddBook;

    private FirebaseUser currentUser;
    private DatabaseReference usersRef;
    private Uri imageUri;

    private static final String CHANNEL_ID = "default";
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_settings, container, false);

        // 뷰 초기화
        imageProfile = view.findViewById(R.id.imageProfile);
        buttonChangeProfileImage = view.findViewById(R.id.buttonChangeProfileImage);
        buttonChangeNickname = view.findViewById(R.id.buttonChangeNickname);
        editNickname = view.findViewById(R.id.editNickname);

        tvReviewsCount = view.findViewById(R.id.tv_reviews_count);
        tvLikesCount = view.findViewById(R.id.tv_likes_count);
        tvRepliesCount = view.findViewById(R.id.tv_replies_count);
        tvFavoritesCount = view.findViewById(R.id.tv_favorites_count);

        btnWrite = view.findViewById(R.id.btn_write);
        btnAddBook = view.findViewById(R.id.btn_add_book);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // ActivityResultLauncher 초기화
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        uploadProfileImage();
                    }
                });

        createNotificationChannel();
        loadUserProfile();

        buttonChangeProfileImage.setOnClickListener(v -> chooseProfileImage());
        buttonChangeNickname.setOnClickListener(v -> {
            String newNickname = editNickname.getText().toString().trim();
            updateNickname(newNickname);
        });

        return view;
    }

    // 사용자 정보 + 통계 불러오기
    private void loadUserProfile() {
        if (currentUser == null) return;

        usersRef.child(currentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        UserModel user = snapshot.getValue(UserModel.class);
                        if (user != null) {
                            editNickname.setText(user.userName);

                            if (!TextUtils.isEmpty(user.profileImageUrl)) {
                                Glide.with(getContext()).load(user.profileImageUrl).into(imageProfile);
                            }

                            updateStats(user.reviewCount, user.likeCount, user.replyCount, user.favoriteCount);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "사용자 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // 통계 업데이트
    private void updateStats(long review, long like, long reply, long favorite) {
        if (getView() == null) return;

        tvReviewsCount.setText(String.valueOf(review));
        tvLikesCount.setText(String.valueOf(like));
        tvRepliesCount.setText(String.valueOf(reply));
        tvFavoritesCount.setText(String.valueOf(favorite));
    }

    // 닉네임 변경
    private void updateNickname(String newNickname) {
        if (TextUtils.isEmpty(newNickname)) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        if (currentUser == null) return;

        usersRef.child(currentUser.getUid()).child("userName").setValue(newNickname)
                .addOnSuccessListener(aVoid -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "닉네임이 변경되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "닉네임 변경 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // 프로필 사진 선택
    private void chooseProfileImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        imagePickerLauncher.launch(Intent.createChooser(intent, "프로필 사진 선택"));
    }

    // 프로필 사진 업로드
    private void uploadProfileImage() {
        if (imageUri == null || currentUser == null) return;

        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("userImages/" + currentUser.getUid() + ".jpg");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            usersRef.child(currentUser.getUid()).child("profileImageUrl")
                                    .setValue(uri.toString())
                                    .addOnSuccessListener(aVoid -> {
                                        Glide.with(getContext()).load(uri).into(imageProfile);
                                        if (getContext() != null) {
                                            Toast.makeText(getContext(), "프로필 사진이 변경되었습니다.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }))
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "프로필 사진 업로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // 알림 채널 생성
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Context context = getContext();
            if (context == null) return;

            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager == null) return;

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "기본 알림",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            manager.createNotificationChannel(channel);
        }
    }
}

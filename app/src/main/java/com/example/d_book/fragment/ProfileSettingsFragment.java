package com.example.d_book.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

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

public class ProfileSettingsFragment extends Fragment {

    private ImageView imageProfile;
    private MaterialButton buttonChangeProfileImage, buttonChangeNickname;
    private TextInputEditText editNickname;

    private FirebaseUser currentUser;
    private DatabaseReference usersRef;
    private Uri imageUri;

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

        imageProfile = view.findViewById(R.id.imageProfile);
        buttonChangeProfileImage = view.findViewById(R.id.buttonChangeProfileImage);
        buttonChangeNickname = view.findViewById(R.id.buttonChangeNickname);
        editNickname = view.findViewById(R.id.editNickname);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        loadUserProfile();

        buttonChangeProfileImage.setOnClickListener(v -> chooseProfileImage());
        buttonChangeNickname.setOnClickListener(v -> {
            String newNickname = editNickname.getText().toString().trim();
            updateNickname(newNickname);
        });

        return view;
    }

    // --- DB에서 사용자 정보 불러오기 ---
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
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "닉네임이 변경되었습니다.", Toast.LENGTH_SHORT).show())
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
                            })
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "DB 업데이트 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
        ).addOnFailureListener(e -> Toast.makeText(getContext(), "이미지 업로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}

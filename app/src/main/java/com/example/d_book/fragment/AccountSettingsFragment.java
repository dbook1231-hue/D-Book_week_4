package com.example.d_book.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.d_book.ChangePasswordActivity;
import com.example.d_book.LoginActivity;
import com.example.d_book.R;

public class AccountSettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_account, rootKey);

        // 비밀번호 변경
        Preference changePw = findPreference("change_password");
        if (changePw != null) {
            changePw.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(getContext(), ChangePasswordActivity.class));
                return true;
            });
        }

        // 로그아웃
        Preference logout = findPreference("logout");
        if (logout != null) {
            logout.setOnPreferenceClickListener(preference -> {
                showLogoutDialog();
                return true;
            });
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("로그아웃")
                .setMessage("정말 로그아웃하시겠습니까?")
                .setPositiveButton("로그아웃", (dialog, which) -> {

                    // TODO 로그인 정보 삭제 (필요 시)
                    // SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                    // prefs.edit().clear().apply();

                    // 로그인 화면으로 이동
                    Intent intent = new Intent(getContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("취소", null)
                .show();
    }
}

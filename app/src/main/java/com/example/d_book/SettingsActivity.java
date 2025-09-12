package com.example.d_book;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("설정");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // 뒤로가기
        return true;
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);

            // 프로필 관리 클릭
            Preference profilePref = findPreference("profile");
            if (profilePref != null) {
                profilePref.setOnPreferenceClickListener(preference -> {
                    Toast.makeText(getContext(), "프로필 관리 클릭됨", Toast.LENGTH_SHORT).show();
                    return true;
                });
            }

            // 계정 관리 클릭
            Preference accountPref = findPreference("account");
            if (accountPref != null) {
                accountPref.setOnPreferenceClickListener(preference -> {
                    Toast.makeText(getContext(), "계정 관리 클릭됨", Toast.LENGTH_SHORT).show();
                    return true;
                });
            }

            // 알림 설정 클릭
            Preference notificationPref = findPreference("notifications");
            if (notificationPref != null) {
                notificationPref.setOnPreferenceClickListener(preference -> {
                    Toast.makeText(getContext(), "알림 설정 클릭됨", Toast.LENGTH_SHORT).show();
                    return true;
                });
            }

            // 테마 변경 클릭
            Preference themePref = findPreference("theme");
            if (themePref != null) {
                themePref.setOnPreferenceClickListener(preference -> {
                    Toast.makeText(getContext(), "테마 변경 클릭됨", Toast.LENGTH_SHORT).show();
                    return true;
                });
            }

            // 앱 정보 클릭
            Preference aboutPref = findPreference("about");
            if (aboutPref != null) {
                aboutPref.setOnPreferenceClickListener(preference -> {
                    Intent intent = new Intent(getContext(), AboutActivity.class);
                    startActivity(intent);
                    return true;
                });
            }
        }
    }
}

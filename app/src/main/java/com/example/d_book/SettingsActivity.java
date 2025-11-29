package com.example.d_book;

import android.content.SharedPreferences;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.example.d_book.fragment.AccountSettingsFragment;
import com.example.d_book.fragment.ProfileSettingsFragment;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // 🔥 저장된 테마 적용 — super.onCreate() 전에 실행
        applySavedTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Toolbar 설정
        Toolbar toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("설정");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // rootLayout 배경색 적용 (테마 기반)
        View rootLayout = findViewById(R.id.settings_root_layout);
        if (rootLayout != null) {
            rootLayout.setBackgroundColor(getThemeColor(R.attr.backgroundColor));
        }

        // SettingsFragment 불러오기
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // -------------------------
    // 테마 적용 헬퍼
    // -------------------------
    private void applySavedTheme() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = prefs.getString("theme", "light");

        switch (theme) {
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "system":
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    // 테마 속성 색 가져오기
    private int getThemeColor(int attr) {
        int[] attrs = {attr};
        TypedArray ta = obtainStyledAttributes(attrs);
        int color = ta.getColor(0, 0xFFFFFFFF);
        ta.recycle();
        return color;
    }

    // -------------------------
    // SettingsFragment
    // -------------------------
    public static class SettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

            // 🔹 기존 Boolean 값 제거 (ClassCastException 방지)
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            if (prefs.contains("notifications") && !(prefs.getAll().get("notifications") instanceof String)) {
                prefs.edit().remove("notifications").apply();
            }

            setPreferencesFromResource(R.xml.preferences, rootKey);

            // 🔹 프로필 클릭 → ProfileSettingsFragment
            Preference profilePref = findPreference("profile");
            if (profilePref != null) {
                profilePref.setOnPreferenceClickListener(preference -> {
                    getParentFragmentManager()
                            .beginTransaction()
                            .replace(R.id.settings_container, new ProfileSettingsFragment())
                            .addToBackStack(null)
                            .commit();
                    return true;
                });
            }

            // 🔹 계정 클릭 → AccountSettingsFragment
            Preference accountPref = findPreference("account");
            if (accountPref != null) {
                accountPref.setOnPreferenceClickListener(preference -> {
                    getParentFragmentManager()
                            .beginTransaction()
                            .replace(R.id.settings_container, new AccountSettingsFragment())
                            .addToBackStack(null)
                            .commit();
                    return true;
                });
            }

            // 🔹 알림 설정 클릭
            ListPreference notificationsPref = findPreference("notifications");
            if (notificationsPref != null) {
                notificationsPref.setOnPreferenceClickListener(preference -> {
                    Toast.makeText(getContext(), "알림 설정 클릭됨", Toast.LENGTH_SHORT).show();
                    return true;
                });
            }

            // 🔹 테마 변경 처리
            ListPreference themePref = findPreference("theme");
            if (themePref != null) {
                themePref.setOnPreferenceChangeListener((preference, newValue) -> {
                    String selectedTheme = newValue.toString();

                    // SharedPreferences에 저장
                    SharedPreferences.Editor editor =
                            PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
                    editor.putString("theme", selectedTheme);
                    editor.apply();

                    // 모드 적용
                    switch (selectedTheme) {
                        case "light":
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            break;
                        case "dark":
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                            break;
                        case "system":
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                            break;
                    }

                    // Activity 재시작 → UI 갱신
                    if (getActivity() != null) getActivity().recreate();
                    return true;
                });
            }

            // 🔹 앱 정보 클릭
            Preference aboutPref = findPreference("about");
            if (aboutPref != null) {
                aboutPref.setOnPreferenceClickListener(preference -> {
                    startActivity(new Intent(getContext(), AboutActivity.class));
                    return true;
                });
            }
        }
    }
}

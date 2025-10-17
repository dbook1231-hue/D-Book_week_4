package com.example.d_book;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ReaderActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);

        androidx.viewpager2.widget.ViewPager2 pager = findViewById(R.id.pagerReader);
        java.util.List<String> dummyPages = new java.util.ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            dummyPages.add("페이지 " + i + "\n\n" +
                    "로렘 입숨은 인쇄 및 조판에서 흔히 사용하는 더미 텍스트입니다. "+
                    "가독성 확인을 위해 여러 문단을 구성하여 페이지 스크롤을 확인할 수 있습니다. "+
                    "이 문장은 예시 텍스트로 실제 데이터와 무관합니다. \n\n" +
                    "문단 반복 예시... 문단 반복 예시... 문단 반복 예시... 문단 반복 예시...\n\n");
        }
        pager.setAdapter(new ReaderPagerAdapter(dummyPages));
    }
}
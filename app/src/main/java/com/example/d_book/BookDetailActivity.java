package com.example.d_book;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class BookDetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvAuthor;
    private ImageView ivCover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        tvTitle = findViewById(R.id.tvBookTitle);
        tvAuthor = findViewById(R.id.tvBookAuthor);
        ivCover = findViewById(R.id.ivBookCover);

        // 인텐트로 데이터 받아오기
        String title = getIntent().getStringExtra("title");
        String author = getIntent().getStringExtra("author");

        tvTitle.setText(title);
        tvAuthor.setText(author);
        // ivCover는 샘플 이미지 적용 가능
    }
}

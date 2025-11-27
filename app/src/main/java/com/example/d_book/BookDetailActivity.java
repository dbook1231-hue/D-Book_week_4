package com.example.d_book;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

public class BookDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.title_book_detail));
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        TextView textTitle = findViewById(R.id.textBookTitle);
        TextView textAuthor = findViewById(R.id.textBookAuthor);
        TextView textSummary = findViewById(R.id.textBookSummary);
        TextView textChallengeTitle = findViewById(R.id.textChallengeTitle);
        TextView textChallengeDescription = findViewById(R.id.textChallengeDescription);
        com.google.android.material.chip.ChipGroup chipGroupKeywords = findViewById(R.id.chipGroupKeywords);
        android.widget.LinearLayout layoutReviews = findViewById(R.id.layoutReviews);
        TextView textReviewsEmpty = findViewById(R.id.textReviewsEmpty);
        android.widget.ImageView imageCover = findViewById(R.id.imageCoverLarge);
        com.google.android.material.textfield.TextInputEditText editReview = findViewById(R.id.editReview);
        com.google.android.material.button.MaterialButton buttonSubmitReview = findViewById(R.id.buttonSubmitReview);

        String title = getIntent().getStringExtra("title");
        String author = getIntent().getStringExtra("author");
        String thumbnail = getIntent().getStringExtra("thumbnail");
        if (title != null) textTitle.setText(title);
        if (author != null) textAuthor.setText(author);
        if (thumbnail != null && !thumbnail.isEmpty()) {
            Glide.with(this)
                    .load(thumbnail)
                    .placeholder(R.drawable.ic_book_placeholder)
                    .error(R.drawable.ic_book_placeholder)
                    .into(imageCover);
        } else {
            imageCover.setImageResource(R.drawable.ic_book_placeholder);
        }

        BookDetailData detailData = getDetailData(title);
        if (detailData != null) {
            textSummary.setText(detailData.summary);
        } else {
            textSummary.setText(getString(R.string.book_detail_summary_placeholder));
        }

        // Populate keyword chips
        chipGroupKeywords.removeAllViews();
        String[] keywords = detailData != null && detailData.keywords != null
                ? detailData.keywords
                : getResources().getStringArray(R.array.book_detail_keywords_sample);
        android.view.LayoutInflater inflater = android.view.LayoutInflater.from(this);
        for (String keyword : keywords) {
            com.google.android.material.chip.Chip chip = (com.google.android.material.chip.Chip) inflater.inflate(R.layout.item_keyword_chip, chipGroupKeywords, false);
            chip.setText(keyword);
            chipGroupKeywords.addView(chip);
        }

        // Setup challenge card
        textChallengeTitle.setText(getString(R.string.book_detail_challenge_title_placeholder));
        textChallengeDescription.setText(getString(R.string.book_detail_challenge_desc_placeholder));

        buttonSubmitReview.setOnClickListener(v -> {
            String reviewText = editReview.getText() != null ? editReview.getText().toString().trim() : "";
            if (reviewText.isEmpty()) {
                android.widget.Toast.makeText(this, "후기를 입력해 주세요.", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            addReview(layoutReviews, "나", reviewText, "방금");
            editReview.setText("");
            textReviewsEmpty.setVisibility(android.view.View.GONE);
            android.widget.Toast.makeText(this, "후기가 등록되었습니다.", android.widget.Toast.LENGTH_SHORT).show();
        });

        // Populate reviews
        String[] reviews = detailData != null && detailData.reviews != null
                ? detailData.reviews
                : getResources().getStringArray(R.array.book_detail_reviews_sample);
        layoutReviews.removeAllViews();
        if (reviews.length == 0) {
            textReviewsEmpty.setVisibility(android.view.View.VISIBLE);
        } else {
            textReviewsEmpty.setVisibility(android.view.View.GONE);
            for (String review : reviews) {
                addReview(layoutReviews, review.split("\\|", 3)[0], review.split("\\|", 3)[1], review.split("\\|", 3)[2]);
            }
        }
    }

    private void addReview(android.widget.LinearLayout container, String reviewer, String body, String meta) {
        android.view.View reviewItem = getLayoutInflater().inflate(R.layout.item_review_preview, container, false);
        TextView textReviewer = reviewItem.findViewById(R.id.textReviewerName);
        TextView textReviewBody = reviewItem.findViewById(R.id.textReviewBody);
        TextView textReviewMeta = reviewItem.findViewById(R.id.textReviewMeta);
        textReviewer.setText(reviewer);
        textReviewBody.setText(body);
        textReviewMeta.setText(meta);
        container.addView(reviewItem);
    }

    private BookDetailData getDetailData(String title) {
        if (title == null) return null;
        java.util.Map<String, BookDetailData> data = new java.util.HashMap<>();
        data.put("나미야 잡화점의 기적", new BookDetailData(
                "과거와 현재가 편지로 이어지는 작은 잡화점에서 벌어지는 따뜻한 기적의 이야기.",
                new String[]{"미스터리", "따뜻한 이야기", "편지", "시간여행"},
                new String[]{
                        "은지|편지를 통해 이어지는 인연이 감동적이에요.|2024.11.01 · 좋아요 12",
                        "성호|잠들기 전에 읽기 딱 좋은 잔잔한 위로.|2024.10.20 · 좋아요 8"
                }
        ));
        data.put("해리 포터와 마법사의 돌", new BookDetailData(
                "마법세계에 입문한 해리가 호그와트에서 친구들과 겪는 첫 모험.",
                new String[]{"판타지", "마법", "모험"},
                new String[]{
                        "민수|어릴 때 읽은 설렘을 다시 느꼈어요.|2024.09.10 · 좋아요 20"
                }
        ));
        data.put("해리 포터와 비밀의 방", new BookDetailData(
                "호그와트에 다시 나타난 비밀의 방을 둘러싼 미스터리와 해리의 성장.",
                new String[]{"판타지", "미스터리", "호그와트"},
                null
        ));
        data.put("어린 왕자", new BookDetailData(
                "사막에서 만난 어린 왕자가 전하는 순수함과 삶에 대한 통찰.",
                new String[]{"우화", "철학", "성장"},
                null
        ));
        return data.get(title);
    }

    private static class BookDetailData {
        final String summary;
        final String[] keywords;
        final String[] reviews;

        BookDetailData(String summary, String[] keywords, String[] reviews) {
            this.summary = summary;
            this.keywords = keywords;
            this.reviews = reviews == null ? new String[0] : reviews;
        }
    }
}

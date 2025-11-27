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
        com.google.android.material.progressindicator.LinearProgressIndicator progressChallenge = findViewById(R.id.progressChallenge);
        com.google.android.material.chip.ChipGroup chipGroupKeywords = findViewById(R.id.chipGroupKeywords);
        android.widget.LinearLayout layoutReviews = findViewById(R.id.layoutReviews);
        TextView textReviewsEmpty = findViewById(R.id.textReviewsEmpty);
        android.widget.ImageView imageCover = findViewById(R.id.imageCoverLarge);

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

        // Dummy storyline text
        textSummary.setText(getString(R.string.book_detail_summary_placeholder));

        // Populate keyword chips
        chipGroupKeywords.removeAllViews();
        String[] keywords = getResources().getStringArray(R.array.book_detail_keywords_sample);
        android.view.LayoutInflater inflater = android.view.LayoutInflater.from(this);
        for (String keyword : keywords) {
            com.google.android.material.chip.Chip chip = (com.google.android.material.chip.Chip) inflater.inflate(R.layout.item_keyword_chip, chipGroupKeywords, false);
            chip.setText(keyword);
            chipGroupKeywords.addView(chip);
        }

        // Setup challenge card
        textChallengeTitle.setText(getString(R.string.book_detail_challenge_title_placeholder));
        textChallengeDescription.setText(getString(R.string.book_detail_challenge_desc_placeholder));
        progressChallenge.setProgress(45);

        MaterialButton buttonJoinChallenge = findViewById(R.id.buttonJoinChallenge);
        buttonJoinChallenge.setOnClickListener(v -> {
            android.widget.Toast.makeText(this, R.string.toast_challenge_joined, android.widget.Toast.LENGTH_SHORT).show();
        });

        // Populate reviews
        String[] reviews = getResources().getStringArray(R.array.book_detail_reviews_sample);
        layoutReviews.removeAllViews();
        if (reviews.length == 0) {
            textReviewsEmpty.setVisibility(android.view.View.VISIBLE);
        } else {
            textReviewsEmpty.setVisibility(android.view.View.GONE);
            for (String review : reviews) {
                android.view.View reviewItem = inflater.inflate(R.layout.item_review_preview, layoutReviews, false);
                TextView textReviewer = reviewItem.findViewById(R.id.textReviewerName);
                TextView textReviewBody = reviewItem.findViewById(R.id.textReviewBody);
                TextView textReviewMeta = reviewItem.findViewById(R.id.textReviewMeta);
                textReviewer.setText(review.split("\\|", 3)[0]);
                textReviewBody.setText(review.split("\\|", 3)[1]);
                textReviewMeta.setText(review.split("\\|", 3)[2]);
                layoutReviews.addView(reviewItem);
            }
        }
    }
}

package com.example.d_book;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BookDetailActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference usersRef;
    private ListenerRegistration reviewsListener;
    private String currentUserName = "Guest";
    private String bookTitle;

    private LinearLayout layoutReviews;
    private TextView textReviewsEmpty;
    private TextInputEditText editReview;
    private MaterialButton buttonSubmitReview;
    private BookDetailData detailData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

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
        ChipGroup chipGroupKeywords = findViewById(R.id.chipGroupKeywords);
        layoutReviews = findViewById(R.id.layoutReviews);
        textReviewsEmpty = findViewById(R.id.textReviewsEmpty);
        ImageView imageCover = findViewById(R.id.imageCoverLarge);
        editReview = findViewById(R.id.editReview);
        buttonSubmitReview = findViewById(R.id.buttonSubmitReview);

        String title = getIntent().getStringExtra("title");
        String author = getIntent().getStringExtra("author");
        String thumbnail = getIntent().getStringExtra("thumbnail");
        bookTitle = title;

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

        detailData = getDetailData(title);
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
        LayoutInflater inflater = LayoutInflater.from(this);
        for (String keyword : keywords) {
            Chip chip = (Chip) inflater.inflate(R.layout.item_keyword_chip, chipGroupKeywords, false);
            chip.setText(keyword);
            chipGroupKeywords.addView(chip);
        }

        // Setup challenge card
        textChallengeTitle.setText(getString(R.string.book_detail_challenge_title_placeholder));
        textChallengeDescription.setText(getString(R.string.book_detail_challenge_desc_placeholder));

        buttonSubmitReview.setOnClickListener(v -> {
            String reviewText = editReview.getText() != null ? editReview.getText().toString().trim() : "";
            if (reviewText.isEmpty()) {
                Toast.makeText(this, "Please enter a review", Toast.LENGTH_SHORT).show();
                return;
            }
            saveUserReview(reviewText);
        });

        // Firestore review listener
        startReviewListener();
        fetchCurrentUserName();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reviewsListener != null) {
            reviewsListener.remove();
        }
    }

    private void startReviewListener() {
        if (bookTitle == null || bookTitle.isEmpty()) {
            textReviewsEmpty.setVisibility(View.VISIBLE);
            return;
        }
        reviewsListener = firestore.collection("bookReviews")
                .document(bookTitle)
                .collection("items")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    layoutReviews.removeAllViews();
                    if (error != null) {
                        Toast.makeText(this, "Failed to load reviews: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        textReviewsEmpty.setVisibility(View.VISIBLE);
                        return;
                    }
                    if (snapshot == null || snapshot.isEmpty()) {
                        // Show baked-in samples only when Firestore is empty
                        showFallbackReviews();
                        return;
                    }

                    textReviewsEmpty.setVisibility(View.GONE);
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String reviewer = doc.getString("userName");
                        String body = doc.getString("body");
                        Timestamp createdAt = doc.getTimestamp("createdAt");
                        String meta = createdAt != null ? formatTimestamp(createdAt.toDate()) : "";
                        addReview(layoutReviews,
                                reviewer != null ? reviewer : "Guest",
                                body != null ? body : "",
                                meta);
                    }
                });
    }

    private void addReview(LinearLayout container, String reviewer, String body, String meta) {
        View reviewItem = getLayoutInflater().inflate(R.layout.item_review_preview, container, false);
        TextView textReviewer = reviewItem.findViewById(R.id.textReviewerName);
        TextView textReviewBody = reviewItem.findViewById(R.id.textReviewBody);
        TextView textReviewMeta = reviewItem.findViewById(R.id.textReviewMeta);
        textReviewer.setText(reviewer);
        textReviewBody.setText(body);
        textReviewMeta.setText(meta);
        container.addView(reviewItem);
    }

    private void saveUserReview(String reviewText) {
        if (bookTitle == null || reviewText == null || reviewText.isEmpty()) return;

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please sign in to leave a review", Toast.LENGTH_SHORT).show();
            return;
        }

        buttonSubmitReview.setEnabled(false);
        Map<String, Object> reviewData = new HashMap<>();
        reviewData.put("userId", user.getUid());
        reviewData.put("userName", currentUserName != null ? currentUserName : "Guest");
        reviewData.put("body", reviewText);
        reviewData.put("createdAt", FieldValue.serverTimestamp());

        firestore.collection("bookReviews")
                .document(bookTitle)
                .collection("items")
                .add(reviewData)
                .addOnSuccessListener(docRef -> {
                    editReview.setText("");
                    Toast.makeText(this, "Review saved", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to save review: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                )
                .addOnCompleteListener(task -> buttonSubmitReview.setEnabled(true));
    }

    private void fetchCurrentUserName() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) return;

        usersRef.child(user.getUid())
                .child("userName")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        String name = snapshot.getValue(String.class);
                        if (name != null && !name.isEmpty()) {
                            currentUserName = name;
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) { }
                });
    }

    private void showFallbackReviews() {
        if (detailData == null || detailData.reviews.length == 0) {
            textReviewsEmpty.setVisibility(View.VISIBLE);
            return;
        }
        textReviewsEmpty.setVisibility(View.GONE);
        for (String review : detailData.reviews) {
            String[] parts = review.split("\\|", 3);
            String reviewer = parts.length > 0 ? parts[0] : "";
            String body = parts.length > 1 ? parts[1] : "";
            String meta = parts.length > 2 ? parts[2] : "";
            addReview(layoutReviews, reviewer, body, meta);
        }
    }

    private String formatTimestamp(Date date) {
        return new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault()).format(date);
    }

    private BookDetailData getDetailData(String title) {
        if (title == null) return null;
        Map<String, BookDetailData> data = new HashMap<>();
        data.put("나미야 잡화점의 기적", new BookDetailData(
                "과거와 현재가 뒤섞여저서 평온한 마을에 작은 기적이 이루어진다는 따뜻한 이야기입니다.",
                new String[]{"미스터리", "따뜻한", "기적", "시간여행"},
                new String[]{
                        "윤지|선물과 같은 책이어서 감동적입니다.|2024.11.01 · 좋아요12",
                        "정호|어른들을 위한 동화 같은 느낌이에요.|2024.10.20 · 좋아요8"
                }
        ));
        data.put("해리 포터와 마법사의 돌", new BookDetailData(
                "마법세계로 초대한 해리의 모험과 우정이 아름답게 그려지는 작품입니다.",
                new String[]{"판타지", "마법", "모험"},
                new String[]{
                        "민수|어릴 때부터 여러 번 읽었는데도 새로워요.|2024.09.10 · 좋아요20"
                }
        ));
        data.put("해리 포터와 비밀의 방", new BookDetailData(
                "초대한 해리의 두 번째 모험, 비밀의 방을 둘러싼 미스터리가 스릴 넘치게 전개됩니다.",
                new String[]{"판타지", "미스터리", "마법"},
                null
        ));
        data.put("어린 왕자", new BookDetailData(
                "사막에서 만난 어린 왕자가 남긴 사랑과 삶에 대한 메시지로 가득한 동화입니다.",
                new String[]{"감동", "철학", "성장"},
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

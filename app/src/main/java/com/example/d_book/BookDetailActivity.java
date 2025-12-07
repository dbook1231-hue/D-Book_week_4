package com.example.d_book;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
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
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
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
    private ListenerRegistration favoriteListener;
    private FirebaseUser currentUser;
    private String currentUserName = "Guest";
    private String bookTitle;

    private LinearLayout layoutReviews;
    private TextView textReviewsEmpty;
    private TextInputEditText editReview;
    private MaterialButton buttonSubmitReview;
    private ImageButton buttonFavorite;
    private boolean isFavorite = false;
    private BookDetailData detailData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        // Firebase 초기화
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        currentUser = firebaseAuth.getCurrentUser();

        // Toolbar 설정
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.title_book_detail));
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // UI 컴포넌트 초기화
        layoutReviews = findViewById(R.id.layoutReviews);
        textReviewsEmpty = findViewById(R.id.textReviewsEmpty);
        editReview = findViewById(R.id.editReview);
        buttonSubmitReview = findViewById(R.id.buttonSubmitReview);

        TextView textTitle = findViewById(R.id.textBookTitle);
        TextView textAuthor = findViewById(R.id.textBookAuthor);
        TextView textSummary = findViewById(R.id.textBookSummary);
        ChipGroup chipGroupKeywords = findViewById(R.id.chipGroupKeywords);
        ImageView imageCover = findViewById(R.id.imageCoverLarge);
        buttonFavorite = findViewById(R.id.buttonFavorite);

        // Intent에서 데이터 가져오기
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

        if (currentUser != null && bookTitle != null) {
            registerFavoriteListener();
        }

        buttonFavorite.setOnClickListener(v -> {
            if (currentUser != null) toggleFavoriteFirestore();
            else Toast.makeText(this, "Please sign in to use favorites", Toast.LENGTH_SHORT).show();
        });

        // 상세 데이터 가져오기
        detailData = getDetailData(title);
        textSummary.setText(detailData.summary);

        chipGroupKeywords.removeAllViews();
        for (String keyword : detailData.keywords) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.item_keyword_chip, chipGroupKeywords, false);
            chip.setText(keyword);
            chipGroupKeywords.addView(chip);
        }

        buttonSubmitReview.setOnClickListener(v -> {
            String reviewText = editReview.getText() != null ? editReview.getText().toString().trim() : "";
            if (reviewText.isEmpty()) {
                Toast.makeText(this, "Please enter a review", Toast.LENGTH_SHORT).show();
                return;
            }
            saveUserReview(reviewText, null);
            editReview.setText("");
        });

        fetchCurrentUserName();
        startReviewListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reviewsListener != null) reviewsListener.remove();
        if (favoriteListener != null) favoriteListener.remove();
    }

    /** 즐겨찾기 상태 리스너 등록 */
    private void registerFavoriteListener() {
        DocumentReference favRef = firestore.collection("users")
                .document(currentUser.getUid())
                .collection("favorites")
                .document(bookTitle);

        favoriteListener = favRef.addSnapshotListener((snapshot, error) -> {
            if (error != null) return;
            isFavorite = snapshot != null && snapshot.exists();
            updateFavoriteButton();
        });
    }

    /** 즐겨찾기 토글 */
    private void toggleFavoriteFirestore() {
        if (currentUser == null || bookTitle == null) return;

        DocumentReference favRef = firestore.collection("users")
                .document(currentUser.getUid())
                .collection("favorites")
                .document(bookTitle);

        if (isFavorite) {
            favRef.delete().addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "즐겨찾기에서 제거되었습니다", Toast.LENGTH_SHORT).show();
                updateFavoriteCount(false);
            });
        } else {
            String author = getIntent().getStringExtra("author");
            String thumbnail = getIntent().getStringExtra("thumbnail");
            String category = getIntent().getStringExtra("category");

            Map<String, Object> data = new HashMap<>();
            data.put("addedAt", FieldValue.serverTimestamp());
            data.put("title", bookTitle);
            data.put("author", author);
            data.put("thumbnail", thumbnail);
            data.put("category", category);

            favRef.set(data).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "즐겨찾기에 추가되었습니다", Toast.LENGTH_SHORT).show();
                updateFavoriteCount(true);
            });
        }
    }

    /** 즐겨찾기 카운트 업데이트 */
    private void updateFavoriteCount(boolean increment) {
        if (currentUser == null) return;
        DatabaseReference favoriteCountRef = usersRef.child(currentUser.getUid()).child("favoriteCount");
        favoriteCountRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                Long count = currentData.getValue(Long.class);
                if (count == null) count = 0L;
                currentData.setValue(increment ? count + 1 : Math.max(count - 1, 0));
                return Transaction.success(currentData);
            }
            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {}
        });
    }

    /** 즐겨찾기 버튼 UI 업데이트 */
    private void updateFavoriteButton() {
        if (isFavorite) {
            buttonFavorite.setImageResource(R.drawable.ic_favorite_border_filled);
            buttonFavorite.setColorFilter(getResources().getColor(R.color.primaryYellow));
        } else {
            buttonFavorite.setImageResource(R.drawable.ic_favorite_border);
            buttonFavorite.setColorFilter(getResources().getColor(R.color.primaryBlue));
        }
    }

    /** 리뷰 실시간 리스너 */
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
                        showFallbackReviews();
                        return;
                    }

                    textReviewsEmpty.setVisibility(View.GONE);

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String parentId = doc.getString("parentReviewId");
                        if (parentId != null && !parentId.isEmpty()) continue;
                        addReview(layoutReviews, doc, 0);
                    }
                });
    }

    /** 리뷰 항목 추가 */
    private void addReview(LinearLayout container, DocumentSnapshot doc, int depth) {
        View reviewItem = getLayoutInflater().inflate(R.layout.item_review_preview, container, false);
        bindReviewItem(reviewItem, doc, depth);
        container.addView(reviewItem);
    }

    /** 답글 항목 추가 */
    private void addChildReply(LinearLayout container, DocumentSnapshot doc, int depth) {
        View replyItem = getLayoutInflater().inflate(R.layout.item_review_preview, container, false);
        replyItem.setTag(doc.getId());
        bindReviewItem(replyItem, doc, depth);
        container.addView(replyItem);
    }

    /** 리뷰/답글 바인딩 */
    private void bindReviewItem(View itemView, DocumentSnapshot doc, int depth) {
        TextView textReviewer = itemView.findViewById(R.id.textReviewerName);
        TextView textReviewBody = itemView.findViewById(R.id.textReviewBody);
        TextView textReviewMeta = itemView.findViewById(R.id.textReviewMeta);
        TextView textReplyCount = itemView.findViewById(R.id.textReplyCount);

        LinearLayout btnLike = itemView.findViewById(R.id.btnLike);
        LinearLayout btnDislike = itemView.findViewById(R.id.btnDislike);
        LinearLayout btnReply = itemView.findViewById(R.id.btnReply);
        LinearLayout btnDelete = itemView.findViewById(R.id.btnDelete);

        LinearLayout layoutReplyInput = itemView.findViewById(R.id.layoutReplyInput);
        TextInputEditText editReply = itemView.findViewById(R.id.editReply);
        MaterialButton buttonSubmitReply = itemView.findViewById(R.id.buttonSubmitReply);

        LinearLayout layoutChildReplies = itemView.findViewById(R.id.layoutChildReplies);

        layoutChildReplies.removeAllViews();
        layoutChildReplies.setVisibility(View.GONE);
        layoutReplyInput.setVisibility(View.GONE);

        ImageView likeIcon = btnLike.findViewById(R.id.imageLike);
        ImageView dislikeIcon = btnDislike.findViewById(R.id.imageDislike);

        TextView textLikeCount = btnLike.findViewById(R.id.textLikeCount);
        TextView textDislikeCount = btnDislike.findViewById(R.id.textDislikeCount);

        String reviewer = doc.getString("userName");
        String body = doc.getString("body");
        Timestamp createdAt = doc.getTimestamp("createdAt");
        String meta = createdAt != null ? formatTimestamp(createdAt.toDate()) : "";
        String userId = doc.getString("userId");

        Map<String, Boolean> likes = (Map<String, Boolean>) doc.get("likes");
        Map<String, Boolean> dislikes = (Map<String, Boolean>) doc.get("dislikes");

        textReviewer.setText(reviewer != null ? reviewer : "Guest");
        textReviewBody.setText(body != null ? body : "");
        textReviewMeta.setText(meta);

        String currentUid = currentUser != null ? currentUser.getUid() : "";
        boolean liked = likes != null && likes.containsKey(currentUid);
        boolean disliked = dislikes != null && dislikes.containsKey(currentUid);

        likeIcon.setImageResource(liked ? R.drawable.ic_like_filled : R.drawable.ic_like);
        dislikeIcon.setImageResource(disliked ? R.drawable.ic_dislike_filled : R.drawable.ic_dislike);

        textLikeCount.setText(String.valueOf(likes != null ? likes.size() : 0));
        textDislikeCount.setText(String.valueOf(dislikes != null ? dislikes.size() : 0));

        btnDelete.setVisibility(userId.equals(currentUid) ? View.VISIBLE : View.GONE);

        DocumentReference reviewRef = firestore.collection("bookReviews")
                .document(bookTitle)
                .collection("items")
                .document(doc.getId());

        btnLike.setOnClickListener(v -> toggleLikeDislike(reviewRef, true));
        btnDislike.setOnClickListener(v -> toggleLikeDislike(reviewRef, false));

        // 삭제
        btnDelete.setOnClickListener(v -> deleteReviewRecursive(reviewRef));

        if (depth == 0) {
            btnReply.setOnClickListener(v -> {
                boolean show = layoutChildReplies.getVisibility() == View.GONE;
                layoutChildReplies.setVisibility(show ? View.VISIBLE : View.GONE);
                layoutReplyInput.setVisibility(show ? View.VISIBLE : View.GONE);
                if (show) startChildReplyListener(layoutChildReplies, doc.getId(), depth + 1, textReplyCount);
            });
        } else {
            btnReply.setVisibility(View.GONE);
        }

        buttonSubmitReply.setOnClickListener(v -> submitReply(editReply, doc.getId()));

        // 들여쓰기
        if (depth > 0) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) itemView.getLayoutParams();
            params.setMarginStart(depth * 50);
            itemView.setLayoutParams(params);
        }

        // 답글 카운트 업데이트
        firestore.collection("bookReviews")
                .document(bookTitle)
                .collection("items")
                .whereEqualTo("parentReviewId", doc.getId())
                .get()
                .addOnSuccessListener(querySnapshot -> textReplyCount.setText(String.valueOf(querySnapshot != null ? querySnapshot.size() : 0)));
    }

    /** 하위 답글 포함 삭제 + 다른 사용자 통계 반영 */
    /** 하위 답글 포함 삭제 + 다른 사용자 통계 반영 */
    private void deleteReviewRecursive(DocumentReference reviewRef) {
        reviewRef.get().addOnSuccessListener(docSnapshot -> {
            if (!docSnapshot.exists()) return;

            String reviewUserId = docSnapshot.getString("userId");
            boolean isMainReview = docSnapshot.getString("parentReviewId") == null;
            Map<String, Boolean> likes = (Map<String, Boolean>) docSnapshot.get("likes");

            // 1️⃣ 하위 답글 삭제 재귀
            firestore.collection("bookReviews")
                    .document(bookTitle)
                    .collection("items")
                    .whereEqualTo("parentReviewId", docSnapshot.getId())
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (querySnapshot != null) {
                            for (DocumentSnapshot replyDoc : querySnapshot.getDocuments()) {
                                deleteReviewRecursive(replyDoc.getReference());
                            }
                        }

                        // 2️⃣ 좋아요 누른 모든 사용자 카운트 감소
                        if (likes != null) {
                            for (String likerId : likes.keySet()) {
                                decrementUserCount(likerId, "likeCount");
                            }
                        }

                        // 3️⃣ 작성자 통계 감소
                        if (isMainReview) decrementUserCount(reviewUserId, "reviewCount");
                        else decrementUserCount(reviewUserId, "replyCount");

                        // 4️⃣ 문서 삭제
                        reviewRef.delete();
                    });
        });
    }


    private void startChildReplyListener(LinearLayout container, String parentId, int depth, TextView textReplyCount) {
        firestore.collection("bookReviews")
                .document(bookTitle)
                .collection("items")
                .whereEqualTo("parentReviewId", parentId)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (snapshot == null) return;
                    textReplyCount.setText(String.valueOf(snapshot.size()));
                    for (DocumentSnapshot replyDoc : snapshot.getDocuments()) {
                        if (container.findViewWithTag(replyDoc.getId()) != null) continue;
                        addChildReply(container, replyDoc, depth);
                    }
                });
    }

    private void toggleLikeDislike(DocumentReference reviewRef, boolean like) {
        if (currentUser == null) return;

        String currentUid = currentUser.getUid();
        firestore.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(reviewRef);

            Map<String, Boolean> currentLikes = snapshot.contains("likes") ? (Map<String, Boolean>) snapshot.get("likes") : new HashMap<>();
            Map<String, Boolean> currentDislikes = snapshot.contains("dislikes") ? (Map<String, Boolean>) snapshot.get("dislikes") : new HashMap<>();

            boolean previouslyLiked = currentLikes.containsKey(currentUid);
            boolean previouslyDisliked = currentDislikes.containsKey(currentUid);

            if (like) {
                if (previouslyLiked) currentLikes.remove(currentUid);
                else {
                    currentLikes.put(currentUid, true);
                    currentDislikes.remove(currentUid);
                }
            } else {
                if (previouslyDisliked) currentDislikes.remove(currentUid);
                else {
                    currentDislikes.put(currentUid, true);
                    currentLikes.remove(currentUid);
                }
            }

            transaction.update(reviewRef, "likes", currentLikes);
            transaction.update(reviewRef, "dislikes", currentDislikes);

            // 사용자 likeCount 업데이트
            DatabaseReference likeCountRef = usersRef.child(currentUid).child("likeCount");
            likeCountRef.runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData currentData) {
                    Long count = currentData.getValue(Long.class);
                    if (count == null) count = 0L;
                    if (like) {
                        if (previouslyDisliked) count += 1;
                        else if (previouslyLiked) count = Math.max(count - 1, 0);
                        else count += 1;
                    } else if (previouslyLiked) {
                        count = Math.max(count - 1, 0);
                    }
                    currentData.setValue(count);
                    return Transaction.success(currentData);
                }
                @Override
                public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {}
            });

            return null;
        });
    }

    private void saveUserReview(String reviewText, @Nullable String parentReviewId) {
        if (currentUser == null || bookTitle == null) return;

        boolean isReply = parentReviewId != null;
        String reviewId = firestore.collection("bookReviews")
                .document(bookTitle)
                .collection("items")
                .document().getId();

        Map<String, Object> reviewData = new HashMap<>();
        reviewData.put("userId", currentUser.getUid());
        reviewData.put("userName", currentUserName);
        reviewData.put("body", reviewText);
        reviewData.put("createdAt", FieldValue.serverTimestamp());
        reviewData.put("parentReviewId", parentReviewId);

        firestore.collection("bookReviews")
                .document(bookTitle)
                .collection("items")
                .document(reviewId)
                .set(reviewData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, isReply ? "답글이 등록되었습니다" : "리뷰가 등록되었습니다", Toast.LENGTH_SHORT).show();
                    if (!isReply) incrementUserCount("reviewCount");
                });
    }

    private void fetchCurrentUserName() {
        if (currentUser == null) return;
        usersRef.child(currentUser.getUid())
                .child("userName")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        String name = snapshot.getValue(String.class);
                        if (name != null && !name.isEmpty()) currentUserName = name;
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
    }

    private void submitReply(TextInputEditText editReply, String parentReviewId) {
        String replyText = editReply.getText() != null ? editReply.getText().toString().trim() : "";
        if (replyText.isEmpty()) return;
        saveUserReview(replyText, parentReviewId);
        editReply.setText("");
        incrementUserCount("replyCount");
    }

    /** 사용자 카운트 증가 */
    private void incrementUserCount(String field) {
        incrementUserCount(currentUser.getUid(), field, 1);
    }

    private void incrementUserCount(String userId, String field, int amount) {
        DatabaseReference ref = usersRef.child(userId).child(field);
        ref.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                Long count = currentData.getValue(Long.class);
                if (count == null) count = 0L;
                currentData.setValue(count + amount);
                return Transaction.success(currentData);
            }
            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {}
        });
    }

    /** 사용자 카운트 감소 */
    private void decrementUserCount(String field) {
        decrementUserCount(currentUser.getUid(), field, 1);
    }

    private void decrementUserCount(String userId, String field) {
        decrementUserCount(userId, field, 1);
    }

    private void decrementUserCount(String userId, String field, int amount) {
        DatabaseReference ref = usersRef.child(userId).child(field);
        ref.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                Long count = currentData.getValue(Long.class);
                if (count == null) count = 0L;
                currentData.setValue(Math.max(count - amount, 0));
                return Transaction.success(currentData);
            }
            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {}
        });
    }

    private String formatTimestamp(Date date) {
        return new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault()).format(date);
    }

    private void showFallbackReviews() {
        textReviewsEmpty.setVisibility(View.VISIBLE);
    }

    private BookDetailData getDetailData(String title) {
        String summary = getString(R.string.book_detail_summary_placeholder);
        String[] keywords = getResources().getStringArray(R.array.book_detail_keywords_sample);
        String[] reviews = getResources().getStringArray(R.array.book_detail_reviews_sample);
        return new BookDetailData(summary, keywords, reviews);
    }

    private static class BookDetailData {
        final String summary;
        final String[] keywords;
        final String[] reviews;

        BookDetailData(String summary, String[] keywords, String[] reviews) {
            this.summary = summary != null ? summary : "";
            this.keywords = keywords != null ? keywords : new String[0];
            this.reviews = reviews != null ? reviews : new String[0];
        }
    }
}

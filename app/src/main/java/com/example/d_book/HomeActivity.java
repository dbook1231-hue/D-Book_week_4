package com.example.d_book;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.d_book.adapter.TrendingBookAdapter;
import com.example.d_book.adapter.SearchResultAdapter;
import com.example.d_book.item.SearchResultItem;
import com.example.d_book.item.TrendingBook;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private final android.os.Handler bannerHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private ViewPager2 bannerPagerRef;
    private TrendingBookAdapter trendingAdapter;
    private final List<TrendingBook> trendingBooks = new ArrayList<>();

    private final Runnable bannerRunnable = new Runnable() {
        @Override
        public void run() {
            ViewPager2 pager = findViewById(R.id.bannerPager);
            if (pager != null && pager.getAdapter() != null) {
                int next = (pager.getCurrentItem() + 1) % pager.getAdapter().getItemCount();
                pager.setCurrentItem(next, true);
                bannerHandler.postDelayed(this, 5000);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 🔹 검색
        TextInputEditText editSearch = findViewById(R.id.editSearch);
        MaterialButton buttonSearch = findViewById(R.id.buttonSearch);
        editSearch.setOnEditorActionListener((v, actionId, event) -> {
            openSearchWithQuery(v.getText());
            return true;
        });
        if (buttonSearch != null) buttonSearch.setOnClickListener(v -> openSearchWithQuery(editSearch.getText()));

        // 🔹 배너
        ViewPager2 bannerPager = findViewById(R.id.bannerPager);
        this.bannerPagerRef = bannerPager;
        LinearLayout bannerDots = findViewById(R.id.bannerDots);
        List<Banner> bannerData = new ArrayList<>();
        bannerData.add(new Banner(R.drawable.banner_winter, "따뜻한 한 권으로 채우는 겨울 밤"));
        bannerData.add(new Banner(R.drawable.banner_magic, "마법 같은 모험이 시작돼요"));
        bannerData.add(new Banner(R.drawable.banner_letter, "편지처럼 마음을 건네는 이야기"));
        bannerPager.setAdapter(new BannerAdapter(bannerData));
        setupDots(bannerDots, bannerData.size());
        bannerPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateDots(bannerDots, position);
            }
        });
        updateDots(bannerDots, 0);
        bannerHandler.postDelayed(bannerRunnable, 5000);

        // 🔹 Firestore 실시간 트렌딩 책
        RecyclerView trendingRecycler = findViewById(R.id.recyclerTrending);
        trendingRecycler.setLayoutManager(new LinearLayoutManager(this));
        trendingAdapter = new TrendingBookAdapter(this, trendingBooks, item -> {
            incrementVisitCount(item);
            openSearchWithQuery(item.getTitle());
        });
        trendingRecycler.setAdapter(trendingAdapter);
        loadTrendingBooksFromFirestore();

        // 🔹 추천 책
        RecyclerView recommendationsRecycler = findViewById(R.id.recyclerRecommendations);
        recommendationsRecycler.setLayoutManager(new LinearLayoutManager(this));
        SearchResultAdapter recommendationsAdapter = new SearchResultAdapter(this,
                createRecommendationsFromReviews(),
                item -> openSearchWithQuery(item.getTitle()));
        recommendationsRecycler.setAdapter(recommendationsAdapter);

        // 🔹 즐겨찾기 / 최근
        View cardFavorites = findViewById(R.id.cardFavorites);
        View cardRecent = findViewById(R.id.cardRecent);
        cardFavorites.setOnClickListener(v -> {
            if (FirebaseAuth.getInstance().getCurrentUser() == null)
                startActivity(new Intent(this, LoginActivity.class));
            else
                Toast.makeText(this, getString(R.string.label_favorites), Toast.LENGTH_SHORT).show();
        });
        cardRecent.setOnClickListener(v -> startActivity(new Intent(this, UploadBooksActivity.class)));

        // 🔹 하단 네비게이션
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_home);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) return true;
            if (id == R.id.nav_settings) startActivity(new Intent(this, SettingsActivity.class));
            if (id == R.id.nav_books) startActivity(new Intent(this, SearchActivity.class));
            return false;
        });

        setupQuickRead();
    }

    // 🔹 Firestore 실시간 트렌딩 책 로드 (상위 10권만)
    private void loadTrendingBooksFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("books")
                .orderBy("visitCount", Query.Direction.DESCENDING)
                .limit(10) // ← 여기서 상위 10권만 가져오도록 제한
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;
                    trendingBooks.clear();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        String title = doc.getString("title");
                        String author = doc.getString("author");
                        Long visitCount = doc.getLong("visitCount");
                        String thumbnail = doc.getString("thumbnail");

                        TrendingBook book = new TrendingBook(
                                title,
                                author,
                                visitCount != null ? visitCount.intValue() : 0,
                                thumbnail
                        );

                        trendingBooks.add(book);
                    }
                    trendingAdapter.notifyDataSetChanged();
                });
    }

    // 🔹 클릭 시 방문수 증가
    private void incrementVisitCount(TrendingBook item) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("books")
                .whereEqualTo("title", item.getTitle())
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        long current = doc.getLong("visitCount") != null ? doc.getLong("visitCount") : 0;
                        doc.getReference().update("visitCount", current + 1);
                    }
                });
    }

    private void openSearchWithQuery(CharSequence searchText) {
        Intent intent = new Intent(this, SearchActivity.class);
        if (searchText != null && searchText.length() > 0)
            intent.putExtra("query", searchText.toString());
        startActivity(intent);
    }

    // 🔹 추천 책 생성
    private List<SearchResultItem> createRecommendationsFromReviews() {
        List<String> reviews = getSavedReviews();
        List<SearchResultItem> recs = new ArrayList<>();
        for (String review : reviews) {
            String lower = review.toLowerCase();
            if (lower.contains("마법") || lower.contains("호그와트") || lower.contains("마법사")) {
                addIfNotExists(recs, new SearchResultItem("해리 포터와 비밀의 방", "J.K. 롤링", "https://covers.openlibrary.org/b/isbn/9780439064873-L.jpg", "소설"));
                addIfNotExists(recs, new SearchResultItem("해리 포터와 불의 잔", "J.K. 롤링", "https://covers.openlibrary.org/b/isbn/9780439139601-L.jpg", "소설"));
            }
            if (lower.contains("따뜻") || lower.contains("위로") || lower.contains("편지") || lower.contains("잡화점")) {
                addIfNotExists(recs, new SearchResultItem("나미야 잡화점의 기적", "히가시노 게이고", R.drawable.namiya_cover, "소설"));
            }
            if (lower.contains("모험") || lower.contains("판타지") || lower.contains("여정")) {
                addIfNotExists(recs, new SearchResultItem("반지의 제왕: 반지 원정대", "J.R.R. 톨킨", "https://covers.openlibrary.org/b/isbn/9780547928210-L.jpg", "소설"));
            }
        }
        if (recs.isEmpty()) {
            recs.add(new SearchResultItem("위대한 개츠비", "F. 스콧 피츠제럴드", "https://covers.openlibrary.org/b/isbn/9780743273565-L.jpg", "소설"));
            recs.add(new SearchResultItem("어린 왕자", "앙투안 드 생텍쥐페리", "https://covers.openlibrary.org/b/isbn/9780156012195-L.jpg", "에세이"));
        }
        return recs;
    }

    private List<String> getSavedReviews() {
        SharedPreferences prefs = getSharedPreferences("user_reviews", MODE_PRIVATE);
        String data = prefs.getString("reviews", "");
        if (TextUtils.isEmpty(data)) return new ArrayList<>();
        String[] lines = data.split("\\n");
        List<String> list = new ArrayList<>();
        for (String line : lines) {
            String[] parts = line.split("\\|", 2);
            if (parts.length == 2) list.add(parts[1]);
        }
        return list;
    }

    private void addIfNotExists(List<SearchResultItem> list, SearchResultItem item) {
        for (SearchResultItem existing : list) {
            if (existing.getTitle().equals(item.getTitle())) return;
        }
        list.add(item);
    }

    private void setupDots(LinearLayout container, int count) {
        container.removeAllViews();
        for (int i = 0; i < count; i++) {
            View dot = getLayoutInflater().inflate(R.layout.tab_dot, container, false);
            dot.setSelected(i == 0);
            final int index = i;
            dot.setOnClickListener(v -> {
                if (bannerPagerRef != null && bannerPagerRef.getAdapter() != null) {
                    bannerPagerRef.setCurrentItem(index, true);
                    updateDots(container, index);
                    bannerHandler.removeCallbacks(bannerRunnable);
                    bannerHandler.postDelayed(bannerRunnable, 5000);
                }
            });
            container.addView(dot);
        }
    }

    private void updateDots(LinearLayout container, int selected) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            child.setSelected(i == selected);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bannerHandler.removeCallbacks(bannerRunnable);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_notifications) {
            startActivity(new Intent(this, NotificationsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // 🔹 QuickRead 카드 드래그
    private void setupQuickRead() {
        MaterialCardView quickCard = findViewById(R.id.quickReadCard);
        if (quickCard == null) return;
        final View dragView = quickCard;
        final int[] lastAction = {0};
        final float[] dX = new float[1];
        final float[] dY = new float[1];

        dragView.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    dX[0] = v.getX() - event.getRawX();
                    dY[0] = v.getY() - event.getRawY();
                    lastAction[0] = android.view.MotionEvent.ACTION_DOWN;
                    return true;
                case android.view.MotionEvent.ACTION_MOVE:
                    v.setX(event.getRawX() + dX[0]);
                    v.setY(event.getRawY() + dY[0]);
                    lastAction[0] = android.view.MotionEvent.ACTION_MOVE;
                    return true;
                case android.view.MotionEvent.ACTION_UP:
                    if (lastAction[0] == android.view.MotionEvent.ACTION_DOWN) {
                        Toast.makeText(this, "마지막 책 이어읽기", Toast.LENGTH_SHORT).show();
                    }
                    snapQuickButtonToEdge(v);
                    return true;
            }
            return false;
        });
    }

    private void snapQuickButtonToEdge(View v) {
        View parent = findViewById(R.id.homeContentContainer);
        if (parent == null) return;
        int parentWidth = parent.getWidth();
        int parentHeight = parent.getHeight();
        int viewWidth = v.getWidth();
        int viewHeight = v.getHeight();

        float targetX;
        float targetY = v.getY();

        float centerX = v.getX() + viewWidth / 2f;
        boolean snapToRight = centerX > parentWidth / 2f;
        targetX = snapToRight ? parentWidth - viewWidth - 16f : 16f;

        if (targetY < 16f) targetY = 16f;
        if (targetY > parentHeight - viewHeight - 16f) targetY = parentHeight - viewHeight - 16f;

        v.animate()
                .x(targetX)
                .y(targetY)
                .setDuration(180)
                .start();
    }
}

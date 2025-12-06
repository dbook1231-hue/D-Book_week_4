package com.example.d_book;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.d_book.adapter.SearchResultAdapter;
import com.example.d_book.adapter.TrendingBookAdapter;
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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    // ============================
    // 배너 자동 롤링 관련
    // ============================
    // 메인 스레드에서 배너 자동 롤링을 위한 Handler
    private final android.os.Handler bannerHandler = new android.os.Handler(android.os.Looper.getMainLooper());

    // ViewPager2 참조 변수
    private ViewPager2 bannerPagerRef;

    // 인기 책 리스트 RecyclerView Adapter
    private TrendingBookAdapter trendingAdapter;

    // 인기 책 데이터 저장 리스트
    private final List<TrendingBook> trendingBooks = new ArrayList<>();

    // 배너 자동 스크롤 Runnable
    private final Runnable bannerRunnable = new Runnable() {
        @Override
        public void run() {
            if (bannerPagerRef != null && bannerPagerRef.getAdapter() != null) {
                // 현재 배너의 다음 인덱스로 이동
                int next = (bannerPagerRef.getCurrentItem() + 1) % bannerPagerRef.getAdapter().getItemCount();
                bannerPagerRef.setCurrentItem(next, true);
                // 5초 후 다시 실행
                bannerHandler.postDelayed(this, 5000);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // ============================
        // 상단 툴바 설정
        // ============================
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // ============================
        // 검색 기능
        // ============================
        TextInputEditText editSearch = findViewById(R.id.editSearch);
        MaterialButton buttonSearch = findViewById(R.id.buttonSearch);

        // 키보드 검색 버튼 클릭 시
        editSearch.setOnEditorActionListener((v, a, e) -> {
            openSearchWithQuery(v.getText());
            return true;
        });

        // 검색 버튼 클릭 시
        if (buttonSearch != null)
            buttonSearch.setOnClickListener(v -> openSearchWithQuery(editSearch.getText()));

        // ============================
        // 배너 및 인기 책 초기화
        // ============================
        setupBanner();
        setupTrendingBooks();

        // ============================
        // 실시간 추천 책 RecyclerView
        // ============================
        RecyclerView recommendationsRecycler = findViewById(R.id.recyclerRecommendations);
        recommendationsRecycler.setLayoutManager(new LinearLayoutManager(this));
        loadRealtimeRecommendations(recommendationsRecycler);

        // ============================
        // 즐겨찾기 버튼 클릭
        // ============================
        findViewById(R.id.cardFavorites).setOnClickListener(v -> {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
            } else {
                startActivity(new Intent(this, FavoritesActivity.class));
            }
        });

        // 최근 업로드 책 버튼 클릭
        findViewById(R.id.cardRecent).setOnClickListener(v ->
                startActivity(new Intent(this, UploadBooksActivity.class))
        );

        // ============================
        // 하단 네비게이션 설정
        // ============================
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_home);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) return true; // 현재 홈이면 아무 동작 없음
            if (id == R.id.nav_settings)
                startActivity(new Intent(this, SettingsActivity.class));
            if (id == R.id.nav_books)
                startActivity(new Intent(this, SearchActivity.class));
            return false;
        });

        // ============================
        // QuickRead 드래그 버튼 초기화
        // ============================
        setupQuickRead();
    }

    // =====================================================
    // 배너 관련 메서드
    // =====================================================
    private void setupBanner() {
        ViewPager2 bannerPager = findViewById(R.id.bannerPager);
        this.bannerPagerRef = bannerPager;

        LinearLayout bannerDots = findViewById(R.id.bannerDots);

        // 배너 데이터 생성
        List<Banner> bannerData = new ArrayList<>();
        bannerData.add(new Banner(R.drawable.banner_winter, "따뜻한 한 권으로 채우는 겨울 밤"));
        bannerData.add(new Banner(R.drawable.banner_magic, "마법 같은 모험이 시작돼요"));
        bannerData.add(new Banner(R.drawable.banner_letter, "편지처럼 마음을 건네는 이야기"));

        // Adapter 설정
        bannerPager.setAdapter(new BannerAdapter(bannerData));

        // 배너 점(dot) 표시
        setupDots(bannerDots, bannerData.size());

        // 배너 스크롤 시 점 업데이트
        bannerPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateDots(bannerDots, position);
            }
        });

        // 초기 점 상태 업데이트
        updateDots(bannerDots, 0);

        // 배너 자동 스크롤 시작
        bannerHandler.postDelayed(bannerRunnable, 5000);
    }

    // =====================================================
    // 인기 책 관련 메서드
    // =====================================================
    private void setupTrendingBooks() {
        RecyclerView trendingRecycler = findViewById(R.id.recyclerTrending);
        trendingRecycler.setLayoutManager(new LinearLayoutManager(this));

        // Adapter 생성 및 클릭 이벤트 정의
        trendingAdapter = new TrendingBookAdapter(this, trendingBooks,
                item -> {
                    incrementVisitCount(item); // 클릭 시 방문 수 증가
                    openSearchWithQuery(item.getTitle()); // 검색 화면으로 이동
                });

        trendingRecycler.setAdapter(trendingAdapter);

        // Firestore에서 인기 책 로드
        loadTrendingBooksFromFirestore();
    }

    private void loadTrendingBooksFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("books")
                .orderBy("visitCount", Query.Direction.DESCENDING) // 방문수 기준 내림차순
                .limit(10) // 상위 10권
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;

                    trendingBooks.clear();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        TrendingBook book = new TrendingBook(
                                doc.getString("title"),
                                doc.getString("author"),
                                doc.getLong("visitCount") != null ? doc.getLong("visitCount").intValue() : 0,
                                doc.getString("thumbnail")
                        );
                        trendingBooks.add(book);
                    }
                    trendingAdapter.notifyDataSetChanged();
                });
    }

    // 인기 책 클릭 시 방문 수 증가
    private void incrementVisitCount(TrendingBook item) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("books")
                .whereEqualTo("title", item.getTitle())
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        DocumentSnapshot doc = snap.getDocuments().get(0);
                        long current = doc.getLong("visitCount") != null ? doc.getLong("visitCount") : 0;
                        doc.getReference().update("visitCount", current + 1);
                    }
                });
    }

    // =====================================================
    // 실시간 후기 기반 추천 (상위 3권)
    // =====================================================
    private static class BookScore {
        String title;
        double score; // 추천 점수
        String category;
        String description;
        List<String> keywords = new ArrayList<>();
        int visitCount = 0;

        BookScore(String t) { this.title = t; }
    }

    private void loadRealtimeRecommendations(RecyclerView recyclerView) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 책 컬렉션을 실시간 감시
        db.collection("books").addSnapshotListener((bookSnapshots, e) -> {
            if (e != null || bookSnapshots == null) return;

            List<BookScore> scoreList = new ArrayList<>();
            for (DocumentSnapshot bookDoc : bookSnapshots.getDocuments()) {
                String title = bookDoc.getString("title");
                if (title == null) continue;

                BookScore bs = new BookScore(title);
                bs.category = bookDoc.getString("category");
                bs.description = bookDoc.getString("description");
                bs.visitCount = bookDoc.getLong("visitCount") != null ? bookDoc.getLong("visitCount").intValue() : 0;

                Object kw = bookDoc.get("keywords");
                if (kw instanceof List) bs.keywords = (List<String>) kw;

                scoreList.add(bs);

                // 각 책의 리뷰 실시간 반영
                db.collection("bookReviews")
                        .document(title)
                        .collection("items")
                        .addSnapshotListener((reviewSnapshots, ex) -> {
                            if (ex != null || reviewSnapshots == null) return;

                            double likeScore = 0;
                            int replyCount = 0;

                            for (DocumentSnapshot r : reviewSnapshots.getDocuments()) {
                                Map<String, Boolean> likes = (Map<String, Boolean>) r.get("likes");
                                Map<String, Boolean> dislikes = (Map<String, Boolean>) r.get("dislikes");

                                int likeCnt = likes != null ? likes.size() : 0;
                                int dislikeCnt = dislikes != null ? dislikes.size() : 0;

                                // 추천 점수 계산: 좋아요*2 - 싫어요*1
                                likeScore += (likeCnt * 2.0) - (dislikeCnt * 1.0);

                                String parent = r.getString("parentReviewId");
                                if (parent != null && !parent.isEmpty()) replyCount++; // 답글 수 추가
                            }

                            // 최종 추천 점수
                            bs.score = likeScore + (replyCount * 1.5) + Math.log10(bs.visitCount + 10);

                            // TF-IDF 키워드 가중치
                            Map<String, Integer> dfMap = new HashMap<>();
                            for (BookScore s : scoreList) {
                                if (s.keywords != null) {
                                    for (String kwd : s.keywords) {
                                        dfMap.put(kwd, dfMap.getOrDefault(kwd, 0) + 1);
                                    }
                                }
                            }

                            int totalDocs = scoreList.size();
                            for (String kwd : bs.keywords) {
                                int df = dfMap.getOrDefault(kwd, 1);
                                double idf = Math.log((double) totalDocs / df);
                                bs.score += idf * 1.3; // TF-IDF 가중치
                            }

                            // 추천 UI 갱신
                            updateRecommendationUISafe(scoreList, recyclerView, db);
                        });
            }
        });
    }

    private void updateRecommendationUISafe(List<BookScore> scoreList, RecyclerView recyclerView, FirebaseFirestore db) {
        // 추천 점수 기준 내림차순 정렬
        scoreList.sort((a, b) -> Double.compare(b.score, a.score));

        // 상위 3권만 가져오기
        List<BookScore> topList = new ArrayList<>(scoreList.size() > 3 ? scoreList.subList(0, 3) : scoreList);

        List<SearchResultItem> results = new ArrayList<>();
        final int[] loadedCount = {0};

        // Firestore에서 상세 정보 가져오기
        for (BookScore s : topList) {
            db.collection("books")
                    .whereEqualTo("title", s.title)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (!snapshot.isEmpty()) {
                            DocumentSnapshot doc = snapshot.getDocuments().get(0);
                            results.add(new SearchResultItem(
                                    doc.getString("title"),
                                    doc.getString("author"),
                                    doc.getString("thumbnail"),
                                    doc.getString("category")
                            ));
                        }
                        loadedCount[0]++;
                        if (loadedCount[0] == topList.size()) {
                            // Adapter 설정
                            SearchResultAdapter adapter = new SearchResultAdapter(
                                    this,
                                    results,
                                    item -> openSearchWithQuery(item.getTitle())
                            );
                            recyclerView.setAdapter(adapter);
                        }
                    });
        }
    }

    // =====================================================
    // 공통 기능
    // =====================================================
    private void openSearchWithQuery(CharSequence searchText) {
        Intent i = new Intent(this, SearchActivity.class);
        if (!TextUtils.isEmpty(searchText))
            i.putExtra("query", searchText.toString());
        startActivity(i);
    }

    // 배너 점(dot) 설정
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
        for (int i = 0; i < container.getChildCount(); i++)
            container.getChildAt(i).setSelected(i == selected);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bannerHandler.removeCallbacks(bannerRunnable); // 메모리 누수 방지
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

    // =====================================================
    // QuickRead 드래그 기능
    // =====================================================
    private void setupQuickRead() {
        MaterialCardView quickCard = findViewById(R.id.quickReadCard);
        if (quickCard == null) return;

        final View drag = quickCard;
        final float[] dX = new float[1];
        final float[] dY = new float[1];
        final int[] lastAction = {0};

        drag.setOnTouchListener((v, event) -> {
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
                    // 클릭 시 마지막 책 이어읽기
                    if (lastAction[0] == android.view.MotionEvent.ACTION_DOWN) {
                        Toast.makeText(this, "마지막 책 이어읽기", Toast.LENGTH_SHORT).show();
                    }
                    snapQuickButtonToEdge(v); // 드래그 후 화면 끝으로 이동
                    return true;
            }
            return false;
        });
    }

    private void snapQuickButtonToEdge(View v) {
        View parent = findViewById(R.id.homeContentContainer);
        if (parent == null) return;

        int pw = parent.getWidth();
        int ph = parent.getHeight();
        int vw = v.getWidth();
        int vh = v.getHeight();

        // X 좌표 스냅
        float targetX = (v.getX() + vw / 2f > pw / 2f) ? pw - vw - 16f : 16f;

        // Y 좌표 제한
        float targetY = v.getY();
        if (targetY < 16f) targetY = 16f;
        if (targetY > ph - vh - 16f)
            targetY = ph - vh - 16f;

        // 애니메이션 이동
        v.animate().x(targetX).y(targetY).setDuration(180).start();
    }
}

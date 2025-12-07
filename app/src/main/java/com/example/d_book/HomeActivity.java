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
    // 諛곕꼫 ?먮룞 濡ㅻ쭅 愿??    // ============================
    // 硫붿씤 ?ㅻ젅?쒖뿉??諛곕꼫 ?먮룞 濡ㅻ쭅???꾪븳 Handler
    private final android.os.Handler bannerHandler = new android.os.Handler(android.os.Looper.getMainLooper());

    // ViewPager2 李몄“ 蹂??    private ViewPager2 bannerPagerRef;

    // ?멸린 梨?由ъ뒪??RecyclerView Adapter
    private TrendingBookAdapter trendingAdapter;

    // ?멸린 梨??곗씠?????由ъ뒪??    private final List<TrendingBook> trendingBooks = new ArrayList<>();

    // 諛곕꼫 ?먮룞 ?ㅽ겕濡?Runnable
    private final Runnable bannerRunnable = new Runnable() {
        @Override
        public void run() {
            if (bannerPagerRef != null && bannerPagerRef.getAdapter() != null) {
                // ?꾩옱 諛곕꼫???ㅼ쓬 ?몃뜳?ㅻ줈 ?대룞
                int next = (bannerPagerRef.getCurrentItem() + 1) % bannerPagerRef.getAdapter().getItemCount();
                bannerPagerRef.setCurrentItem(next, true);
                // 5珥????ㅼ떆 ?ㅽ뻾
                bannerHandler.postDelayed(this, 5000);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // ============================
        // ?곷떒 ?대컮 ?ㅼ젙
        // ============================
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // ============================
        // 寃??湲곕뒫
        // ============================
        TextInputEditText editSearch = findViewById(R.id.editSearch);
        MaterialButton buttonSearch = findViewById(R.id.buttonSearch);

        // ?ㅻ낫??寃??踰꾪듉 ?대┃ ??        editSearch.setOnEditorActionListener((v, a, e) -> {
            openSearchWithQuery(v.getText());
            return true;
        });

        // 寃??踰꾪듉 ?대┃ ??        if (buttonSearch != null)
            buttonSearch.setOnClickListener(v -> openSearchWithQuery(editSearch.getText()));

        // ============================
        // 諛곕꼫 諛??멸린 梨?珥덇린??        // ============================
        setupBanner();
        setupTrendingBooks();

        // ============================
        // ?ㅼ떆媛?異붿쿇 梨?RecyclerView
        // ============================
        RecyclerView recommendationsRecycler = findViewById(R.id.recyclerRecommendations);
        recommendationsRecycler.setLayoutManager(new LinearLayoutManager(this));
        loadRealtimeRecommendations(recommendationsRecycler);

        // ============================
        // 利먭꺼李얘린 踰꾪듉 ?대┃
        // ============================
        findViewById(R.id.cardFavorites).setOnClickListener(v -> {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                Toast.makeText(this, "濡쒓렇?몄씠 ?꾩슂?⑸땲??, Toast.LENGTH_SHORT).show();
            } else {
                startActivity(new Intent(this, FavoritesActivity.class));
            }
        });

        // 理쒓렐 ?낅줈??梨?踰꾪듉 ?대┃
        findViewById(R.id.cardRecent).setOnClickListener(v ->
                startActivity(new Intent(this, UploadBooksActivity.class))
        );

        // ============================
        // ?섎떒 ?ㅻ퉬寃뚯씠???ㅼ젙
        // ============================
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_home);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) return true; // ?꾩옱 ?덉씠硫??꾨Т ?숈옉 ?놁쓬
            if (id == R.id.nav_settings)
                startActivity(new Intent(this, SettingsActivity.class));
            if (id == R.id.nav_books)
                startActivity(new Intent(this, SearchActivity.class));
            return false;
        });

}

    // =====================================================
    // 諛곕꼫 愿??硫붿꽌??    // =====================================================
    private void setupBanner() {
        ViewPager2 bannerPager = findViewById(R.id.bannerPager);
        this.bannerPagerRef = bannerPager;

        LinearLayout bannerDots = findViewById(R.id.bannerDots);

        // 諛곕꼫 ?곗씠???앹꽦
        List<Banner> bannerData = new ArrayList<>();
        bannerData.add(new Banner(R.drawable.banner_winter, "?곕쑜????沅뚯쑝濡?梨꾩슦??寃⑥슱 諛?));
        bannerData.add(new Banner(R.drawable.banner_magic, "留덈쾿 媛숈? 紐⑦뿕???쒖옉?쇱슂"));
        bannerData.add(new Banner(R.drawable.banner_letter, "?몄?泥섎읆 留덉쓬??嫄대꽕???댁빞湲?));

        // Adapter ?ㅼ젙
        bannerPager.setAdapter(new BannerAdapter(bannerData));

        // 諛곕꼫 ??dot) ?쒖떆
        setupDots(bannerDots, bannerData.size());

        // 諛곕꼫 ?ㅽ겕濡??????낅뜲?댄듃
        bannerPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateDots(bannerDots, position);
            }
        });

        // 珥덇린 ???곹깭 ?낅뜲?댄듃
        updateDots(bannerDots, 0);

        // 諛곕꼫 ?먮룞 ?ㅽ겕濡??쒖옉
        bannerHandler.postDelayed(bannerRunnable, 5000);
    }

    // =====================================================
    // ?멸린 梨?愿??硫붿꽌??    // =====================================================
    private void setupTrendingBooks() {
        RecyclerView trendingRecycler = findViewById(R.id.recyclerTrending);
        trendingRecycler.setLayoutManager(new LinearLayoutManager(this));

        // Adapter ?앹꽦 諛??대┃ ?대깽???뺤쓽
        trendingAdapter = new TrendingBookAdapter(this, trendingBooks,
                item -> {
                    incrementVisitCount(item); // ?대┃ ??諛⑸Ц ??利앷?
                    openSearchWithQuery(item.getTitle()); // 寃???붾㈃?쇰줈 ?대룞
                });

        trendingRecycler.setAdapter(trendingAdapter);

        // Firestore?먯꽌 ?멸린 梨?濡쒕뱶
        loadTrendingBooksFromFirestore();
    }

    private void loadTrendingBooksFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("books")
                .orderBy("visitCount", Query.Direction.DESCENDING) // 諛⑸Ц??湲곗? ?대┝李⑥닚
                .limit(10) // ?곸쐞 10沅?                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;

                    trendingBooks.clear();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        String title = doc.getString("title");
                        String author = doc.getString("author");
                        String rawThumb = doc.getString("thumbnail");
                        String displayThumb = ThumbnailHelper.display(rawThumb, title, author);
                        String storageThumb = ThumbnailHelper.storage(rawThumb, title, author);
                        if (ThumbnailHelper.isNullOrEmpty(rawThumb) && !ThumbnailHelper.isNullOrEmpty(storageThumb)) {
                            doc.getReference().update("thumbnail", storageThumb);
                        }
                        TrendingBook book = new TrendingBook(
                                title,
                                author,
                                doc.getLong("visitCount") != null ? doc.getLong("visitCount").intValue() : 0,
                                displayThumb
                        );
                        trendingBooks.add(book);
                    }
                    trendingAdapter.notifyDataSetChanged();
                });
    }

    // ?멸린 梨??대┃ ??諛⑸Ц ??利앷?
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
    // ?ㅼ떆媛??꾧린 湲곕컲 異붿쿇 (?곸쐞 3沅?
    // =====================================================
    private static class BookScore {
        String title;
        double score; // 異붿쿇 ?먯닔
        String category;
        String description;
        List<String> keywords = new ArrayList<>();
        int visitCount = 0;

        BookScore(String t) { this.title = t; }
    }

    private void loadRealtimeRecommendations(RecyclerView recyclerView) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 梨?而щ젆?섏쓣 ?ㅼ떆媛?媛먯떆
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

                // 媛?梨낆쓽 由щ럭 ?ㅼ떆媛?諛섏쁺
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

                                // 異붿쿇 ?먯닔 怨꾩궛: 醫뗭븘??2 - ?レ뼱??1
                                likeScore += (likeCnt * 2.0) - (dislikeCnt * 1.0);

                                String parent = r.getString("parentReviewId");
                                if (parent != null && !parent.isEmpty()) replyCount++; // ?듦? ??異붽?
                            }

                            // 理쒖쥌 異붿쿇 ?먯닔
                            bs.score = likeScore + (replyCount * 1.5) + Math.log10(bs.visitCount + 10);

                            // TF-IDF ?ㅼ썙??媛以묒튂
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
                                bs.score += idf * 1.3; // TF-IDF 媛以묒튂
                            }

                            // 異붿쿇 UI 媛깆떊
                            updateRecommendationUISafe(scoreList, recyclerView, db);
                        });
            }
        });
    }

    private void updateRecommendationUISafe(List<BookScore> scoreList, RecyclerView recyclerView, FirebaseFirestore db) {
        // 異붿쿇 ?먯닔 湲곗? ?대┝李⑥닚 ?뺣젹
        scoreList.sort((a, b) -> Double.compare(b.score, a.score));

        // ?곸쐞 3沅뚮쭔 媛?몄삤湲?        List<BookScore> topList = new ArrayList<>(scoreList.size() > 3 ? scoreList.subList(0, 3) : scoreList);

        List<SearchResultItem> results = new ArrayList<>();
        final int[] loadedCount = {0};

        // Firestore?먯꽌 ?곸꽭 ?뺣낫 媛?몄삤湲?        for (BookScore s : topList) {
            db.collection("books")
                    .whereEqualTo("title", s.title)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (!snapshot.isEmpty()) {
                            DocumentSnapshot doc = snapshot.getDocuments().get(0);
                            String title = doc.getString("title");
                            String rawThumb = doc.getString("thumbnail");
                            String author = doc.getString("author");
                            results.add(new SearchResultItem(
                                    title,
                                    author,
                                    ThumbnailHelper.display(rawThumb, title, author),
                                    doc.getString("category")
                            ));
                        }
                        loadedCount[0]++;
                        if (loadedCount[0] == topList.size()) {
                            // Adapter ?ㅼ젙
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
    // 怨듯넻 湲곕뒫
    // =====================================================
    private void openSearchWithQuery(CharSequence searchText) {
        Intent i = new Intent(this, SearchActivity.class);
        if (!TextUtils.isEmpty(searchText))
            i.putExtra("query", searchText.toString());
        startActivity(i);
    }

    // 諛곕꼫 ??dot) ?ㅼ젙
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
        bannerHandler.removeCallbacks(bannerRunnable); // 硫붾え由??꾩닔 諛⑹?
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

    }

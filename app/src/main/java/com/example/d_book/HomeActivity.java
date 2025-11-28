package com.example.d_book;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.text.TextUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.card.MaterialCardView;
import android.widget.LinearLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.auth.FirebaseAuth;
import com.example.d_book.adapter.TrendingBookAdapter;
import com.example.d_book.item.TrendingBook;
import com.example.d_book.adapter.SearchResultAdapter;
import com.example.d_book.item.SearchResultItem;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private final android.os.Handler bannerHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private ViewPager2 bannerPagerRef;
    private final Runnable bannerRunnable = new Runnable() {
        @Override public void run() {
            androidx.viewpager2.widget.ViewPager2 pager = findViewById(R.id.bannerPager);
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
        TextInputEditText editSearch = findViewById(R.id.editSearch);
        MaterialButton buttonSearch = findViewById(R.id.buttonSearch);
        editSearch.setOnEditorActionListener((v, actionId, event) -> {
            openSearchWithQuery(v.getText());
            return true;
        });
        if (buttonSearch != null) {
            buttonSearch.setOnClickListener(v -> openSearchWithQuery(editSearch.getText()));
        }

        // Banner setup
        androidx.viewpager2.widget.ViewPager2 bannerPager = findViewById(R.id.bannerPager);
        this.bannerPagerRef = bannerPager;
        LinearLayout bannerDots = findViewById(R.id.bannerDots);
        java.util.List<Banner> bannerData = new java.util.ArrayList<>();
        bannerData.add(new Banner(R.drawable.banner_winter, "따뜻한 한 권으로 채우는 겨울 밤"));
        bannerData.add(new Banner(R.drawable.banner_magic, "마법 같은 모험이 시작돼요"));
        bannerData.add(new Banner(R.drawable.banner_letter, "편지처럼 마음을 건네는 이야기"));
        bannerPager.setAdapter(new BannerAdapter(bannerData));
        // setup small custom dots manually for full size control
        setupDots(bannerDots, bannerData.size());
        bannerPager.registerOnPageChangeCallback(new androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateDots(bannerDots, position);
            }
        });
        // 초기 선택 상태 반영
        updateDots(bannerDots, 0);
        bannerHandler.postDelayed(bannerRunnable, 5000);

        RecyclerView trendingRecycler = findViewById(R.id.recyclerTrending);
        trendingRecycler.setLayoutManager(new LinearLayoutManager(this));
        TrendingBookAdapter trendingAdapter = new TrendingBookAdapter(this, createTrendingBooks(), item -> openSearchWithQuery(item.getTitle()));
        trendingRecycler.setAdapter(trendingAdapter);
        RecyclerView recommendationsRecycler = findViewById(R.id.recyclerRecommendations);
        recommendationsRecycler.setLayoutManager(new LinearLayoutManager(this));
        SearchResultAdapter recommendationsAdapter = new SearchResultAdapter(this, createRecommendationsFromReviews(), item -> openSearchWithQuery(item.getTitle()));
        recommendationsRecycler.setAdapter(recommendationsAdapter);

        View cardFavorites = findViewById(R.id.cardFavorites);
        View cardRecent = findViewById(R.id.cardRecent);
        cardFavorites.setOnClickListener(v -> {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                startActivity(new Intent(this, LoginActivity.class));
            } else {
                Toast.makeText(this, getString(R.string.label_favorites), Toast.LENGTH_SHORT).show();
            }
        });
        cardRecent.setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));

        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_home);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            } else if (id == R.id.nav_books) {
                startActivity(new Intent(this, SearchActivity.class));
                return true;
            }
            return false;
        });

        setupQuickRead();
    }

    private void openSearchWithQuery(CharSequence searchText) {
        Intent intent = new Intent(this, SearchActivity.class);
        if (searchText != null && searchText.length() > 0) {
            intent.putExtra("query", searchText.toString());
        }
        startActivity(intent);
    }

    private List<TrendingBook> createTrendingBooks() {
        List<TrendingBook> books = new ArrayList<>();
        books.add(new TrendingBook("해리 포터와 마법사의 돌", "J.K. 롤링", 5820, "https://covers.openlibrary.org/b/isbn/9780439708180-L.jpg"));
        books.add(new TrendingBook("해리 포터와 비밀의 방", "J.K. 롤링", 4210, "https://covers.openlibrary.org/b/isbn/9780439064873-L.jpg"));
        books.add(new TrendingBook("해리 포터와 아즈카반의 죄수", "J.K. 롤링", 3980, "https://covers.openlibrary.org/b/isbn/9780439136365-L.jpg"));
        books.add(new TrendingBook("반지의 제왕: 반지 원정대", "J.R.R. 톨킨", 2760, "https://covers.openlibrary.org/b/isbn/9780547928210-L.jpg"));
        books.add(new TrendingBook("반지의 제왕: 두 개의 탑", "J.R.R. 톨킨", 2450, "https://covers.openlibrary.org/b/isbn/9780547928203-L.jpg"));
        books.add(new TrendingBook("위대한 개츠비", "F. 스콧 피츠제럴드", 2120, "https://covers.openlibrary.org/b/isbn/9780743273565-L.jpg"));
        books.add(new TrendingBook("나미야 잡화점의 기적", "히가시노 게이고", 1980, R.drawable.namiya_cover));
        books.add(new TrendingBook("어린 왕자", "앙투안 드 생텍쥐페리", 1870, "https://covers.openlibrary.org/b/isbn/9780156012195-L.jpg"));
        return books;
    }

    private List<SearchResultItem> createRecommendationsFromReviews() {
        List<String> reviews = getSavedReviews();
        List<SearchResultItem> recs = new ArrayList<>();

        for (String review : reviews) {
            String lower = review.toLowerCase();
            if (lower.contains("마법") || lower.contains("호그와트") || lower.contains("마법사")) {
                addIfNotExists(recs, new SearchResultItem("해리 포터와 비밀의 방", "J.K. 롤링", "https://covers.openlibrary.org/b/isbn/9780439064873-L.jpg"));
                addIfNotExists(recs, new SearchResultItem("해리 포터와 불의 잔", "J.K. 롤링", "https://covers.openlibrary.org/b/isbn/9780439139601-L.jpg"));
            }
            if (lower.contains("따뜻") || lower.contains("위로") || lower.contains("편지") || lower.contains("잡화점")) {
                addIfNotExists(recs, new SearchResultItem("나미야 잡화점의 기적", "히가시노 게이고", R.drawable.namiya_cover));
            }
            if (lower.contains("모험") || lower.contains("판타지") || lower.contains("여정")) {
                addIfNotExists(recs, new SearchResultItem("반지의 제왕: 반지 원정대", "J.R.R. 톨킨", "https://covers.openlibrary.org/b/isbn/9780547928210-L.jpg"));
            }
        }

        if (recs.isEmpty()) {
            recs.add(new SearchResultItem("위대한 개츠비", "F. 스콧 피츠제럴드", "https://covers.openlibrary.org/b/isbn/9780743273565-L.jpg"));
            recs.add(new SearchResultItem("어린 왕자", "앙투안 드 생텍쥐페리", "https://covers.openlibrary.org/b/isbn/9780156012195-L.jpg"));
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
            if (parts.length == 2) {
                list.add(parts[1]);
            }
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
            startActivity(new android.content.Intent(this, NotificationsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

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

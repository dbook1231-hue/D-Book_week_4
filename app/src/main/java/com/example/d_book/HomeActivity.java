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

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.card.MaterialCardView;
import android.widget.LinearLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.auth.FirebaseAuth;

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
        bannerData.add(new Banner(R.drawable.banner_1, "신간 추천"));
        bannerData.add(new Banner(R.drawable.banner_2, "이벤트 할인"));
        bannerData.add(new Banner(R.drawable.banner_3, "베스트셀러"));
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

        RecyclerView recyclerView = findViewById(R.id.recyclerRecommended);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        BookAdapter adapter = new BookAdapter(createDummyBooks());
        recyclerView.setAdapter(adapter);

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

    private List<Book> createDummyBooks() {
        List<Book> books = new ArrayList<>();
        books.add(new Book("클린 코드", "로버트 C. 마틴"));
        books.add(new Book("오브젝트", "조영호"));
        books.add(new Book("모던 자바 인 액션", "라울-게이브리얼 우르마"));
        books.add(new Book("Effective Java", "조슈아 블로크"));
        books.add(new Book("이것이 안드로이드다", "고돈호"));
        return books;
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

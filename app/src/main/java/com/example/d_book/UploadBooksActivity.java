package com.example.d_book;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UploadBooksActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_books);

        db = FirebaseFirestore.getInstance();

        Button btnUploadBooks = findViewById(R.id.btnUploadBooks);
        btnUploadBooks.setOnClickListener(v -> uploadAllBooks());
    }

    // 🔹 전체 도서 업로드
    private void uploadAllBooks() {

        List<Map<String, Object>> books = new ArrayList<>();

        // 제목, 저자, 이미지, 카테고리 배열 (예: 100권)
        String[][] bookData = {
                {"해리 포터와 마법사의 돌", "J.K. 롤링", "https://covers.openlibrary.org/b/isbn/9780439708180-L.jpg", "소설"},
                {"해리 포터와 비밀의 방", "J.K. 롤링", "https://covers.openlibrary.org/b/isbn/9780439064873-L.jpg", "소설"},
                {"해리 포터와 아즈카반의 죄수", "J.K. 롤링", "https://covers.openlibrary.org/b/isbn/9780439136365-L.jpg", "소설"},
                {"해리 포터와 불의 잔", "J.K. 롤링", "https://covers.openlibrary.org/b/isbn/9780439139601-L.jpg", "소설"},
                {"해리 포터와 불사조 기사단", "J.K. 롤링", "https://covers.openlibrary.org/b/isbn/9780439358071-L.jpg", "소설"},
                {"해리 포터와 혼혈 왕자", "J.K. 롤링", "https://covers.openlibrary.org/b/isbn/9780439785969-L.jpg", "소설"},
                {"해리 포터와 죽음의 성물", "J.K. 롤링", "https://covers.openlibrary.org/b/isbn/9780545010221-L.jpg", "소설"},
                {"반지의 제왕: 반지 원정대", "J.R.R. 톨킨", "https://covers.openlibrary.org/b/isbn/9780547928210-L.jpg", "소설"},
                {"반지의 제왕: 두 개의 탑", "J.R.R. 톨킨", "https://covers.openlibrary.org/b/isbn/9780547928203-L.jpg", "소설"},
                {"반지의 제왕: 왕의 귀환", "J.R.R. 톨킨", "https://covers.openlibrary.org/b/isbn/9780547928197-L.jpg", "소설"},
                {"나미야 잡화점의 기적", "히가시노 게이고", "https://example.com/namiya_cover.jpg", "소설"},
                {"어린 왕자", "앙투안 드 생텍쥐페리", "https://covers.openlibrary.org/b/isbn/9780156012195-L.jpg", "에세이"},
                {"데미안", "헤르만 헤세", "https://covers.openlibrary.org/b/isbn/9780143106784-L.jpg", "인문학"},
                {"노인과 바다", "어니스트 헤밍웨이", "https://covers.openlibrary.org/b/isbn/9780684801223-L.jpg", "소설"},
                {"위대한 개츠비", "F. 스콧 피츠제럴드", "https://covers.openlibrary.org/b/isbn/9780743273565-L.jpg", "소설"},
                // 여기에 나머지 85권 이상 책 데이터를 계속 추가
        };

        // 반복문으로 books에 추가
        for (String[] data : bookData) {
            books.add(createBook(data[0], data[1], data[2], data[3]));
        }

        // Firestore 업로드
        for (Map<String, Object> book : books) {
            db.collection("books")
                    .add(book)
                    .addOnSuccessListener(doc -> Log.d("Firestore", "추가 성공: " + doc.getId()))
                    .addOnFailureListener(e -> Log.e("Firestore", "추가 실패", e));
        }

        Toast.makeText(this, "전체 도서 업로드 완료!", Toast.LENGTH_SHORT).show();
    }

    // 🔹 도서 생성 함수 (visitCount 포함)
    private Map<String, Object> createBook(String title, String author, String thumbnail, String category) {
        Map<String, Object> book = new HashMap<>();
        book.put("title", title);
        book.put("author", author);
        book.put("thumbnail", thumbnail);
        book.put("category", category);
        book.put("visitCount", 0); // 방문 수 초기화
        return book;
    }
}

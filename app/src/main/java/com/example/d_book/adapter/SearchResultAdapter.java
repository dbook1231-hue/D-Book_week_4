package com.example.d_book.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.d_book.R;  // 반드시 추가


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.d_book.item.SearchResultItem;

import java.util.List;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.SearchViewHolder> {

    private Context context;
    private List<SearchResultItem> searchResults;
    private OnItemClickListener listener;

    // 클릭 이벤트 인터페이스
    public interface OnItemClickListener {
        void onItemClick(SearchResultItem item);
    }

    public SearchResultAdapter(Context context, List<SearchResultItem> searchResults, OnItemClickListener listener) {
        this.context = context;
        this.searchResults = searchResults;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_search_result, parent, false);
        return new SearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        SearchResultItem item = searchResults.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return searchResults.size();
    }

    static class SearchViewHolder extends RecyclerView.ViewHolder {

        ImageView imageThumbnail;
        TextView textTitle, textAuthor;

        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            imageThumbnail = itemView.findViewById(R.id.imageThumbnail);
            textTitle = itemView.findViewById(R.id.textTitle);
            textAuthor = itemView.findViewById(R.id.textAuthor);
        }

        public void bind(final SearchResultItem item, final OnItemClickListener listener) {
            textTitle.setText(item.getTitle());
            textAuthor.setText(item.getAuthor());

            // Glide로 이미지 로드
            if (item.getThumbnailUrl() != null && !item.getThumbnailUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(item.getThumbnailUrl())
                        .placeholder(R.drawable.ic_book_placeholder)
                        .into(imageThumbnail);
            } else {
                imageThumbnail.setImageResource(R.drawable.ic_book_placeholder);
            }

            // 클릭 이벤트
            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }
}

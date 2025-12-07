package com.example.d_book.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.d_book.R;
import com.example.d_book.ThumbnailHelper;
import com.example.d_book.item.FavoriteBookItem;

import java.util.List;

public class FavoriteBookAdapter extends RecyclerView.Adapter<FavoriteBookAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onClick(FavoriteBookItem item);
    }

    private final List<FavoriteBookItem> items;
    private final OnItemClickListener listener;

    public FavoriteBookAdapter(List<FavoriteBookItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FavoriteBookItem item = items.get(position);

        // 텍스트 바인딩
        holder.textTitle.setText(item.getTitle() != null ? item.getTitle() : "제목 없음");
        holder.textAuthor.setText(item.getAuthor() != null ? item.getAuthor() : "작가 정보 없음");
        holder.textCategory.setText(item.getCategory() != null ? item.getCategory() : "카테고리 없음");

        // 썸네일 바인딩 (데미안 로컬 폴백 지원)
        int fallbackRes = ThumbnailHelper.fallbackRes(item.getTitle(), item.getAuthor());
        // 데미안은 무조건 로컬 표지
        if (fallbackRes != 0) {
            holder.imageThumbnail.setImageResource(fallbackRes);
        } else if (item.getThumbnail() != null && !item.getThumbnail().isEmpty()) {
            Glide.with(holder.imageThumbnail.getContext())
                    .load(item.getThumbnail())
                    .placeholder(R.drawable.ic_book_placeholder)
                    .error(R.drawable.ic_book_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.imageThumbnail);
        } else {
            holder.imageThumbnail.setImageResource(R.drawable.ic_book_placeholder);
        }

        // 클릭 이벤트
        holder.itemView.setOnClickListener(v -> listener.onClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView imageThumbnail;
        final TextView textTitle;
        final TextView textAuthor;
        final TextView textCategory;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageThumbnail = itemView.findViewById(R.id.imageThumbnail);
            textTitle = itemView.findViewById(R.id.textTitle);
            textAuthor = itemView.findViewById(R.id.textAuthor);
            textCategory = itemView.findViewById(R.id.textCategory);
        }
    }
}

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
import com.example.d_book.item.FavoriteBookItem;
import com.example.d_book.R;

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

        // 텍스트 세팅
        holder.textTitle.setText(item.getTitle() != null ? item.getTitle() : "제목 없음");
        holder.textAuthor.setText(item.getAuthor() != null ? item.getAuthor() : "저자 없음");
        holder.textCategory.setText(item.getCategory() != null ? item.getCategory() : "카테고리 없음");

        // 썸네일 세팅
        if (item.getThumbnail() != null && !item.getThumbnail().isEmpty()) {
            Glide.with(holder.imageThumbnail.getContext())
                    .load(item.getThumbnail())
                    .placeholder(R.drawable.ic_book_placeholder)
                    .error(R.drawable.ic_book_placeholder) // 로딩 실패 시 기본 이미지
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // 캐시 전략
                    .into(holder.imageThumbnail);
        } else {
            holder.imageThumbnail.setImageResource(R.drawable.ic_book_placeholder);
        }

        // 아이템 클릭
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

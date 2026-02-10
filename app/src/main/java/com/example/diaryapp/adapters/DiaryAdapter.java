package com.example.diaryapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.diaryapp.R;
import com.example.diaryapp.activities.DiaryEditActivity;
import com.example.diaryapp.models.Diary;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class DiaryAdapter extends RecyclerView.Adapter<DiaryAdapter.DiaryViewHolder> {

    private List<Diary> diaryList;
    private Context context;
    private OnDiaryDeleteListener deleteListener;

    public interface OnDiaryDeleteListener {
        void onDelete(Diary diary);
    }

    public DiaryAdapter(List<Diary> diaryList, Context context, OnDiaryDeleteListener deleteListener) {
        this.diaryList = diaryList;
        this.context = context;
        this.deleteListener = deleteListener;
    }

    @Override
    public DiaryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_diary, parent, false);
        return new DiaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DiaryViewHolder holder, int position) {
        Diary diary = diaryList.get(position);
        holder.titleTextView.setText(diary.getTitle());
        holder.contentTextView.setText(diary.getContent());
        holder.dateTextView.setText(formatDate(diary.getUpdatedAt()));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DiaryEditActivity.class);
            intent.putExtra("diary_id", diary.getId());
            context.startActivity(intent);
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(diary);
            }
        });
    }

    @Override
    public int getItemCount() {
        return diaryList.size();
    }

    public void updateData(List<Diary> newDiaryList) {
        this.diaryList = newDiaryList;
        notifyDataSetChanged();
    }

    private String formatDate(java.util.Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(date);
    }

    static class DiaryViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView contentTextView;
        TextView dateTextView;
        TextView deleteButton;

        DiaryViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.diary_title);
            contentTextView = itemView.findViewById(R.id.diary_content);
            dateTextView = itemView.findViewById(R.id.diary_date);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}

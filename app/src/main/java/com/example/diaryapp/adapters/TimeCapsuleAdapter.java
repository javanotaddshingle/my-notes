package com.example.diaryapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.diaryapp.R;
import com.example.diaryapp.activities.TimeCapsuleDetailActivity;
import com.example.diaryapp.models.TimeCapsule;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TimeCapsuleAdapter extends RecyclerView.Adapter<TimeCapsuleAdapter.TimeCapsuleViewHolder> {

    private List<TimeCapsule> timeCapsuleList;
    private Context context;
    private OnTimeCapsuleDeleteListener deleteListener;

    public interface OnTimeCapsuleDeleteListener {
        void onDelete(TimeCapsule timeCapsule);
    }

    public TimeCapsuleAdapter(List<TimeCapsule> timeCapsuleList, Context context, OnTimeCapsuleDeleteListener deleteListener) {
        this.timeCapsuleList = timeCapsuleList;
        this.context = context;
        this.deleteListener = deleteListener;
    }

    @Override
    public TimeCapsuleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_time_capsule, parent, false);
        return new TimeCapsuleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TimeCapsuleViewHolder holder, int position) {
        TimeCapsule timeCapsule = timeCapsuleList.get(position);
        holder.titleTextView.setText(timeCapsule.getTitle());
        holder.openTimeTextView.setText(formatDate(timeCapsule.getOpenAt()));

        Date now = new Date();
        if (timeCapsule.isOpened()) {
            holder.statusTextView.setText("已解锁");
            holder.statusTextView.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        } else if (now.after(timeCapsule.getOpenAt())) {
            holder.statusTextView.setText("可以解锁");
            holder.statusTextView.setTextColor(context.getResources().getColor(android.R.color.holo_blue_dark));
        } else {
            holder.statusTextView.setText("未到解锁时间");
            holder.statusTextView.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TimeCapsuleDetailActivity.class);
            intent.putExtra("time_capsule_id", timeCapsule.getId());
            context.startActivity(intent);
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(timeCapsule);
            }
        });
    }

    @Override
    public int getItemCount() {
        return timeCapsuleList.size();
    }

    public void updateData(List<TimeCapsule> newTimeCapsuleList) {
        this.timeCapsuleList = newTimeCapsuleList;
        notifyDataSetChanged();
    }

    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(date);
    }

    static class TimeCapsuleViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView openTimeTextView;
        TextView statusTextView;
        TextView deleteButton;

        TimeCapsuleViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.capsule_title);
            openTimeTextView = itemView.findViewById(R.id.open_time);
            statusTextView = itemView.findViewById(R.id.capsule_status);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}

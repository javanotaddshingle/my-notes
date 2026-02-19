package com.example.diaryapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.diaryapp.R;
import com.example.diaryapp.activities.CheckInItemEditActivity;
import com.example.diaryapp.database.AppDatabase;
import com.example.diaryapp.models.CheckInItem;
import com.example.diaryapp.models.CheckInRecord;

import java.util.Date;
import java.util.List;

public class CheckInAdapter extends RecyclerView.Adapter<CheckInAdapter.CheckInViewHolder> {

    private List<CheckInItem> checkInItemList;
    private Context context;
    private OnCheckInDeleteListener deleteListener;
    private AppDatabase database;

    public interface OnCheckInDeleteListener {
        void onDelete(CheckInItem checkInItem);
    }

    public CheckInAdapter(List<CheckInItem> checkInItemList, Context context, OnCheckInDeleteListener deleteListener) {
        this.checkInItemList = checkInItemList;
        this.context = context;
        this.deleteListener = deleteListener;
        this.database = AppDatabase.getInstance(context);
    }

    @Override
    public CheckInViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_check_in, parent, false);
        return new CheckInViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CheckInViewHolder holder, int position) {
        CheckInItem checkInItem = checkInItemList.get(position);
        holder.nameTextView.setText(checkInItem.getName());

        holder.itemView.setAlpha(0f);
        holder.itemView.animate()
                .alpha(1f)
                .setDuration(200)
                .setStartDelay(position * 50)
                .start();

        new Thread(() -> {
            Date today = new Date();
            long todayMillis = today.getTime() / (24 * 60 * 60 * 1000);
            List<CheckInRecord> records = database.checkInRecordDao().getCheckInRecordsByItemId(checkInItem.getId());
            boolean isCheckedToday = false;
            for (CheckInRecord record : records) {
                long recordMillis = record.getCheckInDate().getTime() / (24 * 60 * 60 * 1000);
                if (recordMillis == todayMillis) {
                    isCheckedToday = true;
                    break;
                }
            }
            final boolean finalIsCheckedToday = isCheckedToday;
            holder.checkBox.post(() -> {
                holder.checkBox.setOnCheckedChangeListener(null);
                holder.checkBox.setChecked(finalIsCheckedToday);
                holder.checkBox.setEnabled(!finalIsCheckedToday);
                holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        completeCheckIn(checkInItem, holder);
                        holder.checkBox.setEnabled(false);
                    }
                });
            });
        }).start();

        holder.editButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, CheckInItemEditActivity.class);
            intent.putExtra("check_in_item_id", checkInItem.getId());
            context.startActivity(intent);
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(checkInItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return checkInItemList.size();
    }

    public void updateData(List<CheckInItem> newCheckInItemList) {
        this.checkInItemList = newCheckInItemList;
        notifyDataSetChanged();
    }

    private void completeCheckIn(CheckInItem checkInItem, CheckInViewHolder holder) {
        new Thread(() -> {
            Date now = new Date();
            CheckInRecord record = new CheckInRecord(checkInItem.getId(), now, now);
            database.checkInRecordDao().insert(record);
            
            updateUserExperience(checkInItem.getExperiencePoints());
            
            holder.itemView.post(() -> {
                performCheckInAnimation(holder.itemView);
                showCheckInSuccessToast(checkInItem.getExperiencePoints());
            });
        }).start();
    }
    
    private void performCheckInAnimation(View view) {
        android.view.animation.ScaleAnimation scaleAnimation = new android.view.animation.ScaleAnimation(
            1.0f, 1.1f, 1.0f, 1.1f, 
            android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f, 
            android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnimation.setDuration(200);
        scaleAnimation.setRepeatMode(android.view.animation.Animation.REVERSE);
        scaleAnimation.setRepeatCount(1);
        
        android.view.animation.AlphaAnimation alphaAnimation = new android.view.animation.AlphaAnimation(1.0f, 0.7f);
        alphaAnimation.setDuration(200);
        alphaAnimation.setRepeatMode(android.view.animation.Animation.REVERSE);
        alphaAnimation.setRepeatCount(1);
        
        android.view.animation.AnimationSet animationSet = new android.view.animation.AnimationSet(true);
        animationSet.addAnimation(scaleAnimation);
        animationSet.addAnimation(alphaAnimation);
        
        view.startAnimation(animationSet);
    }
    
    private void showCheckInSuccessToast(int experiencePoints) {
        android.widget.Toast.makeText(context, "打卡成功！获得 " + experiencePoints + " 经验值", android.widget.Toast.LENGTH_SHORT).show();
    }

    private void updateUserExperience(int points) {
        com.example.diaryapp.managers.UserManager.getInstance(context).addExperiencePoints(points);
    }

    static class CheckInViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        CheckBox checkBox;
        TextView editButton;
        TextView deleteButton;

        CheckInViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.check_in_name);
            checkBox = itemView.findViewById(R.id.check_in_checkbox);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}

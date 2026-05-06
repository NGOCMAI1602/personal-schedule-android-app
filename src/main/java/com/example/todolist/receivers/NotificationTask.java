package com.example.todolist.receivers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.example.todolist.R;
import com.example.todolist.activities.DetailTaskActivity;

public class NotificationTask extends BroadcastReceiver {

    private static final String CHANNEL_ID = "todo_list_channel";
    private static final String CHANNEL_NAME = "Task Reminders";

    @Override
    public void onReceive(Context context, Intent intent) {
        int taskId = intent.getIntExtra("TASK_ID", -1);
        String taskTitle = intent.getStringExtra("TASK_TITLE");

        if (taskId != -1 && taskTitle != null) {
            showNotification(context, taskId, taskTitle);
        }
    }

    private void showNotification(Context context, int taskId, String title) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // 1. Tạo Notification Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Task Reminder Notification");
            notificationManager.createNotificationChannel(channel);
        }

        // 2. Intent mở chi tiết Task khi click
        Intent detailIntent = new Intent(context, DetailTaskActivity.class);
        detailIntent.putExtra("TASK_ID", taskId);
        // Flags giúp mở Activity mới và xóa stack cũ nếu cần, tạo trải nghiệm mượt mà
        detailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingDetailIntent = PendingIntent.getActivity(
                context,
                taskId,
                detailIntent,
                flags
        );

        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.alarm_load_icon);
        if (largeIcon == null) {
            largeIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        }

        // 4. Xây dựng thông báo
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_small) // Icon nhỏ
                .setLargeIcon(largeIcon) // Icon lớn
                .setContentTitle("Reminder Task")
                .setContentText(title)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true) // Tự đóng khi click
                .setContentIntent(pendingDetailIntent) // Mở detail khi click
        // --- THÊM DÒNG NÀY ĐỂ CÓ TIẾNG VÀ RUNG ---
        .setDefaults(NotificationCompat.DEFAULT_ALL);
        // ------------------------------------------

        // 5. Hiển thị
        if (notificationManager != null) {
            notificationManager.notify(taskId, builder.build());
        }
    }
}
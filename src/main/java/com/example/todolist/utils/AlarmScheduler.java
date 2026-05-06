package com.example.todolist.utils;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import com.example.todolist.models.Task;
import com.example.todolist.receivers.NotificationTask;

public class AlarmScheduler {

    @SuppressLint("ScheduleExactAlarm")
    public static void scheduleTaskNotification(Context context, Task task) {
        if (task.getStartTime() == null) return;

        long triggerAtMillis = task.getStartTime();

        // Chỉ đặt lịch nếu thời gian kích hoạt nằm trong tương lai
        if (triggerAtMillis > System.currentTimeMillis()) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Intent intent = new Intent(context, NotificationTask.class);
            intent.putExtra("TASK_ID", task.getId());
            intent.putExtra("TASK_TITLE", task.getTitle());

            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    task.getId(),
                    intent,
                    flags
            );

            if (alarmManager != null) {
                // Sử dụng setExactAndAllowWhileIdle để đảm bảo báo thức chạy ngay cả khi máy ở chế độ Doze
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
                }
                Log.d("AlarmScheduler", "Scheduled notification for Task ID: " + task.getId() + " at " + triggerAtMillis);
            }
        }
    }

    public static void cancelTaskNotification(Context context, Task task) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationTask.class);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                task.getId(),
                intent,
                flags
        );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            Log.d("AlarmScheduler", "Canceled notification for Task ID: " + task.getId());
        }
    }
}
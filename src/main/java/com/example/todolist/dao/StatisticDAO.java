package com.example.todolist.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.todolist.database.DBHelper;

import java.util.Calendar;

public class StatisticDAO {
    private SQLiteDatabase db;
    private final DBHelper dbHelper;

    public StatisticDAO(Context context) {
        dbHelper = new DBHelper(context);
    }

    private void open() {
        db = dbHelper.getReadableDatabase();
    }

    private void close() {
        dbHelper.close();
    }

    // Lấy tổng Task trong hệ thống
    public int countAllTasks() {
        open();
        int count = 0;
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DBHelper.getTableTasks(), null);
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        close();
        return count;
    }

    // Lấy tổng Task đã hoàn thành
    public int countCompletedTasks() {
        open();
        int count = 0;
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DBHelper.getTableTasks() + " WHERE is_completed = 1", null);
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        close();
        return count;
    }

    // Lấy tổng Task chưa hoàn thành
    public int countUpcomingTasks() {
        open();
        long now = System.currentTimeMillis();
        int count = 0;
        String selection = "is_completed = 0 AND start_time > ?";
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DBHelper.getTableTasks() + " WHERE " + selection, new String[]{String.valueOf(now)});
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        close();
        return count;
    }

    // Lấy tổng Task đang làm
    public int countInProgressTasks() {
        open();
        long now = System.currentTimeMillis();
        int count = 0;
        // Task đang làm là task chưa hoàn thành và nằm trong khoảng thời gian Start/End
        String selection = "is_completed = 0 AND start_time <= ? AND end_time >= ?";
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DBHelper.getTableTasks() + " WHERE " + selection, new String[]{String.valueOf(now), String.valueOf(now)});
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        close();
        return count;
    }

    // Lấy tổng Task theo Độ Ưu Tiên
    public int countCompletedTasksByPriority(int priority) {
        open();
        int count = 0;
        String selection = "is_completed = 1 AND priority = ?";
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DBHelper.getTableTasks() + " WHERE " + selection, new String[]{String.valueOf(priority)});
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        close();
        return count;
    }

    // Tỉ lệ tổng Task đã hoàn thành trong 1 Tuần
    public int getWeeklyCompletionRate() {
        open();
        Calendar calendar = Calendar.getInstance();

        // Thiết lập về Thứ Hai 00:00:00
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int offset = (dayOfWeek == Calendar.SUNDAY) ? 6 : (dayOfWeek - Calendar.MONDAY);
        calendar.add(Calendar.DAY_OF_MONTH, -offset);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long startOfWeek = calendar.getTimeInMillis();

        // Thiết lập về Chủ Nhật 23:59:59
        calendar.add(Calendar.DAY_OF_MONTH, 6);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        long endOfWeek = calendar.getTimeInMillis();

        // Đếm TỔNG Task trong tuần
        String totalQuery = "SELECT COUNT(*) FROM " + DBHelper.getTableTasks() +
                " WHERE (start_time BETWEEN ? AND ?) OR (end_time BETWEEN ? AND ?)";

        int totalTasks = 0;
        Cursor totalCursor = db.rawQuery(totalQuery, new String[]{
                String.valueOf(startOfWeek), String.valueOf(endOfWeek),
                String.valueOf(startOfWeek), String.valueOf(endOfWeek)
        });
        if (totalCursor.moveToFirst()) {
            totalTasks = totalCursor.getInt(0);
        }
        totalCursor.close();

        // Đếm Task ĐÃ HOÀN THÀNH trong tuần
        String completedQuery = "SELECT COUNT(*) FROM " + DBHelper.getTableTasks() +
                " WHERE is_completed = 1 AND ((start_time BETWEEN ? AND ?) OR (end_time BETWEEN ? AND ?))";

        int completedTasks = 0;
        Cursor completedCursor = db.rawQuery(completedQuery, new String[]{
                String.valueOf(startOfWeek), String.valueOf(endOfWeek),
                String.valueOf(startOfWeek), String.valueOf(endOfWeek)
        });
        if (completedCursor.moveToFirst()) {
            completedTasks = completedCursor.getInt(0);
        }
        completedCursor.close();

        close();

        if (totalTasks == 0) {
            return 0;
        }

        return (int) ((double) completedTasks / totalTasks * 100);
    }
}
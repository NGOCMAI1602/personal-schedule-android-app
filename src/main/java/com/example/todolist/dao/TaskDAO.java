package com.example.todolist.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.todolist.models.Task;
import com.example.todolist.database.DBHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskDAO {
    private SQLiteDatabase db;
    private final DBHelper dbHelper;

    // Định dạng ngày (Bỏ static để tránh lỗi Warning)
    private final SimpleDateFormat DATE_FORMAT_INPUT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public TaskDAO(Context context) {
        dbHelper = new DBHelper(context);
    }

    public void open() {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    // =========================================================================
    // 1. INSERT TASK
    // =========================================================================
    public long insertTask(Task task) {
        long taskId = -1;

        if (task.isAllDay()) {
            task.setStartTime(convertDate(task.getStartDay(), true));
            task.setEndTime(convertDate(task.getEndDay(), false));
        }

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put("title", task.getTitle());
            values.put("description", task.getDescription());
            values.put("start_time", task.getStartTime());
            values.put("end_time", task.getEndTime());
            values.put("start_day", task.getStartDay());
            values.put("end_day", task.getEndDay());
            values.put("category", task.getCategory());
            values.put("repeat_type", task.getRepeatType());
            values.put("notification_time", task.getNotificationTime());
            values.put("is_completed", task.isCompleted() ? 1 : 0);
            values.put("is_all_day", task.isAllDay() ? 1 : 0);
            values.put("location", task.getLocation());
            values.put("priority", task.getPriority());

            taskId = db.insert(DBHelper.getTableTasks(), null, values);

            if (taskId != -1) {
                ContentValues noteValues = new ContentValues();
                String content = task.getNoteContent();
                if (content == null || content.trim().isEmpty()) {
                    content = "No Notes Yet";
                }
                noteValues.put("task_id", taskId);
                noteValues.put("content", content);
                noteValues.put("created_at", System.currentTimeMillis());
                noteValues.put("updated_at", System.currentTimeMillis());

                db.insert(DBHelper.getTableNotes(), null, noteValues);
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        return taskId;
    }

    // =========================================================================
    // 2. UPDATE TASK
    // =========================================================================
    public void updateTask(Task task) {
        if (task.isAllDay()) {
            task.setStartTime(convertDate(task.getStartDay(), true));
            task.setEndTime(convertDate(task.getEndDay(), false));
        }

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put("title", task.getTitle());
            values.put("description", task.getDescription());
            values.put("start_time", task.getStartTime());
            values.put("end_time", task.getEndTime());
            values.put("start_day", task.getStartDay());
            values.put("end_day", task.getEndDay());
            values.put("category", task.getCategory());
            values.put("is_completed", task.isCompleted() ? 1 : 0);
            values.put("is_all_day", task.isAllDay() ? 1 : 0);
            values.put("location", task.getLocation());
            values.put("priority", task.getPriority());

            db.update(DBHelper.getTableTasks(), values, "id=?", new String[]{String.valueOf(task.getId())});

            ContentValues noteValues = new ContentValues();
            noteValues.put("content", task.getNoteContent());
            noteValues.put("updated_at", System.currentTimeMillis());

            int rows = db.update(DBHelper.getTableNotes(), noteValues, "task_id=?", new String[]{String.valueOf(task.getId())});

            if (rows == 0) {
                noteValues.put("task_id", task.getId());
                noteValues.put("created_at", System.currentTimeMillis());
                db.insert(DBHelper.getTableNotes(), null, noteValues);
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    // =========================================================================
    // 3. DELETE TASK
    // =========================================================================
    public void deleteTask(int id) {
        db.delete(DBHelper.getTableNotes(), "task_id=?", new String[]{String.valueOf(id)});
        db.delete(DBHelper.getTableTasks(), "id=?", new String[]{String.valueOf(id)});
    }

    // =========================================================================
    // 4. GET & SEARCH (Logic chính cho Drill-down và Tìm kiếm)
    // =========================================================================

    // Tìm kiếm theo tiêu đề (Cần thiết cho CompletedTaskRepository)
    public List<Task> searchTasksByTitle(String keyword) {
        List<Task> list = new ArrayList<>();
        String selection = "title LIKE ?";
        String[] args = new String[]{"%" + keyword + "%"};
        String orderBy = "priority DESC, start_time ASC";

        Cursor cursor = db.query(DBHelper.getTableTasks(), null, selection, args, null, null, orderBy);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        list.add(cursorToTask(cursor));
                    } while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        }
        return list;
    }

    // Logic Drill-down (Lọc theo trạng thái từ Thống kê)
    public List<Task> getTasks(String progress) {
        List<Task> list = new ArrayList<>();
        long now = System.currentTimeMillis();

        String selection = "1=1";
        List<String> args = new ArrayList<>();

        if (progress != null) {
            switch (progress) {
                case "Upcoming":
                    selection += " AND start_time > ? AND is_completed = 0";
                    args.add(String.valueOf(now));
                    break;
                case "Progressing":
                    selection += " AND ((? >= start_time AND ? <= end_time) OR (? >= start_time AND end_time IS NULL)) AND is_completed = 0";
                    args.add(String.valueOf(now));
                    args.add(String.valueOf(now));
                    args.add(String.valueOf(now));
                    break;
                case "Uncompleted":
                    selection += " AND ? > end_time AND end_time IS NOT NULL AND is_completed = 0";
                    args.add(String.valueOf(now));
                    break;
                case "Completed":
                    selection += " AND is_completed = 1";
                    break;
            }
        }

        String orderBy = "priority DESC, start_time ASC";
        Cursor cursor = db.query(DBHelper.getTableTasks(), null, selection, args.toArray(new String[0]), null, null, orderBy);

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        list.add(cursorToTask(cursor));
                    } while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        }
        return list;
    }

    public List<Task> getTasksByCategory(String category) {
        List<Task> list = new ArrayList<>();
        String selection = "category = ?";
        String[] args = {category};
        String orderBy = "start_time ASC";

        Cursor cursor = db.query(DBHelper.getTableTasks(), null, selection, args, null, null, orderBy);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        list.add(cursorToTask(cursor));
                    } while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        }
        return list;
    }

    // =========================================================================
    // 5. HELPER METHODS
    // =========================================================================

    public Task getTaskById(int id) {
        Task t = null;
        Cursor cursor = db.query(DBHelper.getTableTasks(), null, "id=?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    t = cursorToTask(cursor);
                    t.setNoteContent(getNoteContentByTaskId(id));
                }
            } finally {
                cursor.close();
            }
        }
        return t;
    }

    private Task cursorToTask(Cursor cursor) {
        Task t = new Task();
        t.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        t.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
        t.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));

        int startIdx = cursor.getColumnIndexOrThrow("start_time");
        if (!cursor.isNull(startIdx)) t.setStartTime(cursor.getLong(startIdx));

        int endIdx = cursor.getColumnIndexOrThrow("end_time");
        if (!cursor.isNull(endIdx)) t.setEndTime(cursor.getLong(endIdx));

        t.setStartDay(cursor.getString(cursor.getColumnIndexOrThrow("start_day")));
        t.setEndDay(cursor.getString(cursor.getColumnIndexOrThrow("end_day")));
        t.setCategory(cursor.getString(cursor.getColumnIndexOrThrow("category")));
        t.setCompleted(cursor.getInt(cursor.getColumnIndexOrThrow("is_completed")) == 1);
        t.setAllDay(cursor.getInt(cursor.getColumnIndexOrThrow("is_all_day")) == 1);
        t.setPriority(cursor.getInt(cursor.getColumnIndexOrThrow("priority")));
        t.setLocation(cursor.getString(cursor.getColumnIndexOrThrow("location")));
        return t;
    }

    public String getNoteContentByTaskId(int taskId) {
        String content = "";
        Cursor cursor = db.query(DBHelper.getTableNotes(), new String[]{"content"}, "task_id=?", new String[]{String.valueOf(taskId)}, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    content = cursor.getString(cursor.getColumnIndexOrThrow("content"));
                }
            } finally {
                cursor.close();
            }
        }
        return content;
    }

    public int countTodayTasks(String todayDate) {
        int count = 0;
        String query = "SELECT COUNT(*) FROM " + DBHelper.getTableTasks() + " WHERE start_day = ? AND is_completed = 0";
        Cursor cursor = db.rawQuery(query, new String[]{todayDate});
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) count = cursor.getInt(0);
            } finally {
                cursor.close();
            }
        }
        return count;
    }

    public int countProgressingTasks() {
        int count = 0;
        long now = System.currentTimeMillis();
        String query = "SELECT COUNT(*) FROM " + DBHelper.getTableTasks() +
                " WHERE ((? >= start_time AND ? <= end_time) OR (? >= start_time AND end_time IS NULL)) AND is_completed = 0";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(now), String.valueOf(now), String.valueOf(now)});
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) count = cursor.getInt(0);
            } finally {
                cursor.close();
            }
        }
        return count;
    }

    private Long convertDate(String dateString, boolean isStartOfDay) {
        if (dateString == null || dateString.isEmpty()) return null;
        try {
            Date date = DATE_FORMAT_INPUT.parse(dateString);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            if (isStartOfDay) {
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
            } else {
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MILLISECOND, 999);
            }
            return cal.getTimeInMillis();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
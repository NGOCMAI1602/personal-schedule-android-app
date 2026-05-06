package com.example.todolist.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.todolist.models.Task;
import com.example.todolist.database.DBHelper;

import java.util.ArrayList;
import java.util.List;

public class CompletedTaskDAO {
    private SQLiteDatabase db;
    private DBHelper dbHelper;
    public CompletedTaskDAO(Context context) {
        dbHelper = new DBHelper(context);
    }

    public void open() {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public List<Task> getTasks(String category) {
        List<Task> list = new ArrayList<>();

        String selection = "1=1";
        List<String> args = new ArrayList<>();

        if (category != null) {
            selection += " AND category=?";
            args.add(category);
        }

        // Sắp xếp: Ưu tiên (Cao xuống Thấp) -> Thời gian bắt đầu (Sớm đến Muộn)
        String orderBy = "is_completed ASC, priority DESC, start_time ASC";

        Cursor cursor = db.query(DBHelper.getTableTasks(),
                null,
                selection,
                args.toArray(new String[0]),
                null, null, orderBy);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Task t = new Task();
                t.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                t.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
                t.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                t.setStartTime(cursor.isNull(cursor.getColumnIndexOrThrow("start_time")) ? null : cursor.getLong(cursor.getColumnIndexOrThrow("start_time")));
                t.setEndTime(cursor.isNull(cursor.getColumnIndexOrThrow("end_time")) ? null : cursor.getLong(cursor.getColumnIndexOrThrow("end_time")));
                t.setStartDay(cursor.getString(cursor.getColumnIndexOrThrow("start_day")));
                t.setEndDay(cursor.getString(cursor.getColumnIndexOrThrow("end_day")));
                t.setCategory(cursor.getString(cursor.getColumnIndexOrThrow("category")));
                t.setCompleted(cursor.getInt(cursor.getColumnIndexOrThrow("is_completed")) == 1);
                t.setAllDay(cursor.getInt(cursor.getColumnIndexOrThrow("is_all_day")) == 1);
                t.setPriority(cursor.getInt(cursor.getColumnIndexOrThrow("priority")));
                list.add(t);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public Task getTaskById(int id) {
        Cursor cursor = null;
        Task t = null;
        try {
            cursor = db.query(DBHelper.getTableTasks(),
                    null,
                    "id=?",
                    new String[]{String.valueOf(id)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                t = new Task();
                t.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                t.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
                t.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                t.setStartTime(cursor.isNull(cursor.getColumnIndexOrThrow("start_time")) ? null : cursor.getLong(cursor.getColumnIndexOrThrow("start_time")));
                t.setEndTime(cursor.isNull(cursor.getColumnIndexOrThrow("end_time")) ? null : cursor.getLong(cursor.getColumnIndexOrThrow("end_time")));
                t.setStartDay(cursor.getString(cursor.getColumnIndexOrThrow("start_day")));
                t.setEndDay(cursor.getString(cursor.getColumnIndexOrThrow("end_day")));
                t.setCategory(cursor.getString(cursor.getColumnIndexOrThrow("category")));
                t.setCompleted(cursor.getInt(cursor.getColumnIndexOrThrow("is_completed")) == 1);
                t.setAllDay(cursor.getInt(cursor.getColumnIndexOrThrow("is_all_day")) == 1);
                t.setPriority(cursor.getInt(cursor.getColumnIndexOrThrow("priority")));
                t.setLocation(cursor.getString(cursor.getColumnIndexOrThrow("location")));

                String noteContent = getNoteContentByTaskId(id);
                t.setNoteContent(noteContent);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return t;
    }

    public String getNoteContentByTaskId(int taskId) {
        String content = "";

        Cursor cursor = db.query(DBHelper.getTableNotes(),
                new String[]{"content"},
                "task_id=?",
                new String[]{String.valueOf(taskId)},
                null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                content = cursor.getString(cursor.getColumnIndexOrThrow("content"));
            }
            cursor.close();
        }

        return content;
    }
}
package com.example.todolist.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "tuitentien.db";
    private static final int DB_VERSION = 1;

    // Bảng TASKs
    private static final String TABLE_TASKS = "tasks";
    private static final String COL_ID = "id";
    private static final String COL_TITLE = "title";
    private static final String COL_DESC = "description";
    private static final String COL_START = "start_time";
    private static final String COL_END = "end_time";
    private static final String COL_START_DAY = "start_day";
    private static final String COL_END_DAY = "end_day";
    private static final String COL_PRIORITY = "priority";
    private static final String COL_LOCATION = "location";
    private static final String COL_IS_ALL_DAY = "is_all_day";
    private static final String COL_CATEGORY = "category";
    private static final String COL_REPEAT = "repeat_type";
    private static final String COL_NOTIFY = "notification_time";
    private static final String COL_COMPLETED = "is_completed";

    // Bảng TASK_NOTES
    private static final String TABLE_NOTES = "task_notes";
    private static final String NOTE_ID = "id";
    private static final String NOTE_TASK_ID = "task_id";
    private static final String NOTE_CONTENT = "content";
    private static final String NOTE_CREATED_AT = "created_at";
    private static final String NOTE_UPDATED_AT = "updated_at";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TẠO BẢNG TASKS
        String createTasks = "CREATE TABLE " + TABLE_TASKS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TITLE + " TEXT NOT NULL, "
                + COL_DESC + " TEXT, "
                + COL_START + " INTEGER, "
                + COL_END + " INTEGER, "
                + COL_START_DAY + " TEXT NOT NULL, "
                + COL_END_DAY + " TEXT, "
                + COL_PRIORITY + " INTEGER DEFAULT 1, "
                + COL_LOCATION + " TEXT, "
                + COL_IS_ALL_DAY + " INTEGER DEFAULT 0, "
                + COL_CATEGORY + " TEXT, "
                + COL_REPEAT + " TEXT, "
                + COL_NOTIFY + " INTEGER, "
                + COL_COMPLETED + " INTEGER DEFAULT 0)";
        db.execSQL(createTasks);

        // TẠO BẢNG NOTES
        String createNotes = "CREATE TABLE " + TABLE_NOTES + " ("
                + NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NOTE_TASK_ID + " INTEGER NOT NULL, "
                + NOTE_CONTENT + " TEXT, "
                + NOTE_CREATED_AT + " INTEGER, "
                + NOTE_UPDATED_AT + " INTEGER, "
                + "FOREIGN KEY(" + NOTE_TASK_ID + ") REFERENCES "
                + TABLE_TASKS + "(" + COL_ID + ") ON DELETE CASCADE)"; // Tham chiếu đến TABLE_TASKS
        db.execSQL(createNotes);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Vì đây là Version 1, onUpgrade sẽ được để trống.
    }

    public static String getTableTasks() {return TABLE_TASKS;}
    public static String getTableNotes() {return TABLE_NOTES;}

}
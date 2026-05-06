package com.example.todolist.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.todolist.database.DBHelper;

import java.util.HashSet;
import java.util.Calendar;
import java.util.Locale;
import java.util.Set;
import java.text.SimpleDateFormat;

public class CalendarDAO {
    private SQLiteDatabase db;
    private DBHelper dbHelper;
    private static final SimpleDateFormat DB_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public CalendarDAO(Context context) {
        dbHelper = new DBHelper(context);
    }

    public void open() {
        db = dbHelper.getReadableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public Set<String> getTaskDaysInMonth(Calendar monthStartCalendar, Calendar monthEndCalendar) {
        Set<String> taskDays = new HashSet<>();

        // Chuyển thành milliseconds để truy vấn trong DB
        long startMs = monthStartCalendar.getTimeInMillis();
        long endMs = monthEndCalendar.getTimeInMillis();

        // Truy vấn: Lấy ra các task có start_time hoặc end_time nằm trong phạm vi tháng.
        // Cần truy vấn cả cột start_time và end_time vì task có thể kéo dài qua nhiều ngày.
        // Tuy nhiên, để đơn giản, ta chỉ cần các task có Start/End nằm trong tháng hoặc vượt ra ngoài tháng nhưng vẫn giao với tháng đó.

        // Lấy tất cả các task có thời gian bắt đầu HOẶC kết thúc nằm trong phạm vi tháng này
        // Hoặc các task bắt đầu trước và kết thúc sau tháng này (kéo dài qua tháng)
        String selection = "(start_time BETWEEN ? AND ?) OR (end_time BETWEEN ? AND ?)";
        String[] selectionArgs = new String[]{
                String.valueOf(startMs),
                String.valueOf(endMs),
                String.valueOf(startMs),
                String.valueOf(endMs)
        };

        // Thêm điều kiện cho các task kéo dài qua tháng
        selection += " OR (start_time < ? AND end_time > ?)";
        String[] tempArgs = new String[selectionArgs.length + 2];
        System.arraycopy(selectionArgs, 0, tempArgs, 0, selectionArgs.length);
        tempArgs[selectionArgs.length] = String.valueOf(startMs);
        tempArgs[selectionArgs.length + 1] = String.valueOf(endMs);
        selectionArgs = tempArgs;


        Cursor cursor = db.query(DBHelper.getTableTasks(),
                new String[]{"start_day", "end_day"},
                selection,
                selectionArgs,
                null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String startDay = cursor.getString(cursor.getColumnIndexOrThrow("start_day"));
                String endDay = cursor.getString(cursor.getColumnIndexOrThrow("end_day"));

                // Logic: Nếu start_day hoặc end_day nằm trong tháng này, ta thêm chúng vào Set

                try {
                    // Lấy ngày bắt đầu và kết thúc của Task
                    Calendar current = Calendar.getInstance();
                    if (startDay != null) {
                        current.setTime(DB_DATE_FORMAT.parse(startDay));
                    } else {
                        continue;
                    }

                    Calendar end = Calendar.getInstance();
                    if (endDay != null) {
                        end.setTime(DB_DATE_FORMAT.parse(endDay));
                    } else {
                        end = (Calendar) current.clone();
                    }

                    // Lặp qua từng ngày giữa Start và End để thêm vào taskDays
                    while (!current.after(end)) {
                        // Kiểm tra ngày có thuộc tháng đang xem không
                        if (current.get(Calendar.MONTH) == monthStartCalendar.get(Calendar.MONTH) &&
                                current.get(Calendar.YEAR) == monthStartCalendar.get(Calendar.YEAR)) {
                            taskDays.add(DB_DATE_FORMAT.format(current.getTime()));
                        }
                        current.add(Calendar.DATE, 1);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            cursor.close();
        }

        return taskDays;
    }
}
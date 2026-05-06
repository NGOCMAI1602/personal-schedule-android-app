package com.example.todolist.repository;

import android.content.Context;

import com.example.todolist.dao.CalendarDAO;

import java.util.Calendar;
import java.util.Set;

public class CalendarRepository {
    private CalendarDAO calendarDAO;

    public CalendarRepository(Context context) {
        calendarDAO = new CalendarDAO(context);
    }

    public Set<String> getTaskDaysInMonth(Calendar start, Calendar end) {
        open();
        Set<String> taskDays = calendarDAO.getTaskDaysInMonth(start, end);
        close();
        return taskDays;
    }

    public void open() {
        calendarDAO.open();
    }
    public void close() {
        calendarDAO.close();
    }
}

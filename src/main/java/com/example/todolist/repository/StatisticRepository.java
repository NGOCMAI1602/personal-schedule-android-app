package com.example.todolist.repository;

import android.content.Context;
import com.example.todolist.dao.StatisticDAO;

public class StatisticRepository {
    private StatisticDAO statisticsDAO;

    public StatisticRepository(Context context) {
        this.statisticsDAO = new StatisticDAO(context);
    }

    public int countAllTasks() {
        return statisticsDAO.countAllTasks();
    }

    public int countCompletedTasks() {
        return statisticsDAO.countCompletedTasks();
    }

    public int countUpcomingTasks() {
        return statisticsDAO.countUpcomingTasks();
    }

    public int countInProgressTasks() {
        return statisticsDAO.countInProgressTasks();
    }

    public int countCompletedTasksByPriority(int priority) {
        return statisticsDAO.countCompletedTasksByPriority(priority);
    }

    public int getWeeklyCompletionRate() {
        return statisticsDAO.getWeeklyCompletionRate();
    }
}
package com.example.todolist.repository;

import android.content.Context;
import com.example.todolist.dao.TaskDAO;
import com.example.todolist.models.Task;
import java.util.List;

public class CompletedTaskRepository {
    private final TaskDAO taskDAO;

    public CompletedTaskRepository(Context context) {
        taskDAO = new TaskDAO(context);
    }

    public void open() {
        taskDAO.open();
    }

    public void close() {
        taskDAO.close();
    }

    // --- Hàm cũ: Lọc theo Category (Work, Personal...) ---
    public List<Task> getFilteredTasks(String category) {
        return taskDAO.getTasksByCategory(category);
    }

    // --- CÁC HÀM MỚI CHO CHỨC NĂNG DRILL-DOWN ---

    public List<Task> getUpcomingTasks() {
        return taskDAO.getTasks("Upcoming");
    }

    public List<Task> getProgressingTasks() {
        return taskDAO.getTasks("Progressing");
    }

    public List<Task> getCompletedTasksOnly() {
        return taskDAO.getTasks("Completed");
    }

    public List<Task> getAllTasks() {
        return taskDAO.getTasks(null);
    }
}
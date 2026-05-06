package com.example.todolist.repository;

import android.content.Context;
import com.example.todolist.dao.TaskDAO;
import com.example.todolist.models.Task;
import java.util.List;

public class TaskRepository {
    private TaskDAO taskDAO;

    public TaskRepository(Context context) {
        taskDAO = new TaskDAO(context);
        taskDAO.open();
    }

    public long insert(Task task) {
        return taskDAO.insertTask(task);
    }
    public void update(Task task) {
        taskDAO.updateTask(task);
    }
    public void delete(int id) {
        taskDAO.deleteTask(id);
    }
    public Task getTaskById(int id) {
        return taskDAO.getTaskById(id);
    }
    public List<Task> getFilteredTasks(String progress) {
        return taskDAO.getTasks(progress);
    }
    public int countTodayTasks(String todayDate) {
        return taskDAO.countTodayTasks(todayDate);
    }
    public int countProgressingTasks() {
        return taskDAO.countProgressingTasks();
    }
    public void open() {
        taskDAO.open();
    }
    public void close() {
        taskDAO.close();
    }
}
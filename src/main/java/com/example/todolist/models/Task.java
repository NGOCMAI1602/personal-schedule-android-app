package com.example.todolist.models;

import java.io.Serializable;

public class Task implements Serializable {
    private int id;
    private String title;
    private String description;
    private Long startTime;
    private Long endTime;
    private String startDay;
    private String endDay;
    private String category;
    private String repeatType;
    private int notificationTime;
    private boolean isCompleted;
    private boolean isAllDay;
    private String location;
    private int priority; // 1: Low, 2: Medium, 3: High
    private String noteContent;

    public Task() {
        // Default
        this.repeatType = "NONE";
        this.priority = 1;
        this.isCompleted = false;
        this.isAllDay = false;
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Long getStartTime() { return startTime; }
    public Long getEndTime() { return endTime; }
    public String getStartDay() { return startDay; }
    public String getEndDay() { return endDay; }
    public String getCategory() { return category; }
    public String getRepeatType() { return repeatType; }
    public int getNotificationTime() { return notificationTime; }
    public boolean isCompleted() { return isCompleted; }
    public boolean isAllDay() { return isAllDay; }
    public String getLocation() { return location; }
    public int getPriority() { return priority; }
    public String getNoteContent() { return noteContent; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setStartTime(Long startTime) { this.startTime = startTime; }
    public void setEndTime(Long endTime) { this.endTime = endTime; }
    public void setStartDay(String startDay) { this.startDay = startDay; }
    public void setEndDay(String endDay) { this.endDay = endDay; }
    public void setCategory(String category) { this.category = category; }
    public void setRepeatType(String repeatType) { this.repeatType = repeatType; }
    public void setNotificationTime(int notificationTime) { this.notificationTime = notificationTime; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
    public void setAllDay(boolean allDay) { isAllDay = allDay; }
    public void setLocation(String location) { this.location = location; }
    public void setPriority(int priority) { this.priority = priority; }
    public void setNoteContent(String noteContent) { this.noteContent = noteContent; }
}
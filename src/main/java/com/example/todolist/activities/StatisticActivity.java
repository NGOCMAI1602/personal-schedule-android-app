package com.example.todolist.activities;

import android.content.Intent; // Import thêm Intent
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.todolist.R;
import com.example.todolist.repository.StatisticRepository;
import java.util.Locale;

public class StatisticActivity extends AppCompatActivity {

    private StatisticRepository repository;
    private ImageView btnBack;

    private TextView tvProgressPercent;

    // Khai báo các View cha (Các ô vuông CardView) để bắt sự kiện Click
    private View statAllTasks, statCompletedTasks, statUpcomingTasks, statInProgressTasks;

    private TextView tvAllTasksTitle, tvAllTasksValue;
    private TextView tvCompletedTasksTitle, tvCompletedTasksValue;
    private TextView tvUpcomingTasksTitle, tvUpcomingTasksValue;
    private TextView tvInProgressTasksTitle, tvInProgressTasksValue;

    // Các biến Priority giữ nguyên
    private TextView tvHighPriorityTitle, tvHighPriorityValue;
    private TextView tvMediumPriorityTitle, tvMediumPriorityValue;
    private TextView tvLowPriorityTitle, tvLowPriorityValue;

    private static final int PRIORITY_HIGH = 3;
    private static final int PRIORITY_MEDIUM = 2;
    private static final int PRIORITY_LOW = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic);

        repository = new StatisticRepository(this);

        initViews();
        setupEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStatisticsData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tvProgressPercent = findViewById(R.id.tv_progress_percent);

        // --- Ánh xạ View Cha (Để click) và View Con (Để hiển thị số) ---

        // 1. All Tasks
        statAllTasks = findViewById(R.id.stat_all_tasks);
        tvAllTasksTitle = statAllTasks.findViewById(R.id.tv_stat_title);
        tvAllTasksValue = statAllTasks.findViewById(R.id.tv_stat_value);

        // 2. Completed Tasks
        statCompletedTasks = findViewById(R.id.stat_completed_tasks);
        tvCompletedTasksTitle = statCompletedTasks.findViewById(R.id.tv_stat_title);
        tvCompletedTasksValue = statCompletedTasks.findViewById(R.id.tv_stat_value);

        // 3. Upcoming Tasks
        statUpcomingTasks = findViewById(R.id.stat_upcoming_tasks);
        tvUpcomingTasksTitle = statUpcomingTasks.findViewById(R.id.tv_stat_title);
        tvUpcomingTasksValue = statUpcomingTasks.findViewById(R.id.tv_stat_value);

        // 4. In Progress Tasks
        statInProgressTasks = findViewById(R.id.stat_in_progress_tasks);
        tvInProgressTasksTitle = statInProgressTasks.findViewById(R.id.tv_stat_title);
        tvInProgressTasksValue = statInProgressTasks.findViewById(R.id.tv_stat_value);

        // Các view Priority (Giữ nguyên, thường không drill-down phần này hoặc làm tương tự)
        View statHighPriority = findViewById(R.id.stat_high_priority);
        tvHighPriorityTitle = statHighPriority.findViewById(R.id.tv_priority_title);
        tvHighPriorityValue = statHighPriority.findViewById(R.id.tv_priority_stat_value);

        View statMediumPriority = findViewById(R.id.stat_medium_priority);
        tvMediumPriorityTitle = statMediumPriority.findViewById(R.id.tv_priority_title);
        tvMediumPriorityValue = statMediumPriority.findViewById(R.id.tv_priority_stat_value);

        View statLowPriority = findViewById(R.id.stat_low_priority);
        tvLowPriorityTitle = statLowPriority.findViewById(R.id.tv_priority_title);
        tvLowPriorityValue = statLowPriority.findViewById(R.id.tv_priority_stat_value);
    }

    private void setupEvents() {
        btnBack.setOnClickListener(v -> finish());

        // --- SỰ KIỆN CLICK DRILL-DOWN (MỚI THÊM) ---

        // 1. Click vào ô "Completed Tasks"
        statCompletedTasks.setOnClickListener(v -> {
            navigateToTaskList("COMPLETED");
        });

        // 2. Click vào ô "Upcoming Tasks"
        statUpcomingTasks.setOnClickListener(v -> {
            navigateToTaskList("UPCOMING");
        });

        // 3. Click vào ô "Progressing Tasks"
        statInProgressTasks.setOnClickListener(v -> {
            navigateToTaskList("PROGRESSING");
        });

        // 4. Click vào ô "All Tasks"
        statAllTasks.setOnClickListener(v -> {
            navigateToTaskList("ALL");
        });
    }

    // Hàm chung để chuyển màn hình
    private void navigateToTaskList(String filterType) {
        // Chuyển sang CompletedTasksActivity (Vì file README nói đây là MH Danh sách Task)
        Intent intent = new Intent(StatisticActivity.this, CompletedTasksActivity.class);

        // Gửi kèm "mật mã" lọc dữ liệu
        intent.putExtra("FILTER_TYPE", filterType);

        startActivity(intent);
    }

    private void loadStatisticsData() {
        // (Giữ nguyên logic load dữ liệu cũ của bạn)
        int weeklyRate = repository.getWeeklyCompletionRate();
        tvProgressPercent.setText(String.format(Locale.getDefault(), "%d%%", weeklyRate));

        int allTasks = repository.countAllTasks();
        int completedTasks = repository.countCompletedTasks();

        tvAllTasksTitle.setText("All Tasks");
        tvAllTasksValue.setText(String.valueOf(allTasks));

        tvCompletedTasksTitle.setText("Completed Tasks");
        tvCompletedTasksValue.setText(String.valueOf(completedTasks));

        int upcomingTasks = repository.countUpcomingTasks();
        int inProgressTasks = repository.countInProgressTasks();

        tvUpcomingTasksTitle.setText("Upcoming Tasks");
        tvUpcomingTasksValue.setText(String.valueOf(upcomingTasks));

        tvInProgressTasksTitle.setText("Progressing Tasks");
        tvInProgressTasksValue.setText(String.valueOf(inProgressTasks));

        int highCompleted = repository.countCompletedTasksByPriority(PRIORITY_HIGH);
        tvHighPriorityTitle.setText("HIGH");
        tvHighPriorityValue.setText(String.valueOf(highCompleted));
        tvHighPriorityValue.setBackgroundResource(R.color.priority_high);

        int mediumCompleted = repository.countCompletedTasksByPriority(PRIORITY_MEDIUM);
        tvMediumPriorityTitle.setText("NORMAL");
        tvMediumPriorityValue.setText(String.valueOf(mediumCompleted));
        tvMediumPriorityValue.setBackgroundResource(R.color.priority_medium);

        int lowCompleted = repository.countCompletedTasksByPriority(PRIORITY_LOW);
        tvLowPriorityTitle.setText("LOW");
        tvLowPriorityValue.setText(String.valueOf(lowCompleted));
        tvLowPriorityValue.setBackgroundResource(R.color.priority_low);
    }
}
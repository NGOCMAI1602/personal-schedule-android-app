package com.example.todolist.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.todolist.R;
import com.example.todolist.adapter.CompletedTaskAdapter;
import com.example.todolist.models.Task;
import com.example.todolist.repository.CompletedTaskRepository;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class CompletedTasksActivity extends AppCompatActivity implements CompletedTaskAdapter.OnTaskStatusChangeListener {

    private CompletedTaskRepository repository;
    private ListView listViewTasks;
    private ChipGroup chipGroupCategory;
    private TextView tvEmpty;
    private TextView tvTitle; // Thêm biến để đổi tên màn hình
    private ImageView btnBack;

    private String currentCategory = "Work";
    private String filterType = null; // Biến lưu loại lọc từ Statistic gửi sang

    private static final int DETAIL_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completed_tasks);

        repository = new CompletedTaskRepository(this);

        // 1. Nhận dữ liệu từ StatisticActivity gửi sang
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("FILTER_TYPE")) {
            filterType = intent.getStringExtra("FILTER_TYPE");
        }

        initViews();
        setupEvents();
        loadTasks();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        listViewTasks = findViewById(R.id.listViewCompletedTasks);
        chipGroupCategory = findViewById(R.id.chipGroupCategory);
        tvEmpty = findViewById(R.id.tvEmptyList);

        // Giả sử trong layout bạn có TextView tiêu đề (hoặc dùng ActionBar)
        // Nếu không có ID tvTitle trong layout, bạn có thể bỏ qua dòng này hoặc findViewById theo ID thực tế
        // tvTitle = findViewById(R.id.tvTitle);
    }

    private void setupEvents() {
        btnBack.setOnClickListener(v -> finish());

        // 2. Logic hiển thị giao diện theo chế độ Lọc
        if (filterType != null) {
            // --- CHẾ ĐỘ DRILL-DOWN (TỪ THỐNG KÊ) ---
            // Ẩn thanh chọn Category đi vì ta đang lọc theo Status
            chipGroupCategory.setVisibility(View.GONE);

            // Đổi tiêu đề màn hình (Nếu có ActionBar hoặc TextView Title)
            // if (tvTitle != null) tvTitle.setText(filterType + " TASKS");

        } else {
            // --- CHẾ ĐỘ BÌNH THƯỜNG (CHỌN CATEGORY) ---
            chipGroupCategory.setVisibility(View.VISIBLE);

            // Lọc Task khi Category thay đổi
            chipGroupCategory.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId != -1) {
                    Chip chip = findViewById(checkedId);
                    currentCategory = chip.getText().toString();
                    loadTasks();
                }
            });

            // Mặc định chọn chip "Work" nếu chưa chọn gì
            if (chipGroupCategory.getCheckedChipId() == -1) {
                ((Chip)findViewById(R.id.chipWork)).setChecked(true);
            }
        }

        // Click vào Task để mở Detail
        listViewTasks.setOnItemClickListener((parent, view, position, id) -> {
            Task selectedTask = (Task) parent.getItemAtPosition(position);
            if (selectedTask != null) {
                Intent intent = new Intent(CompletedTasksActivity.this, DetailTaskActivity.class);
                intent.putExtra("TASK_ID", selectedTask.getId());
                startActivityForResult(intent, DETAIL_REQUEST_CODE);
            } else {
                Toast.makeText(this, "Something went wrong.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTasks() {
        repository.open();
        List<Task> tasks;

        // 3. Phân luồng lấy dữ liệu
        if (filterType != null) {
            // Nếu có Filter từ Statistic -> Gọi hàm lọc theo Status
            switch (filterType) {
                case "UPCOMING":
                    tasks = repository.getUpcomingTasks();
                    tvEmpty.setText("No upcoming tasks found.");
                    break;
                case "PROGRESSING":
                    tasks = repository.getProgressingTasks();
                    tvEmpty.setText("No progressing tasks found.");
                    break;
                case "COMPLETED":
                    tasks = repository.getCompletedTasksOnly(); // Hàm lấy task đã xong
                    tvEmpty.setText("No completed tasks found.");
                    break;
                case "ALL":
                    tasks = repository.getAllTasks(); // Hàm lấy tất cả task
                    tvEmpty.setText("No tasks found in system.");
                    break;
                default:
                    tasks = repository.getAllTasks();
                    break;
            }
        } else {
            // Nếu không có Filter -> Chạy logic cũ (Lọc theo Category)
            tasks = repository.getFilteredTasks(currentCategory);
            tvEmpty.setText("There is no task with category: " + currentCategory);
        }

        repository.close();

        // Hiển thị list hoặc thông báo rỗng
        if (tasks.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            listViewTasks.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            listViewTasks.setVisibility(View.VISIBLE);
            CompletedTaskAdapter adapter = new CompletedTaskAdapter(this, tasks, null, this);
            listViewTasks.setAdapter(adapter);
        }
    }

    @Override
    public void onStatusChanged() {
        loadTasks();
        setResult(RESULT_OK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DETAIL_REQUEST_CODE && resultCode == RESULT_OK) {
            loadTasks();
            setResult(RESULT_OK);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasks();
    }

    @Override
    protected void onDestroy() {
        repository.close();
        super.onDestroy();
    }
}
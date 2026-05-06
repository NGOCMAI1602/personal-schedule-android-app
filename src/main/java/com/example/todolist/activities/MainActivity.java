package com.example.todolist.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.todolist.adapter.TaskAdapter;
import com.example.todolist.R;
import com.example.todolist.models.Task;
import com.example.todolist.repository.TaskRepository;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskStatusChangeListener {
    private TaskRepository repository;
    private ListView listViewTasks;
    private ChipGroup chipGroupProgress;
    private ImageView btnMenu;
    private TextView tvEmpty, tvDate, tvCountUpcoming, tvCountInProgress;
    private String currentProgress = "Upcoming";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repository = new TaskRepository(this);
        initViews();
        setupEvents();

        // Thiết lập Ngày tháng
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("en", "US"));
        tvDate.setText(dateFormat.format(new Date()));

        // Cập nhật thống kê ngay khi Activity được tạo
        updateDashboardCounts();
        checkNotificationPermission();
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void initViews() {
        listViewTasks = findViewById(R.id.listViewTasks);
        chipGroupProgress = findViewById(R.id.chipGroupProgress);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvDate = findViewById(R.id.tvDate);
        btnMenu = findViewById(R.id.btnMenu);

        tvCountUpcoming = findViewById(R.id.tvCountUpcoming);
        tvCountInProgress = findViewById(R.id.tvCountInProgress);

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> startActivity(new Intent(this, AddTaskActivity.class)));
    }

    private void setupEvents() {
        chipGroupProgress.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId != -1) {
                Chip chip = findViewById(checkedId);
                currentProgress = chip.getText().toString();
                if (currentProgress.equals("Progressing")) {
                    currentProgress = "Progressing"; // Giữ nguyên để khớp với TaskDAO
                } else if (currentProgress.equals("Upcoming")) {
                    currentProgress = "Upcoming"; // Giữ nguyên để khớp với TaskDAO
                } else if (currentProgress.equals("Uncompleted")) {
                    currentProgress = "Uncompleted"; // Giữ nguyên để khớp với TaskDAO
                }

                loadTasks();
            }
        });
        // Mặc định chọn Sắp tới (Upcoming)
        ((Chip)findViewById(R.id.chipUpcoming)).setChecked(true);

        listViewTasks.setOnItemClickListener((parent, view, position, id) -> {
            Task selectedTask = (Task) parent.getItemAtPosition(position);

            if (selectedTask != null) {
                // 2. Tạo Intent và truyền ID
                Intent intent = new Intent(MainActivity.this, DetailTaskActivity.class);
                intent.putExtra("TASK_ID", selectedTask.getId()); // Truyền ID của Task

                // 3. Khởi chạy Activity và chờ kết quả
                startActivityForResult(intent, 1);
            } else {
                Toast.makeText(this, "Something went wrong.", Toast.LENGTH_SHORT).show();
            }
        });

        btnMenu.setOnClickListener(v -> showPopupMenu(v));
    }

    private void loadTasks() {
        List<Task> tasks = repository.getFilteredTasks(currentProgress);

        if (tasks.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("There is no task with progress " + currentProgress);
            listViewTasks.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            listViewTasks.setVisibility(View.VISIBLE);
            TaskAdapter adapter = new TaskAdapter(this, tasks, repository, this);
            listViewTasks.setAdapter(adapter);
        }
    }

    // --- Cập nhật thông tin thống kê trên header ---
    private void updateDashboardCounts() {
        SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayDbFormat = dbDateFormat.format(new Date());

        int countToday = repository.countTodayTasks(todayDbFormat);
        int countProgress = repository.countProgressingTasks();

        tvCountUpcoming.setText("Today: " + countToday);
        tvCountInProgress.setText("Progressing: " + countProgress);
    }
    // --- XỬ LÝ KẾT QUẢ TRẢ VỀ TỪ TaskDetailActivity ---
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Khi TaskDetailActivity trả về RESULT_OK (sau khi Lưu hoặc Xóa),
            // gọi lại hàm load và update để làm mới dữ liệu ngay lập tức.
            loadTasks();
            updateDashboardCounts();
        }
    }

    // Phương thức hiển thị PopupMenu
    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.main_menu, popup.getMenu());

        // Thiết lập lắng nghe sự kiện khi một item trong menu được chọn
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.mnuGetCompleted) {
                    Intent intent = new Intent(MainActivity.this, CompletedTasksActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.mnuCalendar) {
                    Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.mnuNotes) {
                    Intent intent = new Intent(MainActivity.this, StatisticActivity.class);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
        popup.show();
    }

    // --- TRIỂN KHAI PHƯƠNG THỨC TỪ INTERFACE ---
    @Override
    public void onStatusChanged() {
        loadTasks();
        updateDashboardCounts();
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadTasks();
        updateDashboardCounts(); // Cập nhật lại số liệu khi quay lại màn hình
    }

    @Override
    protected void onDestroy() {
        repository.close();
        super.onDestroy();
    }
}
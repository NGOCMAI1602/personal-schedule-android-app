package com.example.todolist.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.todolist.R;
import com.example.todolist.models.Task;
import com.example.todolist.repository.TaskRepository;
import com.example.todolist.utils.AlarmScheduler;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DetailTaskActivity extends AppCompatActivity {

    private TaskRepository repository;
    private Task currentTask;
    private boolean isEditMode = false;

    private ImageButton btnBack, btnDelete, btnEdit;
    private Button btnSave;

    private EditText etTitle, etDescription, etLocation, etNote;
    private TextView tvCategory, tvProgress, tvStartTime, tvEndTime;
    private View priorityDivider;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    private final Calendar calendar = Calendar.getInstance();

    private Long tempStartTime = null;
    private Long tempEndTime = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_task);

        repository = new TaskRepository(this);
        initViews();
        setupListeners();

        // Lấy Task ID từ Intent
        int taskId = getIntent().getIntExtra("TASK_ID", -1);

        if (taskId != -1) {
            loadTaskData(taskId);
        } else {
            Toast.makeText(this, "Task ID Not Found.", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Mặc định ở chế độ chỉ đọc
        setEditMode(false);
    }

    private void initViews() {
        // Header
        btnBack = findViewById(R.id.btn_back);
        btnDelete = findViewById(R.id.btn_delete);
        btnEdit = findViewById(R.id.btn_edit);
        btnSave = findViewById(R.id.btn_save);

        etTitle = findViewById(R.id.et_task_title);
        tvCategory = findViewById(R.id.tv_category);
        tvProgress = findViewById(R.id.tv_progress);
        priorityDivider = findViewById(R.id.priority_divider);
        etDescription = findViewById(R.id.et_task_description);
        tvStartTime = findViewById(R.id.tv_start_time);
        tvEndTime = findViewById(R.id.tv_end_time);
        etLocation = findViewById(R.id.et_location);
        etNote = findViewById(R.id.et_note);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnEdit.setOnClickListener(v -> setEditMode(true));
        btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog());
        btnSave.setOnClickListener(v -> saveTaskChanges());
    }

    // --- LOGIC TẢI DỮ LIỆU ---

    private void loadTaskData(int taskId) {
        repository.open();
        currentTask = repository.getTaskById(taskId);
        repository.close();

        if (currentTask != null) {
            // Đặt các giá trị ban đầu (Read Mode)
            etTitle.setText(currentTask.getTitle());
            tvCategory.setText(currentTask.getCategory());
            etDescription.setText(currentTask.getDescription());
            etLocation.setText(currentTask.getLocation());
            etNote.setText(currentTask.getNoteContent());

            tempStartTime = currentTask.getStartTime();
            tempEndTime = currentTask.getEndTime();

            updateTimeViews(tempStartTime, tempEndTime);
            updatePriorityAndProgressViews();

            checkCompletedStatus(currentTask.isCompleted());
        } else {
            Toast.makeText(this, "Lỗi: Không thể tải Task.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void checkCompletedStatus(boolean isCompleted) {
        if (isCompleted) {
            // Vô hiệu hóa và làm mờ các nút thao tác
            btnEdit.setEnabled(false);
            btnEdit.setAlpha(0.5f);

            btnDelete.setEnabled(false);
            btnDelete.setAlpha(0.5f);

            btnSave.setEnabled(false); // Vô hiệu hóa nút Lưu (nếu đang hiển thị)

            Toast.makeText(this, "Task Completed, Cannot Be Edit.", Toast.LENGTH_LONG).show();
        } else {
            btnEdit.setEnabled(true);
            btnEdit.setAlpha(1.0f);

            btnDelete.setEnabled(true);
            btnDelete.setAlpha(1.0f);

            btnSave.setEnabled(true);
        }
    }

    private void updateTimeViews(Long start, Long end) {
        if (start != null) {
            tvStartTime.setText(dateFormat.format(new Date(start)));
        } else {
            tvStartTime.setText("Start-Time Is Not Set");
        }

        if (end != null) {
            tvEndTime.setText(dateFormat.format(new Date(end)));
        } else {
            tvEndTime.setText("End-Time Is Not Set");
        }
    }

    private void updatePriorityAndProgressViews() {
        // Cập nhật Thanh ưu tiên
        int priorityColorId;
        switch (currentTask.getPriority()) {
            case 3: priorityColorId = R.color.priority_high; break;
            case 2: priorityColorId = R.color.priority_medium; break;
            default: priorityColorId = R.color.priority_low; break;
        }
        priorityDivider.setBackgroundColor(ContextCompat.getColor(this, priorityColorId));

        // Cập nhật Tiến độ
        String progress = determineProgressStatus(currentTask);
        tvProgress.setText(progress);
        tvProgress.setBackgroundResource(getProgressBackground(progress));
    }

    // --- LOGIC CHUYỂN ĐỔI CHẾ ĐỘ SỬA/XEM ---

    private void setEditMode(boolean enable) {
        // Nếu Task đã hoàn thành, không cho phép vào Edit Mode
        if (currentTask != null && currentTask.isCompleted()) {
            // Đảm bảo mọi thứ vẫn bị vô hiệu hóa
            checkCompletedStatus(true);
            return;
        }

        isEditMode = enable;

        // 1. Chuyển đổi nút Header
        btnSave.setVisibility(enable ? View.VISIBLE : View.GONE);
        btnEdit.setVisibility(enable ? View.GONE : View.VISIBLE);
        btnDelete.setVisibility(enable ? View.GONE : View.VISIBLE);

        // 2. Kích hoạt/Vô hiệu hóa các trường EditText
        etTitle.setEnabled(enable);
        etDescription.setEnabled(enable);
        etLocation.setEnabled(enable);
        etNote.setEnabled(enable);

        // 3. Thay đổi giao diện
        int activeBg = R.drawable.bg_edit_field_active;
        int inactiveBg = R.color.transparent;

        etTitle.setBackgroundResource(enable ? activeBg : inactiveBg);
        etDescription.setBackgroundResource(enable ? activeBg : inactiveBg);
        etLocation.setBackgroundResource(enable ? activeBg : inactiveBg);

        // Note có nền nổi bật riêng
        etNote.setBackgroundResource(enable ? R.drawable.bg_note_active : R.drawable.bg_note_highlight); // bg_note_active là một drawable khác khi chỉnh sửa

        // 4. Xử lý Time TextView (Dùng để mở Picker)
        if (enable) {
            tvStartTime.setOnClickListener(v -> showDateTimePicker(true)); // true = Start Time
            tvEndTime.setOnClickListener(v -> showDateTimePicker(false)); // false = End Time
        } else {
            tvStartTime.setOnClickListener(null);
            tvEndTime.setOnClickListener(null);
        }

        // 5. Yêu cầu focus vào Title khi chuyển sang Edit Mode
        if (enable) {
            etTitle.requestFocus();
            etTitle.setSelection(etTitle.getText().length());
        }
    }

    // --- LOGIC XỬ LÝ NÚT LƯU VÀ XÓA ---

    private void saveTaskChanges() {
        // 1. Kiểm tra đầu vào tối thiểu
        if (etTitle.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Title Cannot Be Empty.", Toast.LENGTH_SHORT).show();
            etTitle.requestFocus();
            return;
        }

        // 2. Cập nhật Task model
        currentTask.setTitle(etTitle.getText().toString().trim());
        currentTask.setDescription(etDescription.getText().toString().trim());
        currentTask.setLocation(etLocation.getText().toString().trim());
        currentTask.setNoteContent(etNote.getText().toString().trim());

        currentTask.setStartTime(tempStartTime);
        currentTask.setEndTime(tempEndTime);

        // Cần cập nhật lại StartDay/EndDay nếu thời gian thay đổi
        // LƯU Ý: Nếu thay đổi ngày, cần tính toán lại start_day và end_day string từ tempStartTime/tempEndTime
        if (tempStartTime != null) {
            currentTask.setStartDay(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(tempStartTime)));
        } else {
            currentTask.setStartDay(null);
        }

        // Hủy lịch cũ
        AlarmScheduler.cancelTaskNotification(this, currentTask);

        // 3. Lưu vào DB
        repository.open();
        repository.update(currentTask);
        repository.close();

        // Chỉ đặt lịch lại nếu Task chưa hoàn thành
        if (!currentTask.isCompleted()) {
            AlarmScheduler.scheduleTaskNotification(this, currentTask);
        }

        // 4. Chuyển về chế độ chỉ đọc và thông báo
        setEditMode(false);
        // gọi lại updatePriorityAndProgressViews() để cập nhật trạng thái mới sau khi lưu
        updatePriorityAndProgressViews();
        Toast.makeText(this, "Task Updated!", Toast.LENGTH_SHORT).show();

        // Thông báo cho MainActivity refresh dữ liệu
        setResult(RESULT_OK);
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm")
                .setMessage("Are you sure you want to delete this task? This action cannot be undone.")
                .setPositiveButton("OK", (dialog, which) -> {
                    repository.open();
                    repository.delete(currentTask.getId());
                    repository.close();

                    Toast.makeText(this, "Task Deleted: " + currentTask.getTitle(), Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK); // Thông báo cho MainActivity cần refresh
                    finish();
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }

    // --- LOGIC TIME/DATE PICKER ---

    private void showDateTimePicker(final boolean isStartTime) {
        // Lấy thời gian hiện tại để làm giá trị mặc định cho Picker
        final Calendar currentCal = Calendar.getInstance();
        if (isStartTime && tempStartTime != null) {
            currentCal.setTimeInMillis(tempStartTime);
        } else if (!isStartTime && tempEndTime != null) {
            currentCal.setTimeInMillis(tempEndTime);
        }

        int year = currentCal.get(Calendar.YEAR);
        int month = currentCal.get(Calendar.MONTH);
        int day = currentCal.get(Calendar.DAY_OF_MONTH);

        // 1. Mở Date Picker
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // 2. Sau khi chọn Ngày, mở Time Picker
                    TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                            (timeView, hourOfDay, minute) -> {
                                // 3. Sau khi chọn Giờ, cập nhật Calendar và biến tạm
                                calendar.set(selectedYear, selectedMonth, selectedDay, hourOfDay, minute);

                                if (isStartTime) {
                                    tempStartTime = calendar.getTimeInMillis();
                                } else {
                                    tempEndTime = calendar.getTimeInMillis();
                                }

                                // 4. Cập nhật lại giao diện
                                updateTimeViews(tempStartTime, tempEndTime);
                            },
                            currentCal.get(Calendar.HOUR_OF_DAY), currentCal.get(Calendar.MINUTE), true); // true cho định dạng 24h
                    timePickerDialog.show();
                },
                year, month, day);

        datePickerDialog.show();
    }

    // --- HÀM HỖ TRỢ XÁC ĐỊNH TRẠNG THÁI TIẾN ĐỘ ---
    // Cần phải đồng bộ với logic lọc trong TaskDAO
    private String determineProgressStatus(Task task) {
        if (task.isCompleted()) {
            return "Completed";
        }

        long now = System.currentTimeMillis();
        Long startTime = task.getStartTime();
        Long endTime = task.getEndTime();

        // 1. Đang làm (Progressing)
        if (startTime != null && now >= startTime && (endTime == null || now <= endTime)) {
            return "Progressing";
        }

        // 2. Quá hạn (Uncompleted)
        if (endTime != null && now > endTime) {
            return "Uncompleted"; // Quá hạn
        }

        // 3. Sắp tới (Upcoming)
        if (startTime != null && now < startTime) {
            return "Upcoming";
        }

        return "N/A";
    }

    private int getProgressBackground(String progress) {
        switch (progress) {
            case "Upcoming":
                return R.drawable.bg_progress_upcoming;
            case "Progressing":
                return R.drawable.bg_progress_progressing;
            case "Uncompleted":
                return R.drawable.bg_progress_overdue;
            default:
                return R.drawable.bg_rounded_progress;
        }
    }
}
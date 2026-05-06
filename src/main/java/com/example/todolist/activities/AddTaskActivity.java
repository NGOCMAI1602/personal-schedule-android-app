package com.example.todolist.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.todolist.models.Task;
import com.example.todolist.R;
import com.example.todolist.repository.TaskRepository;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import com.example.todolist.utils.AlarmScheduler;

public class AddTaskActivity extends AppCompatActivity {
    private EditText edtTitle, edtDesc, edtLocation, edtNote;
    private TextView tvStartDay, tvStartTime, tvEndDay, tvEndTime;
    private MaterialSwitch switchAllDay;
    private LinearLayout layoutTimeContainer;
    private ChipGroup cgCategory, cgPriority;
    private ImageView btnBack;
    private FloatingActionButton fabSave;

    private Calendar startCal = Calendar.getInstance();
    private Calendar endCal = Calendar.getInstance();
    private TaskRepository repository;
    private SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        repository = new TaskRepository(this);
        initViews();
        setupChips();
        setupPickers();
        setupEvents();
    }

    private void initViews() {
        edtTitle = findViewById(R.id.edtTitle);
        edtDesc = findViewById(R.id.edtDesc);
        edtLocation = findViewById(R.id.edtLocation);
        edtNote = findViewById(R.id.edtNote);
        tvStartDay = findViewById(R.id.tvStartDay);
        tvStartTime = findViewById(R.id.tvStartTime);
        tvEndDay = findViewById(R.id.tvEndDay);
        tvEndTime = findViewById(R.id.tvEndTime);
        switchAllDay = findViewById(R.id.switchAllDay);
        layoutTimeContainer = findViewById(R.id.layoutTimeContainer);
        cgCategory = findViewById(R.id.cgCategory);
        cgPriority = findViewById(R.id.cgPriority);
        btnBack = findViewById(R.id.btnBack);
        fabSave = findViewById(R.id.fabSave);

        // Mặc định endCal + 1 giờ
        endCal.add(Calendar.HOUR_OF_DAY, 1);
        updateDateTimeDisplay();
    }

    private void setupChips() {
        String[] cats = {"Work", "Personal", "Study", "Sport", "Other"};
        for (String c : cats) {
            Chip chip = new Chip(this);
            chip.setText(c);
            chip.setCheckable(true);
            cgCategory.addView(chip);
        }
        ((Chip)cgCategory.getChildAt(0)).setChecked(true); // Default

        String[] prios = {"Low", "Normal", "High"};
        for (int i=0; i<prios.length; i++) {
            Chip chip = new Chip(this);
            chip.setText(prios[i]);
            chip.setCheckable(true);
            chip.setTag(i+1); // Tag lưu giá trị 1, 2, 3
            cgPriority.addView(chip);
        }
        ((Chip)cgPriority.getChildAt(0)).setChecked(true);
    }

    private void setupPickers() {
        tvStartDay.setOnClickListener(v -> showDatePicker(startCal, tvStartDay));
        tvStartTime.setOnClickListener(v -> showTimePicker(startCal, tvStartTime));
        tvEndDay.setOnClickListener(v -> showDatePicker(endCal, tvEndDay));
        tvEndTime.setOnClickListener(v -> showTimePicker(endCal, tvEndTime));
    }

    private void showDatePicker(Calendar cal, TextView tv) {
        new DatePickerDialog(this, (view, year, month, day) -> {
            cal.set(year, month, day);
            tv.setText(dateFmt.format(cal.getTime()));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker(Calendar cal, TextView tv) {
        new TimePickerDialog(this, (view, hour, min) -> {
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, min);
            tv.setText(timeFmt.format(cal.getTime()));
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
    }

    private void updateDateTimeDisplay() {
        tvStartDay.setText(dateFmt.format(startCal.getTime()));
        tvStartTime.setText(timeFmt.format(startCal.getTime()));
        tvEndDay.setText(dateFmt.format(endCal.getTime()));
        tvEndTime.setText(timeFmt.format(endCal.getTime()));
    }

    private void setupEvents() {
        btnBack.setOnClickListener(v -> finish());

        switchAllDay.setOnCheckedChangeListener((view, isChecked) -> {
            layoutTimeContainer.setVisibility(isChecked ? View.GONE : View.VISIBLE);

            if (isChecked) {
                // All day -> set time về 00:00 và 23:59
                startCal.set(Calendar.HOUR_OF_DAY, 0);
                startCal.set(Calendar.MINUTE, 0);

                endCal.set(Calendar.HOUR_OF_DAY, 23);
                endCal.set(Calendar.MINUTE, 59);

                updateDateTimeDisplay();
            }
        });

        fabSave.setOnClickListener(v -> {
            String title = edtTitle.getText().toString();
            if (title.isEmpty()) {
                edtTitle.setError("Cannot be empty");
                return;
            }

            // ============================================================
            //  BẮT ĐẦU ĐOẠN LOGIC MỚI: KIỂM TRA THỜI GIAN
            // ============================================================

            long startTimeMillis = startCal.getTimeInMillis();
            long endTimeMillis = endCal.getTimeInMillis();

            // So sánh: Nếu thời gian kết thúc nhỏ hơn thời gian bắt đầu -> Báo lỗi
            if (endTimeMillis < startTimeMillis) {
                Toast.makeText(AddTaskActivity.this, "End time cannot be earlier than Start time!", Toast.LENGTH_SHORT).show();
                return; // Dừng lại, không chạy code bên dưới nữa
            }

            // ============================================================
            //  KẾT THÚC ĐOẠN LOGIC MỚI
            // ============================================================

            Task task = new Task();
            task.setTitle(title);
            task.setDescription(edtDesc.getText().toString());
            task.setAllDay(switchAllDay.isChecked());

            // Xử lý Category
            int catId = cgCategory.getCheckedChipId();
            if (catId != -1) {
                Chip c = findViewById(catId);
                task.setCategory(c.getText().toString());
            }

            // Xử lý Priority
            int priId = cgPriority.getCheckedChipId();
            if (priId != -1) {
                Chip c = findViewById(priId);
                task.setPriority((int) c.getTag());
            }

            if (!task.isAllDay()) {
                task.setStartTime(startCal.getTimeInMillis());
                task.setEndTime(endCal.getTimeInMillis());
            }
            task.setStartDay(tvStartDay.getText().toString());
            task.setEndDay(tvEndDay.getText().toString());
            task.setLocation(edtLocation.getText().toString());
            task.setNoteContent(edtNote.getText().toString());

            long newTaskId = repository.insert(task);

            if (newTaskId != -1) {
                task.setId((int) newTaskId); // Cập nhật ID cho object task

                // ĐẶT LỊCH THÔNG BÁO
                AlarmScheduler.scheduleTaskNotification(AddTaskActivity.this, task);

                Toast.makeText(AddTaskActivity.this, "Task Added", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(AddTaskActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
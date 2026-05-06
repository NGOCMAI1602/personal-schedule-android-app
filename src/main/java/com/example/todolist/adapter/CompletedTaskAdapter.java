package com.example.todolist.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.example.todolist.R;
import com.example.todolist.models.Task;
import com.example.todolist.repository.TaskRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CompletedTaskAdapter extends ArrayAdapter<Task> {
    private Context context;
    private List<Task> tasks;
    private TaskRepository repository;
    private OnTaskStatusChangeListener listener;

    public interface OnTaskStatusChangeListener {
        void onStatusChanged();
    }

    public CompletedTaskAdapter(Context context, List<Task> tasks, TaskRepository repository, OnTaskStatusChangeListener listener) {
        super(context, 0, tasks);
        this.context = context;
        this.tasks = tasks;
        this.repository = repository;
        this.listener = listener;
    }
    // --------------------------------------------------

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_completed_task, parent, false);
        }

        Task task = getItem(position);
        if (task == null) return convertView;

        TextView tvTitle = convertView.findViewById(R.id.tv_task_title);
        TextView tvDescription = convertView.findViewById(R.id.tv_task_description);
        TextView tvTime = convertView.findViewById(R.id.tv_task_time);
        TextView tvCategory = convertView.findViewById(R.id.tv_task_category);
        View priorityBar = convertView.findViewById(R.id.priority_bar);
        CheckBox chkCompleted = convertView.findViewById(R.id.chk_completed);

        tvTitle.setText(task.getTitle());
        tvDescription.setText(task.getDescription());
        tvCategory.setText(task.getCategory());

        if (task.isAllDay()) {
            tvTime.setText("All Day");
        } else {
            SimpleDateFormat fullSdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

            String startDisplay = "";
            String endDisplay = "";

            if (task.getStartTime() != null) {
                // Hiển thị cả ngày và giờ cho thời gian bắt đầu
                startDisplay = fullSdf.format(new Date(task.getStartTime()));
            }

            if (task.getEndTime() != null) {
                Date startDate = new Date(task.getStartTime() != null ? task.getStartTime() : 0);
                Date endDate = new Date(task.getEndTime());

                // Kiểm tra xem ngày bắt đầu và ngày kết thúc có khác nhau không
                boolean isSameDay = task.getStartDay() != null && task.getStartDay().equals(task.getEndDay());

                if (isSameDay) {
                    // Nếu cùng ngày, chỉ hiển thị giờ kết thúc
                    endDisplay = " - " + timeSdf.format(endDate);
                } else {
                    // Nếu khác ngày, hiển thị cả ngày và giờ kết thúc
                    endDisplay = " - " + fullSdf.format(endDate);
                }
            }

            tvTime.setText(startDisplay + endDisplay);
        }

        int colorId;
        switch (task.getPriority()) {
            case 3: colorId = R.color.priority_high; break;
            case 2: colorId = R.color.priority_medium; break;
            default: colorId = R.color.priority_low; break;
        }
        priorityBar.setBackgroundColor(ContextCompat.getColor(context, colorId));

        boolean isCompleted = task.isCompleted();

        if (isCompleted) {
            // Task đã hoàn thành: Làm mờ và gạch ngang
            convertView.setAlpha(0.6f); // Thiết lập độ mờ 60%
        } else {
            // Task chưa hoàn thành: Hiển thị bình thường
            convertView.setAlpha(1.0f);
        }

        chkCompleted.setOnCheckedChangeListener(null);
        chkCompleted.setChecked(task.isCompleted());

        // --- LOGIC CẬP NHẬT TRẠNG THÁI VÀ GỌI CALLBACK ---
        chkCompleted.setOnClickListener(v -> {
            boolean isNowChecked = chkCompleted.isChecked();

            new AlertDialog.Builder(context)
                    .setTitle("Confirm The Task")
                    .setMessage(isNowChecked ? "Mark Completed?" : "Mark Uncompleted?")
                    .setPositiveButton("OK", (dialog, which) -> {
                        task.setCompleted(isNowChecked);
                        repository.update(task);

                        // Kích hoạt callback để thông báo cho MainActivity
                        if (listener != null) {
                            listener.onStatusChanged();
                        }
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        // Quay lại trạng thái ban đầu khi hủy
                        chkCompleted.setChecked(task.isCompleted());
                        notifyDataSetChanged();
                    })
                    .show();
        });

        return convertView;
    }
}
package com.example.todolist.activities;

import android.os.Bundle;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.todolist.R;
import com.example.todolist.adapter.CalendarAdapter;
import com.example.todolist.repository.CalendarRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CalendarActivity extends AppCompatActivity {

    private ImageView btnBack, btnPrevMonth, btnNextMonth;
    private TextView tvCurrentMonth;
    private GridView gridViewCalendar;

    private CalendarRepository calendarRepository;
    private Calendar currentCalendar;
    private CalendarAdapter calendarAdapter;

    private static final SimpleDateFormat MONTH_FORMAT = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        calendarRepository = new CalendarRepository(this);
        currentCalendar = Calendar.getInstance();

        initViews();
        setupEvents();
        loadCalendarData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        btnPrevMonth = findViewById(R.id.btn_prev_month);
        btnNextMonth = findViewById(R.id.btn_next_month);
        tvCurrentMonth = findViewById(R.id.tv_current_month);
        gridViewCalendar = findViewById(R.id.gridViewCalendar);
    }

    private void setupEvents() {
        btnBack.setOnClickListener(v -> finish());

        btnPrevMonth.setOnClickListener(v -> navigateMonth(-1));
        btnNextMonth.setOnClickListener(v -> navigateMonth(1));
    }

    private void navigateMonth(int direction) {
        currentCalendar.add(Calendar.MONTH, direction);
        loadCalendarData();
    }

    private void loadCalendarData() {
        // 1. Cập nhật tiêu đề tháng
        tvCurrentMonth.setText(MONTH_FORMAT.format(currentCalendar.getTime()));

        // 2. Chuẩn bị phạm vi ngày cho Adapter
        List<Date> days = getCalendarDays(currentCalendar);

        // 3. Chuẩn bị phạm vi tháng
        Calendar monthStart = (Calendar) currentCalendar.clone();
        monthStart.set(Calendar.DAY_OF_MONTH, 1);
        monthStart.set(Calendar.HOUR_OF_DAY, 0);
        monthStart.set(Calendar.MINUTE, 0);
        monthStart.set(Calendar.SECOND, 0);

        Calendar monthEnd = (Calendar) currentCalendar.clone();
        monthEnd.set(Calendar.DAY_OF_MONTH, monthEnd.getActualMaximum(Calendar.DAY_OF_MONTH));
        monthEnd.set(Calendar.HOUR_OF_DAY, 23);
        monthEnd.set(Calendar.MINUTE, 59);
        monthEnd.set(Calendar.SECOND, 59);

        // 4. Lấy dữ liệu Task từ DAO
        calendarRepository.open();
        Set<String> daysWithTasks = calendarRepository.getTaskDaysInMonth(monthStart, monthEnd);
        calendarRepository.close();

        // 5. Khởi tạo/cập nhật Adapter
        if (calendarAdapter == null) {
            calendarAdapter = new CalendarAdapter(this, days, currentCalendar, daysWithTasks);
            gridViewCalendar.setAdapter(calendarAdapter);
        } else {
            calendarAdapter.refreshCalendar(days, currentCalendar, daysWithTasks);
        }
    }

    private List<Date> getCalendarDays(Calendar month) {
        List<Date> days = new ArrayList<>();
        Calendar calendar = (Calendar) month.clone();

        // Thiết lập về ngày 1 của tháng
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        // Lùi lại để bắt đầu từ Thứ Hai đầu tiên
        // calendar.get(Calendar.DAY_OF_WEEK) trả về 1 (Sunday) đến 7 (Saturday)
        // Chúng ta cần bắt đầu từ Thứ Hai (2)
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int offset = (dayOfWeek == Calendar.SUNDAY) ? 6 : (dayOfWeek - Calendar.MONDAY);
        calendar.add(Calendar.DAY_OF_MONTH, -offset);

        int MAX_DAYS = 42;
        for (int i = 0; i < MAX_DAYS; i++) {
            days.add(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return days;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCalendarData();
    }

    @Override
    protected void onDestroy() {
        calendarRepository.close();
        super.onDestroy();
    }
}
package com.example.todolist.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.todolist.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CalendarAdapter extends BaseAdapter {

    private final Context context;
    private final List<Date> daysOfMonth;
    private final Calendar currentMonthCalendar;
    private final Set<String> daysWithTasks; // Set các ngày có Task (đã query từ DAO)
    private final Calendar today = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public CalendarAdapter(Context context, List<Date> daysOfMonth, Calendar currentMonthCalendar, Set<String> daysWithTasks) {
        this.context = context;
        this.daysOfMonth = daysOfMonth;
        this.currentMonthCalendar = currentMonthCalendar;
        this.daysWithTasks = daysWithTasks;
    }

    @Override
    public int getCount() {
        return daysOfMonth.size();
    }

    @Override
    public Object getItem(int position) {
        return daysOfMonth.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_day, parent, false);
        }

        TextView tvDayNumber = convertView.findViewById(R.id.tv_day_number);
        View currentDayBg = convertView.findViewById(R.id.view_current_day_bg); // Hình tròn màu xanh
        View taskIndicator = convertView.findViewById(R.id.view_task_indicator); // Chấm nhỏ màu đỏ

        Date day = daysOfMonth.get(position);
        Calendar dayCalendar = Calendar.getInstance();
        dayCalendar.setTime(day);

        // 1. Hiển thị Số Ngày
        tvDayNumber.setText(String.valueOf(dayCalendar.get(Calendar.DAY_OF_MONTH)));

        // Lấy tháng của ngày hiện tại để so sánh
        int dayMonth = dayCalendar.get(Calendar.MONTH);
        int currentDisplayMonth = currentMonthCalendar.get(Calendar.MONTH);

        // a. Làm mờ ngày không thuộc tháng đang hiển thị
        if (dayMonth == currentDisplayMonth) {
            tvDayNumber.setTextColor(Color.BLACK);
            convertView.setClickable(true);
        } else {
            tvDayNumber.setTextColor(Color.GRAY); // Màu mờ
            convertView.setClickable(false);
        }

        // b. Tô hình tròn màu xanh cho Ngày Hiện Tại
        boolean isToday = dayCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                && dayCalendar.get(Calendar.MONTH) == today.get(Calendar.MONTH)
                && dayCalendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH);

        if (isToday) {
            // Đặt màu trắng cho số ngày nếu là ngày hiện tại (màu nền xanh)
            tvDayNumber.setTextColor(context.getResources().getColor(android.R.color.white));
            currentDayBg.setVisibility(View.VISIBLE);
        } else {
            // không có nền
            currentDayBg.setVisibility(View.GONE);
            // Đặt lại màu
            if (dayMonth == currentDisplayMonth) {
                tvDayNumber.setTextColor(Color.BLACK);
            }
        }

        // c. Chấm nhỏ màu đỏ nếu Ngày có Task
        String dateString = dateFormat.format(day);
        if (daysWithTasks.contains(dateString)) {
            taskIndicator.setVisibility(View.VISIBLE);
        } else {
            taskIndicator.setVisibility(View.GONE);
        }

        return convertView;
    }

    public void refreshCalendar(List<Date> newDays, Calendar newMonth, Set<String> newTasks) {
        daysOfMonth.clear();
        daysOfMonth.addAll(newDays);
        currentMonthCalendar.setTime(newMonth.getTime());
        daysWithTasks.clear();
        daysWithTasks.addAll(newTasks);
        notifyDataSetChanged();
    }
}
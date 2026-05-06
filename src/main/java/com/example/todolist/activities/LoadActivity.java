package com.example.todolist.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.example.todolist.R;

public class LoadActivity extends AppCompatActivity {

    // Thời gian hiển thị màn hình chờ là 3 giây
    private static final int SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Đặt fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_load);

        new Handler().postDelayed(() -> {
            // Khởi tạo Intent để chuyển sang MainActivity
            Intent i = new Intent(LoadActivity.this, MainActivity.class);
            startActivity(i);

            // Kết thúc Activity để người dùng không thể quay lại bằng nút Back
            finish();
        }, SPLASH_TIME_OUT);
    }
}
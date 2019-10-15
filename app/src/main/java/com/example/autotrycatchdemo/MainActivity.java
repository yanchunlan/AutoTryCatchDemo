package com.example.autotrycatchdemo;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.autotrycatchdemo.autotrycatch.AutoTryCatch;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initAutoTryCatch();
    }

    private void initAutoTryCatch() {
        catchAllException();
        catchNullPointerException();
        catchNullPointerAndClassCastException();
    }

    @AutoTryCatch
    public void catchAllException() {
        int i = 1 / 0;
    }

    @AutoTryCatch(NullPointerException.class)
    public void catchNullPointerException() {
    }

    @AutoTryCatch({NullPointerException.class,ArithmeticException.class})
    public void catchNullPointerAndClassCastException() {
        List list = null;
        int size = list.size();
        size = 1 / 0;
    }
}

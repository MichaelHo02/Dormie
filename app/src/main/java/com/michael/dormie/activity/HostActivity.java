package com.michael.dormie.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.color.DynamicColors;
import com.michael.dormie.R;
import com.michael.dormie.databinding.ActivityHostBinding;

public class HostActivity extends AppCompatActivity {
    ActivityHostBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityHostBinding.inflate(getLayoutInflater());
        View view = b.getRoot();
        setContentView(view);
    }
}
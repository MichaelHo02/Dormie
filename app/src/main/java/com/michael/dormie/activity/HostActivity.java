package com.michael.dormie.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

import com.michael.dormie.MyBroadcastReceiver;
import com.michael.dormie.databinding.ActivityHostBinding;

public class HostActivity extends AppCompatActivity {
    ActivityHostBinding b;
    MyBroadcastReceiver bcr;
    IntentFilter intentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityHostBinding.inflate(getLayoutInflater());
        View view = b.getRoot();
        setContentView(view);
        registerService();
    }

    private void registerService() {
        bcr = new MyBroadcastReceiver();
        intentFilter = new IntentFilter("android.intent.action.BATTERY_LOW");
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.registerReceiver(bcr, intentFilter);
    }
}
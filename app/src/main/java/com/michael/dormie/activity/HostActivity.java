package com.michael.dormie.activity;

import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.michael.dormie.MyBroadcastReceiver;
import com.michael.dormie.databinding.ActivityHostBinding;

public class HostActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.michael.dormie.databinding.ActivityHostBinding b = ActivityHostBinding.inflate(getLayoutInflater());
        View view = b.getRoot();
        setContentView(view);
        registerService();
    }

    private void registerService() {
        MyBroadcastReceiver bcr = new MyBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter("android.intent.action.BATTERY_LOW");
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.registerReceiver(bcr, intentFilter);
    }
}
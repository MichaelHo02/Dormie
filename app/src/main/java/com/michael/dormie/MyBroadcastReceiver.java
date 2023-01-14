package com.michael.dormie;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.widget.Toast;

public class MyBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BATTERY_LOW")) {
            // Do what you want here
            Toast.makeText(context, "Low Battery", Toast.LENGTH_SHORT).show();
        } else if (intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo network = connectivityManager.getActiveNetworkInfo();
            if (network == null) {
                // Do what you want here
                Toast.makeText(context, "No Internet connection", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

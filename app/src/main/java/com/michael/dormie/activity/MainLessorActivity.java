package com.michael.dormie.activity;

import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.michael.dormie.MyBroadcastReceiver;
import com.michael.dormie.R;
import com.michael.dormie.databinding.ActivityMainLessorBinding;

public class MainLessorActivity extends AppCompatActivity {
    private ActivityMainLessorBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        b = ActivityMainLessorBinding.inflate(getLayoutInflater());
        View view = b.getRoot();
        setContentView(view);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(b.fragmentContainerView.getId());
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(b.navigationView, navController);

        b.navigationView.setCheckedItem(R.id.homeLessorFragment);
        b.navigationView.setNavigationItemSelectedListener(item -> {
            b.drawerLayout.close();
            return NavigationUI.onNavDestinationSelected(item, navController, false)
                    || super.onOptionsItemSelected(item);
        });

        registerService();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        b.navigationView.setCheckedItem(Navigation.findNavController(b.fragmentContainerView)
                .getCurrentDestination().getId());
    }

    private void registerService() {
        MyBroadcastReceiver bcr = new MyBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter("android.intent.action.BATTERY_LOW");
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.registerReceiver(bcr, intentFilter);
    }
}
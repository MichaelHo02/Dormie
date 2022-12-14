package com.michael.dormie.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.color.DynamicColors;

import com.google.android.material.navigation.NavigationView;
import com.michael.dormie.R;

public class MasterActivity extends AppCompatActivity {

    private MaterialToolbar topAppBar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master);
        DynamicColors.applyToActivitiesIfAvailable(this.getApplication());
        initUI();
        initUIAction();
    }

    private void initUI() {
        topAppBar = findViewById(R.id.activity_master_top_bar);
        drawerLayout = findViewById(R.id.activity_master_drawer_layout);
        navigationView = findViewById(R.id.activity_master_navigation);
    }

    private void initUIAction() {
        topAppBar.setNavigationOnClickListener(this::handleNavigationOnClick);
        navigationView.setNavigationItemSelectedListener(this::handleNavigationItemSelected);
        navigationView.setCheckedItem(R.id.home_page);
    }

    private void handleNavigationOnClick(View view) {
        drawerLayout.open();
    }

    private boolean handleNavigationItemSelected(MenuItem item) {
        item.setChecked(true);
        drawerLayout.close();
        switch (item.getItemId()) {
            case R.id.home_page:
                return true;
            case R.id.chat_page:
                return true;
            case R.id.rental_registration_page:
                return true;
            case R.id.profile_page:
                return true;
            case R.id.setting_page:
                return true;
        }
        return false;
    }
}
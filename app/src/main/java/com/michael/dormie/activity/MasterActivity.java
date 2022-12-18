package com.michael.dormie.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.color.DynamicColors;

import com.google.android.material.navigation.NavigationView;
import com.michael.dormie.R;
import com.michael.dormie.fragment.ChatFragment;
import com.michael.dormie.fragment.HomeFragment;
import com.michael.dormie.fragment.ProfileFragment;
import com.michael.dormie.fragment.RentalRegistrationFragment;
import com.michael.dormie.fragment.SettingFragment;
import com.michael.dormie.utils.NavigationUtil;

public class MasterActivity extends AppCompatActivity {

    private MaterialToolbar topAppBar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private final HomeFragment homeFragment = HomeFragment.newInstance("", "");
    private final ChatFragment chatFragment = ChatFragment.newInstance("", "");
    private final RentalRegistrationFragment rentalRegistrationFragment = RentalRegistrationFragment.newInstance("", "");
    private final ProfileFragment profileFragment = ProfileFragment.newInstance("", "");
    private final SettingFragment settingFragment = SettingFragment.newInstance("", "");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master);
        DynamicColors.applyToActivitiesIfAvailable(this.getApplication());
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

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
        NavigationUtil.changeFragment(this, R.id.activity_master_fl, homeFragment);
    }

    private void handleNavigationOnClick(View view) {
        drawerLayout.open();
    }

    private boolean handleNavigationItemSelected(MenuItem item) {
        item.setChecked(true);
        drawerLayout.close();
        switch (item.getItemId()) {
            case R.id.home_page:
                NavigationUtil.changeFragment(this, R.id.activity_master_fl, homeFragment);
                topAppBar.setTitle(R.string.home);
                return true;
            case R.id.chat_page:
                NavigationUtil.changeFragment(this, R.id.activity_master_fl, chatFragment);
                topAppBar.setTitle(R.string.chat);
                return true;
            case R.id.rental_registration_page:
                NavigationUtil.changeFragment(this, R.id.activity_master_fl, rentalRegistrationFragment);
                topAppBar.setTitle(R.string.rental_registration);
                return true;
            case R.id.profile_page:
                NavigationUtil.changeFragment(this, R.id.activity_master_fl, profileFragment);
                topAppBar.setTitle(R.string.profile);
                return true;
            case R.id.setting_page:
                NavigationUtil.changeFragment(this, R.id.activity_master_fl, settingFragment);
                topAppBar.setTitle(R.string.setting);
                return true;
        }
        return false;
    }
}
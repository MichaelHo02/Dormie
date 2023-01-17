package com.michael.dormie.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.michael.dormie.R;
import com.michael.dormie.fragment.ChatContactFragment;
import com.michael.dormie.fragment.HomeFragment;
import com.michael.dormie.fragment.ProfileFragment;
import com.michael.dormie.fragment.RentalRegistrationFragment;
import com.michael.dormie.fragment.SettingFragment;
import com.michael.dormie.utils.NavigationUtil;

public class MasterActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private final HomeFragment homeFragment = HomeFragment.newInstance("", "");
    private final ChatContactFragment chatContactFragment = ChatContactFragment.newInstance("", "");
    private final RentalRegistrationFragment rentalRegistrationFragment = RentalRegistrationFragment.newInstance("", "");
    private final ProfileFragment profileFragment = ProfileFragment.newInstance("", "");
    private final SettingFragment settingFragment = SettingFragment.newInstance("", "");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        initUI();
        initUIAction();
    }

    private void initUI() {
        drawerLayout = findViewById(R.id.activity_master_drawer_layout);
        navigationView = findViewById(R.id.activity_master_navigation);
    }

    private void initUIAction() {
        navigationView.setNavigationItemSelectedListener(this::handleNavigationItemSelected);
        navigationView.setCheckedItem(R.id.home_page);
        handleUpdateTopAppBar(R.id.home_page);
    }

    private boolean handleNavigationItemSelected(MenuItem item) {
        item.setChecked(true);
        drawerLayout.close();
        return handleUpdateTopAppBar(item.getItemId());
    }

    private boolean handleUpdateTopAppBar(int id) {
        switch (id) {
            case R.id.home_page:
                NavigationUtil.changeFragment(this, R.id.activity_master_fl, homeFragment);
                return true;
            case R.id.chat_page:
                NavigationUtil.changeFragment(this, R.id.activity_master_fl, chatContactFragment);
                return true;
            case R.id.rental_registration_page:
                NavigationUtil.changeFragment(this, R.id.activity_master_fl, rentalRegistrationFragment);
                return true;
            case R.id.profile_page:
                NavigationUtil.changeFragment(this, R.id.activity_master_fl, profileFragment);
                return true;
            case R.id.setting_page:
                NavigationUtil.changeFragment(this, R.id.activity_master_fl, settingFragment);
                return true;
        }
        return false;
    }
}
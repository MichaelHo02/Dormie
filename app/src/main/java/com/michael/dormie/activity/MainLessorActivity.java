package com.michael.dormie.activity;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;
import android.view.View;

import com.michael.dormie.R;
import com.michael.dormie.databinding.ActivityMainLessorBinding;

public class MainLessorActivity extends AppCompatActivity {
    ActivityMainLessorBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        b = ActivityMainLessorBinding.inflate(getLayoutInflater());
        View view = b.getRoot();
        setContentView(view);

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(b.fragmentContainerView.getId());
        NavController navController = navHostFragment.getNavController();
        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration.Builder(R.id.homeLessorFragment, R.id.chatLessorFragment, R.id.profileLessorFragment)
                        .setOpenableLayout(b.drawerLayout)
                        .build();

        NavigationUI.setupWithNavController(b.navigationView, navController);

        b.navigationView.setCheckedItem(R.id.homeLessorFragment);
        b.navigationView.setNavigationItemSelectedListener(item -> {
            b.drawerLayout.close();
            return NavigationUI.onNavDestinationSelected(item, navController)
                    || super.onOptionsItemSelected(item);
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        b.navigationView.setCheckedItem(Navigation.findNavController(b.fragmentContainerView).getCurrentDestination().getId());
    }
}
package com.michael.dormie.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.michael.dormie.R;
import com.michael.dormie.databinding.FragmentAboutMeBinding;

public class AboutMeFragment extends Fragment {
    FragmentAboutMeBinding b;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        b = FragmentAboutMeBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        b = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        b.toolbar.setNavigationOnClickListener(v -> {
            DrawerLayout drawerLayout = v.getRootView().findViewById(R.id.drawerLayout);
            drawerLayout.open();
        });
    }
}

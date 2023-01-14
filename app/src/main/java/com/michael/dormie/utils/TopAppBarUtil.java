package com.michael.dormie.utils;

import com.google.android.material.appbar.MaterialToolbar;
import com.michael.dormie.R;

public class TopAppBarUtil {
    private final MaterialToolbar topAppBar;

    public TopAppBarUtil(MaterialToolbar topAppBar) {
        this.topAppBar = topAppBar;
    }

    private boolean updateTopBarTitle(int id) {
        switch (id) {
            case R.id.homePage:
                topAppBar.setTitle(R.string.home);
                return true;
            case R.id.chatPage:
                topAppBar.setTitle(R.string.chat);
                return true;
            case R.id.rental_registration_page:
                topAppBar.setTitle(R.string.rental_registration);
                return true;
            case R.id.profilePage:
                topAppBar.setTitle(R.string.profile);
                return true;
            case R.id.settingPage:
                topAppBar.setTitle(R.string.setting);
                return true;
        }
        return false;
    }
}

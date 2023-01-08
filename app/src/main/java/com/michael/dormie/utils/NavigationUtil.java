package com.michael.dormie.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

public class NavigationUtil {
    public static void navigateActivity(AppCompatActivity activity, Context context, Class<?> cls , int reqCode, Bundle bundle) {
        Intent intent = new Intent(context, cls);
        intent.putExtras(bundle);
        activity.startActivityForResult(intent, reqCode);
    }

    public static void navigateActivity(Fragment fragment, Context context, Class<?> cls, int reqCode) {
        Intent intent = new Intent(context, cls);
        fragment.startActivityForResult(intent, reqCode);
    }

    public static void changeFragment(FragmentActivity fragmentActivity, @IdRes int containerViewId, @NonNull Fragment fragment) {
        fragmentActivity
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(containerViewId, fragment)
                .commit();
    }

    public static void removeFragment(FragmentActivity fragmentActivity, @NonNull Fragment fragment) {
        fragmentActivity
                .getSupportFragmentManager()
                .beginTransaction()
                .remove(fragment)
                .commit();
    }
}

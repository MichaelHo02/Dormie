package com.michael.dormie.fragment_v2;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.michael.dormie.databinding.FragmentTenentFilterFormBinding;

public class TenantFilterFormFragment extends Fragment {
    private FragmentTenentFilterFormBinding b;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        b = FragmentTenentFilterFormBinding.inflate(inflater, container, false);
        return b.getRoot();
    }
}
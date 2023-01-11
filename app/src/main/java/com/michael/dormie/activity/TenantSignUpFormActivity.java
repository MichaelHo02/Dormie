package com.michael.dormie.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.michael.dormie.R;
import com.michael.dormie.model.Tenant;
import com.michael.dormie.service.SignUpFormService;
import com.michael.dormie.utils.NavigationUtil;
import com.michael.dormie.utils.SignalCode;

import java.util.ArrayList;
import java.util.List;

public class TenantSignUpFormActivity extends AppCompatActivity {
    private static final String TAG = "TenantSignUpFormActivity";

    // Firebase stuffs
    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDB;
    private FirebaseStorage storage;

    // UI stuffs
    MaterialToolbar topAppBar;
    ChipGroup houseTypes, amenities;
    TextInputLayout schoolLayout;
    MaterialButtonToggleGroup minDistance, maxDistance;
    Button continueButton;

    private boolean finish = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_sign_up_form);
        mAuth = FirebaseAuth.getInstance();
        mDB = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        initVariables();
        setActions();
    }

    private void initVariables() {
        topAppBar = findViewById(R.id.topAppBar);
        houseTypes = findViewById(R.id.houseTypes);
        amenities = findViewById(R.id.amenities);
        schoolLayout = findViewById(R.id.schoolLayout);
        minDistance = findViewById(R.id.minDistance);
        maxDistance = findViewById(R.id.maxDistance);
        continueButton = findViewById(R.id.continueButton);
    }

    private void setActions() {
        topAppBar.setNavigationOnClickListener(v -> finish());
        continueButton.setOnClickListener(this::handleContinueButton);
    }

    private void handleContinueButton(View v) {
        List<String> _houseTypes = new ArrayList<>();
        List<String> _amenities = new ArrayList<>();
        String _school = null;
        int _minDistance = 100; // meter
        int _maxDistance = 20000;

        List<Integer> houseChips = houseTypes.getCheckedChipIds();
        List<Integer> amenitiesChips = amenities.getCheckedChipIds();

        if (houseChips.size() == 0) return;
        if (amenitiesChips.size() == 0) return;

        // Find list of house types
        for (int i : houseChips) {
            Chip chip = findViewById(i);
            _houseTypes.add(chip.getText().toString());
        }

        // Find list of amenities
        for (int i : amenitiesChips) {
            Chip chip = findViewById(i);
            _amenities.add(chip.getText().toString());
        }

        int minDistanceId = minDistance.getCheckedButtonId();
        int maxDistanceId = maxDistance.getCheckedButtonId();
        if (minDistanceId == R.id.one) {
            _minDistance = 100;
        } else if (minDistanceId == R.id.two) {
            _minDistance = 200;
        } else if (minDistanceId == R.id.three) {
            _minDistance = 500;
        } else if (minDistanceId == R.id.four) {
            _minDistance = 1000;
        } else return;
        if (maxDistanceId == R.id.five) {
            _maxDistance = 1000;
        } else if (maxDistanceId == R.id.six) {
            _maxDistance = 5000;
        } else if (maxDistanceId == R.id.seven) {
            _maxDistance = 10000;
        } else if (maxDistanceId == R.id.eight) {
            _maxDistance = 20000;
        } else return;

        SubmitResultReceiver receiver = new SubmitResultReceiver(new Handler());
        Tenant tenant = new Tenant(_houseTypes, _amenities, _school, _minDistance, _maxDistance);
        SignUpFormService.startActionUpdateTenant(this, receiver, tenant);
    }

    private class SubmitResultReceiver extends ResultReceiver {
        public SubmitResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            Log.e(TAG, String.valueOf(resultCode));

            if (resultCode == SignalCode.UPDATE_TENANT_SUCCESS) {
                finish = true;
            }

            if (finish) {
                finish();
                NavigationUtil.navigateActivity(
                        TenantSignUpFormActivity.this,
                        TenantSignUpFormActivity.this.getBaseContext(),
                        MasterActivity.class,
                        SignalCode.NAVIGATE_HOME);
                return;
            }

            super.onReceiveResult(resultCode, resultData);
        }
    }
}
package com.michael.dormie.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;

import com.michael.dormie.R;

public class TenantSignUpForm extends AppCompatActivity {
    Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_sign_up_form);
        initVariables();
    }

    private void initVariables() {
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
    }
}
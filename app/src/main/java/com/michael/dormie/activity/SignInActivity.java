package com.michael.dormie.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.michael.dormie.R;
import com.michael.dormie.utils.NavigationUtil;
import com.michael.dormie.utils.RequestSignal;

public class SignInActivity extends AppCompatActivity {
    TextInputLayout username, password;
    MaterialButton signInButton;
    MaterialButton signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        initVariables();
    }

    private void initVariables() {
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        signInButton = findViewById(R.id.signInButton);
        signUpButton = findViewById(R.id.signUpButton);

        signInButton.setOnClickListener(view -> {
            NavigationUtil.navigateActivity(
                    this,
                    SignInActivity.this,
                    MasterActivity.class,
                    RequestSignal.TEMPLATE_FORMAT
            );
            finish();
        });

        signUpButton.setOnClickListener(view -> NavigationUtil.navigateActivity(
                this,
                SignInActivity.this,
                SignUpActivity.class,
                RequestSignal.TEMPLATE_FORMAT
        ));
    }
}
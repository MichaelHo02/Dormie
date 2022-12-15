package com.michael.dormie.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;
import com.michael.dormie.R;

public class SignInActivity extends AppCompatActivity {
    TextInputLayout username, password;
    Button signInButton;
    TextView signUpButton;

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

        signUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }
}
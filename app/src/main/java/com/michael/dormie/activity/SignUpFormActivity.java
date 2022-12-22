package com.michael.dormie.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ToggleButton;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.michael.dormie.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Objects;

public class SignUpFormActivity extends AppCompatActivity {
    Button continueButton, takePhotoButton, uploadPhotoButton;
    MaterialButtonToggleGroup roleButton;
    TextInputLayout fullNameLayout, dobLayout;
    TextInputEditText fullName, dob;
    private MaterialDatePicker materialDatePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_form);
        initVariables();
        initCalendar();
        assignFunctions();
    }

    private void initVariables() {
        takePhotoButton = findViewById(R.id.takePhotoButton);
        uploadPhotoButton = findViewById(R.id.uploadPhotoButton);
        roleButton = findViewById(R.id.roleButton);
        fullNameLayout = findViewById(R.id.fullNameLayout);
        dobLayout = findViewById(R.id.dobLayout);
        continueButton = findViewById(R.id.continueButton);
        fullName = findViewById(R.id.fullName);
        dob = findViewById(R.id.dob);
    }

    private void assignFunctions() {
        continueButton.setOnClickListener(this::continueButtonOnClick);

        // Calendar stuffs
        dobLayout.setEndIconOnClickListener(view -> materialDatePicker.show(getSupportFragmentManager(), "DATE_PICKER"));
        dob.setOnClickListener(view -> materialDatePicker.show(getSupportFragmentManager(), "DATE_PICKER"));
        materialDatePicker.addOnPositiveButtonClickListener(selection -> dob.setText(materialDatePicker.getHeaderText()));
    }

    private void continueButtonOnClick(View view) {
        Intent intent = new Intent(SignUpFormActivity.this, TenantSignUpForm.class);
        startActivity(intent);
    }

    private void initCalendar() {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Select the birthday");
        long today;
        if (dob.getText() == null || dob.getText().toString().isEmpty()) {
            today = MaterialDatePicker.todayInUtcMilliseconds();
        } else {
            String s = dob.getText().toString();
            SimpleDateFormat sdf = new SimpleDateFormat(s.length() == 12 ? "MMM dd, yyyy" : "MMM d, yyyy");
            try {
                today = Objects.requireNonNull(sdf.parse(s)).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
                today = MaterialDatePicker.todayInUtcMilliseconds();
            }
        }
        builder.setSelection(today);
        CalendarConstraints.Builder constraint = new CalendarConstraints.Builder();
        constraint.setValidator(DateValidatorPointBackward.now());
        builder.setCalendarConstraints(constraint.build());
        materialDatePicker = builder.build();
    }
}
package com.michael.dormie.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.michael.dormie.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Objects;

public class SignUpFormActivity extends AppCompatActivity {
    private static final String TAG = "SignUpFormActivity";
    private static final String DATE_PICKER_TAG = "DATE_PICKER";
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDB;
    private FirebaseUser user;

    private MaterialButton savedButton, takePhotoButton, uploadPhotoButton;
    private MaterialButtonToggleGroup roleButton;
    private TextInputLayout nameLayout, dobLayout;
    private MaterialDatePicker materialDatePicker;
    private TextInputEditText name, dob;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_form);
        mAuth = FirebaseAuth.getInstance();
        mDB = FirebaseFirestore.getInstance();
        initVariables();
        initCalendar();
        setActions();
    }

    private void initVariables() {
        takePhotoButton = findViewById(R.id.takePhotoButton);
        uploadPhotoButton = findViewById(R.id.uploadPhotoButton);
        roleButton = findViewById(R.id.role_btn_group);
        nameLayout = findViewById(R.id.full_name_layout);
        dobLayout = findViewById(R.id.dob_layout);
        name = findViewById(R.id.full_name);
        dob = findViewById(R.id.dob);
        savedButton = findViewById(R.id.continue_btn);
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

    private void setActions() {
        savedButton.setOnClickListener(this::handleSavedButton);
        dobLayout.setEndIconOnClickListener(
                view -> materialDatePicker.show(getSupportFragmentManager(), DATE_PICKER_TAG));
        dob.setOnClickListener(
                view -> materialDatePicker.show(getSupportFragmentManager(), DATE_PICKER_TAG));
        materialDatePicker.addOnPositiveButtonClickListener(
                selection -> dob.setText(materialDatePicker.getHeaderText()));
    }

    private void handleSavedButton(View view) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            return;
        }

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName("Jane Q. User")
                .setPhotoUri(Uri.parse("https://example.com/jane-q-user/profile.jpg"))
                .build();

        user.updateProfile(profileUpdates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "User profile updated");
                    }
                });


        Intent intent = new Intent(SignUpFormActivity.this, TenantSignUpForm.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        user = mAuth.getCurrentUser();
        if (user == null) {
            Log.w(TAG, "There is no current user");
            return;
        }
    }
}
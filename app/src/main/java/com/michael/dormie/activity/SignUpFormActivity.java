package com.michael.dormie.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

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
import com.google.firebase.storage.FirebaseStorage;
import com.michael.dormie.R;
import com.michael.dormie.service.DownloadService;
import com.michael.dormie.utils.DataConverter;
import com.michael.dormie.utils.RequestSignal;
import com.michael.dormie.utils.TextInputUtil;
import com.michael.dormie.utils.TextValidator;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Objects;

public class SignUpFormActivity extends AppCompatActivity {
    private static final String TAG = "SignUpFormActivity";
    private static final String DATE_PICKER_TAG = "DATE_PICKER";

    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDB;
    private FirebaseStorage storage;

    private ImageView avatar;
    private Bitmap bitmap;
    private MaterialButton savedButton, takePhotoButton, uploadPhotoButton;
    private MaterialButtonToggleGroup roleButton;
    private TextInputLayout nameLayout, dobLayout;
    private MaterialDatePicker materialDatePicker;
    private TextInputEditText name, dob;

    private SampleResultReceiver resultReceiver = new SampleResultReceiver(new Handler());

    private class SampleResultReceiver extends ResultReceiver {
        public SampleResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            switch (resultCode) {
                case RequestSignal.DOWNLOAD_SUCCESS:
                    Toast.makeText(SignUpFormActivity.this, "Download Avatar Complete", Toast.LENGTH_SHORT).show();
                    avatar.setImageBitmap(DataConverter.convertByteArrToBitmap(resultData.getByteArray(DownloadService.DATA)));
                    break;
                case RequestSignal.DOWNLOAD_ERROR:
                    Toast.makeText(SignUpFormActivity.this, "Error downloading avatar", Toast.LENGTH_SHORT).show();
                    break;
            }
            super.onReceiveResult(resultCode, resultData);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_form);
        mAuth = FirebaseAuth.getInstance();
        mDB = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        initVariables();
        initCalendar();
        setActions();
    }

    private void initVariables() {
        avatar = findViewById(R.id.avatar);
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
                Toast.makeText(this, "Something went wrong please try again!", Toast.LENGTH_SHORT).show();
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
        name.addTextChangedListener(new TextValidator(nameLayout) {
            @Override
            public void validate(TextInputLayout textInputLayout, String text) {
                TextInputUtil.validateName(textInputLayout, text);
            }
        });

        takePhotoButton.setOnClickListener(this::handleTakePhoto);
        uploadPhotoButton.setOnClickListener(this::handleUploadPhoto);
        savedButton.setOnClickListener(this::handleSavedButton);
        dobLayout.setEndIconOnClickListener(this::handleOpenDatePicker);
        dob.setOnClickListener(this::handleOpenDatePicker);
        materialDatePicker.addOnPositiveButtonClickListener(
                selection -> dob.setText(materialDatePicker.getHeaderText()));
    }

    private void handleUploadPhoto(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, RequestSignal.ITEM_CREATION_UPLOAD_PHOTO);
    }

    private void handleTakePhoto(View view) {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, RequestSignal.ITEM_CREATION_PERMISSION_CAM);
        } else {
            Intent camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(camIntent, RequestSignal.ITEM_CREATION_TAKE_PHOTO);
        }
    }

    private void handleOpenDatePicker(View view) {
        materialDatePicker.show(getSupportFragmentManager(), DATE_PICKER_TAG);
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

        if (user.getDisplayName() != null) {
            name.setText(user.getDisplayName());
        }
        if (user.getPhotoUrl() != null) {
            resultReceiver = new SampleResultReceiver(new Handler());
            DownloadService.startActionFetchAvtByURL(
                    SignUpFormActivity.this,
                    user.getPhotoUrl().toString().replace("s96", "s560"),
                    resultReceiver
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RequestSignal.ITEM_CREATION_PERMISSION_CAM) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show();
                Intent camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(camIntent, RequestSignal.ITEM_CREATION_TAKE_PHOTO);
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;
        if (requestCode == RequestSignal.ITEM_CREATION_TAKE_PHOTO) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            bitmap = photo;
            avatar.setImageBitmap(photo);
        } else if (requestCode == RequestSignal.ITEM_CREATION_UPLOAD_PHOTO) {
            Uri selectedImg = data.getData();
            avatar.setImageURI(selectedImg);
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
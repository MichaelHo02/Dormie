package com.michael.dormie.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.michael.dormie.R;
import com.michael.dormie.service.DownloadService;
import com.michael.dormie.service.SignUpFormService;
import com.michael.dormie.utils.DataConverter;
import com.michael.dormie.utils.NavigationUtil;
import com.michael.dormie.utils.SignalCode;
import com.michael.dormie.utils.ValidationUtil;
import com.michael.dormie.utils.TextValidator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
    private MaterialTextView roleErrorText;
    private TextInputLayout nameLayout, dobLayout;
    private MaterialDatePicker materialDatePicker;
    private TextInputEditText name, dob;
    private String accountType = null;

    private boolean isCompleteUpdateAccount = false;
    private boolean isCompleteUpdateUser = false;

    private AvatarResultReceiver resultReceiver = new AvatarResultReceiver(new Handler());

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
        roleErrorText = findViewById(R.id.role_error_text);
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
                ValidationUtil.validateBasic(textInputLayout, text);
            }
        });

        takePhotoButton.setOnClickListener(this::handleTakePhoto);
        uploadPhotoButton.setOnClickListener(this::handleUploadPhoto);
        savedButton.setOnClickListener(this::handleSavedButton);
        dobLayout.setEndIconOnClickListener(this::handleOpenDatePicker);
        dob.setOnClickListener(this::handleOpenDatePicker);
        materialDatePicker.addOnPositiveButtonClickListener(selection -> {
            dob.setText(materialDatePicker.getHeaderText());
            ValidationUtil.validateBasic(dobLayout, dob.getText().toString());
        });
        roleButton.addOnButtonCheckedListener(this::handleGroupButtonRoleChecked);
    }

    private void handleGroupButtonRoleChecked(MaterialButtonToggleGroup materialButtonToggleGroup, int i, boolean b) {
        Log.d(TAG, "Button group click " + materialButtonToggleGroup.toString() + " " + i + " " + b);
        roleErrorText.setText(null);
        if (i == R.id.lessor_btn) {
            accountType = "lessor";
            Log.d(TAG, "The account type value: " + accountType);
            return;
        }
        if (i == R.id.tenant_btn) {
            accountType = "tenant";
            Log.d(TAG, "The account type value: " + accountType);
            return;
        }
    }

    private void handleUploadPhoto(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, SignalCode.ITEM_CREATION_UPLOAD_PHOTO);
    }

    private void handleTakePhoto(View view) {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, SignalCode.ITEM_CREATION_PERMISSION_CAM);
        } else {
            Intent camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(camIntent, SignalCode.ITEM_CREATION_TAKE_PHOTO);
        }
    }

    private void handleOpenDatePicker(View view) {
        materialDatePicker.show(getSupportFragmentManager(), DATE_PICKER_TAG);
    }

    private void handleSavedButton(View view) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String nameStr = null;
        byte[] bytes = null;

        ValidationUtil.validateBasic(nameLayout, name.getText().toString());
        ValidationUtil.validateBasic(dobLayout, dob.getText().toString());

        if (roleButton.getCheckedButtonId() == View.NO_ID) {
            roleErrorText.setText("Please select the account role");
        } else {
            roleErrorText.setText(null);
        }

        if (nameLayout.getError() != null || dob.getText().toString().isEmpty() || accountType == null) {
            Log.e(TAG, String.valueOf(nameLayout.getError() != null));
            Log.e(TAG, String.valueOf(bitmap == null));
            Log.e(TAG, dob.getText().toString());
            Log.e(TAG, String.valueOf(accountType == null));
            Log.i(TAG, "Input is not passed validation");
            return;
        }

        if (name.getText() != null) {
            nameStr = name.getText().toString();
        }
        if (bitmap != null) {
            bytes = DataConverter.convertImageToByteArr(bitmap);
        }
        SubmitResultReceiver receiver = new SubmitResultReceiver(new Handler());
        SignUpFormService.startActionUpdateAccount(this, receiver, nameStr, bytes);

        String dob = this.dob.getText().toString();
        SignUpFormService.startActionUpdateUser(this, receiver, accountType, dob);
    }

    @Override
    protected void onStart() {
        super.onStart();
        user = mAuth.getCurrentUser();
        if (user == null) {
            Log.w(TAG, "There is no current user");
            return;
        }

        Log.e(TAG, String.valueOf(name.getText().toString().isEmpty()));
        if (user.getDisplayName() != null && name.getText().toString().isEmpty()) {
            name.setText(user.getDisplayName());
        }
        if (user.getPhotoUrl() != null && avatar.getDrawable() == null) {
            resultReceiver = new AvatarResultReceiver(new Handler());
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
        if (requestCode == SignalCode.ITEM_CREATION_PERMISSION_CAM) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show();
                Intent camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(camIntent, SignalCode.ITEM_CREATION_TAKE_PHOTO);
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;
        if (requestCode == SignalCode.ITEM_CREATION_TAKE_PHOTO) {
            Log.d(TAG, "Put image taken from camera");
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            bitmap = photo;
            avatar.setImageBitmap(bitmap);
        } else if (requestCode == SignalCode.ITEM_CREATION_UPLOAD_PHOTO) {
            Log.d(TAG, "Put image taken from library");
            Uri selectedImg = data.getData();

            InputStream imageStream = null;
            try {
                imageStream = getContentResolver().openInputStream(selectedImg);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            bitmap = BitmapFactory.decodeStream(imageStream);
            avatar.setImageBitmap(bitmap);
        }
    }

    private class AvatarResultReceiver extends ResultReceiver {
        public AvatarResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            switch (resultCode) {
                case SignalCode.DOWNLOAD_SUCCESS:
                    Toast.makeText(SignUpFormActivity.this, "Download Avatar Complete", Toast.LENGTH_SHORT).show();
                    avatar.setImageBitmap(DataConverter.convertByteArrToBitmap(resultData.getByteArray(DownloadService.DATA)));
                    break;
                case SignalCode.DOWNLOAD_ERROR:
                    Toast.makeText(SignUpFormActivity.this, "Error downloading avatar", Toast.LENGTH_SHORT).show();
                    break;
            }
            super.onReceiveResult(resultCode, resultData);
        }
    }

    private class SubmitResultReceiver extends ResultReceiver {
        public SubmitResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            Log.e(TAG, String.valueOf(resultCode));
            if (resultCode == SignalCode.UPDATE_ACCOUNT_SUCCESS) {
                isCompleteUpdateAccount = true;
            }

            if (resultCode == SignalCode.UPDATE_USER_SUCCESS) {
                isCompleteUpdateUser = true;
            }

            if (isCompleteUpdateUser && isCompleteUpdateAccount && accountType.equals("lessor")) {
                NavigationUtil.navigateActivity(
                        SignUpFormActivity.this,
                        SignUpFormActivity.this.getBaseContext(),
                        MasterActivity.class,
                        SignalCode.NAVIGATE_HOME);
                return;
            }

            if (isCompleteUpdateUser && isCompleteUpdateAccount && accountType.equals("tenant")) {
                NavigationUtil.navigateActivity(
                        SignUpFormActivity.this,
                        SignUpFormActivity.this.getBaseContext(),
                        TenantSignUpFormActivity.class,
                        SignalCode.TEMPLATE_FORMAT);
            }
            super.onReceiveResult(resultCode, resultData);
        }
    }

}
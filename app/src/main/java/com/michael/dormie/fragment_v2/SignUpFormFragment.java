package com.michael.dormie.fragment_v2;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.progressindicator.CircularProgressIndicatorSpec;
import com.google.android.material.progressindicator.IndeterminateDrawable;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.michael.dormie.databinding.FragmentSignUpFormBinding;
import com.michael.dormie.service.SignUpFormService;
import com.michael.dormie.utils.DataConverter;
import com.michael.dormie.utils.SignalCode;
import com.michael.dormie.utils.TextValidator;
import com.michael.dormie.utils.ValidationUtil;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SignUpFormFragment extends Fragment {
    private static final String TAG = "SignUpFormFragment";
    private static final String DATE_PICKER_TAG = "DATE_PICKER";

    private FragmentSignUpFormBinding b;
    private MaterialDatePicker materialDatePicker;
    private IndeterminateDrawable loadIcon;
    private String accountType;
    private Bitmap bitmap;
    private boolean isCompleteUpdateAccount;
    private boolean isCompleteUpdateUser;
    private SubmitResultReceiver receiver;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        b = FragmentSignUpFormBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        b = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        CircularProgressIndicatorSpec spec = new CircularProgressIndicatorSpec(this.requireContext(), null, 0,
                com.google.android.material.R.style.Widget_Material3_CircularProgressIndicator);
        loadIcon = IndeterminateDrawable.createCircularDrawable(this.requireContext(), spec);
        isCompleteUpdateAccount = false;
        isCompleteUpdateUser = false;
        initCalendar();
        addListener();
    }

    private void initCalendar() {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Select the birthday");
        long today;
        if (b.dobEditText.getText() == null || b.dobEditText.getText().toString().isEmpty()) {
            today = MaterialDatePicker.todayInUtcMilliseconds();
        } else {
            String s = b.dobEditText.getText().toString();
            SimpleDateFormat sdf = new SimpleDateFormat(s.length() == 12 ? "MMM dd, yyyy" : "MMM d, yyyy");
            try {
                today = Objects.requireNonNull(sdf.parse(s)).getTime();
            } catch (ParseException e) {
                Toast.makeText(this.requireContext(), "Something went wrong please try again!",
                        Toast.LENGTH_SHORT).show();
                today = MaterialDatePicker.todayInUtcMilliseconds();
            }
        }
        builder.setSelection(today);
        CalendarConstraints.Builder constraint = new CalendarConstraints.Builder();
        constraint.setValidator(DateValidatorPointBackward.now());
        builder.setCalendarConstraints(constraint.build());
        materialDatePicker = builder.build();
    }

    private void addListener() {
        b.fullNameEditText.addTextChangedListener(new TextValidator(b.fullNameLayout) {
            @Override
            public void validate(TextInputLayout textInputLayout, String text) {
                ValidationUtil.validateBasic(textInputLayout, text);
            }
        });

        b.takePhotoBtn.setOnClickListener(this::handleTakePhoto);
        b.uploadPhotoBtn.setOnClickListener(this::handleUploadPhoto);
        b.savedBtn.setOnClickListener(this::handleSavedButton);
        b.dobLayout.setEndIconOnClickListener(this::handleOpenDatePicker);
        b.dobEditText.setOnClickListener(this::handleOpenDatePicker);
        materialDatePicker.addOnPositiveButtonClickListener(selection -> {
            b.dobEditText.setText(materialDatePicker.getHeaderText());
            ValidationUtil.validateBasic(b.dobLayout, b.dobEditText.getText().toString());
        });
        b.roleBtnGroup.addOnButtonCheckedListener(this::handleGroupButtonRoleChecked);
    }

    private void handleTakePhoto(View view) {
        if (this.requireContext().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, SignalCode.ITEM_CREATION_PERMISSION_CAM);
            return;
        }
        Intent camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camIntent, SignalCode.ITEM_CREATION_TAKE_PHOTO);
    }

    private void handleUploadPhoto(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SignalCode.ITEM_CREATION_UPLOAD_PHOTO);
    }

    private void handleSavedButton(View view) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String nameStr = null;
        byte[] bytes = null;

        ValidationUtil.validateBasic(b.fullNameLayout, b.fullNameEditText.getText().toString());
        ValidationUtil.validateBasic(b.dobLayout, b.dobEditText.getText().toString());

        b.roleErrorTextView.setText(null);
        if (b.roleBtnGroup.getCheckedButtonId() == View.NO_ID)
            b.roleErrorTextView.setText("Please select the account role");
        if (b.fullNameLayout.getError() != null || b.dobEditText.getText().toString().isEmpty() || accountType == null) {
            Log.i(TAG, "Input is not passed validation");
            return;
        }
        if (b.fullNameEditText.getText() != null)
            nameStr = b.fullNameEditText.getText().toString();
        if (bitmap != null)
            bytes = DataConverter.convertImageToByteArr(bitmap);

        b.savedBtn.setIcon(loadIcon);
        loadingProcess();
        receiver = new SubmitResultReceiver(new Handler());
        SignUpFormService.startActionUpdateAccount(this.requireContext(), receiver, nameStr, bytes);
    }

    private void handleOpenDatePicker(View view) {
        materialDatePicker.show(this.requireActivity().getSupportFragmentManager(), DATE_PICKER_TAG);
    }

    private void handleGroupButtonRoleChecked(MaterialButtonToggleGroup materialButtonToggleGroup, int i, boolean b) {
        this.b.roleErrorTextView.setText(null);
        if (i == this.b.lessorBtn.getId()) {
            accountType = "lessor";
            Log.d(TAG, "The account type value: " + accountType);
            return;
        }
        if (i == this.b.tenantBtn.getId()) {
            accountType = "tenant";
            Log.d(TAG, "The account type value: " + accountType);
            return;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Log.w(TAG, "There is no current user");
            return;
        }

        if (user.getDisplayName() != null && b.fullNameEditText.getText().toString().isEmpty())
            b.fullNameEditText.setText(user.getDisplayName());
        if (user.getPhotoUrl() != null && b.avatarView.getDrawable() == null)
            Glide.with(b.getRoot()).load(user.getPhotoUrl().toString().replace("s96", "s560")).into(b.avatarView);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SignalCode.ITEM_CREATION_PERMISSION_CAM) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this.requireContext(), "Camera permission granted", Toast.LENGTH_SHORT).show();
                Intent camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(camIntent, SignalCode.ITEM_CREATION_TAKE_PHOTO);
                return;
            }
            Snackbar.make(b.getRoot(), "Camera permission denied", Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;
        if (requestCode == SignalCode.ITEM_CREATION_TAKE_PHOTO) {
            Log.d(TAG, "Put image taken from camera");
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            bitmap = photo;
            b.avatarView.setImageBitmap(bitmap);
            return;
        }
        if (requestCode == SignalCode.ITEM_CREATION_UPLOAD_PHOTO) {
            Log.d(TAG, "Put image taken from library");
            Uri selectedImg = data.getData();
            InputStream imageStream = null;
            try {
                imageStream = this.requireActivity().getContentResolver().openInputStream(selectedImg);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            bitmap = BitmapFactory.decodeStream(imageStream);
            b.avatarView.setImageBitmap(bitmap);
        }
    }

    private class SubmitResultReceiver extends ResultReceiver {
        public SubmitResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if (resultCode == SignalCode.UPDATE_ACCOUNT_SUCCESS) {
                isCompleteUpdateAccount = true;
                String dob = b.dobEditText.getText().toString();
                SignUpFormService.startActionUpdateUser(
                        SignUpFormFragment.this.requireContext(),
                        receiver, accountType, dob);
            }
            if (resultCode == SignalCode.UPDATE_USER_SUCCESS)
                isCompleteUpdateUser = true;
            if (isCompleteUpdateUser && isCompleteUpdateAccount && accountType.equals("lessor")) {
                b.savedBtn.setIcon(null);
                completeLoadingProcess();
                Navigation.findNavController(requireView()).navigate(
                        SignUpFormFragmentDirections.actionGlobalMainLessorActivity());
                return;
            }
            if (isCompleteUpdateUser && isCompleteUpdateAccount && accountType.equals("tenant")) {
                Log.e(TAG, String.valueOf(b == null));
                b.savedBtn.setIcon(null);
                completeLoadingProcess();
                Navigation.findNavController(b.getRoot()).navigate(
                        SignUpFormFragmentDirections.actionSignUpFormFragmentToTenentFilterFormFragment());
            }
        }
    }

    private void loadingProcess() {
        b.linearProgressIndicator.setVisibility(View.VISIBLE);
        List<View> views = Arrays.asList(b.takePhotoBtn, b.uploadPhotoBtn, b.roleBtnGroup,
                b.fullNameLayout, b.dobLayout, b.savedBtn);
        for (View view : views) {
            view.setEnabled(false);
        }
    }

    private void completeLoadingProcess() {
        b.linearProgressIndicator.setVisibility(View.INVISIBLE);
        List<View> views = Arrays.asList(b.takePhotoBtn, b.uploadPhotoBtn, b.roleBtnGroup,
                b.fullNameLayout, b.dobLayout, b.savedBtn);
        for (View view : views) {
            view.setEnabled(true);
        }
    }
}
package com.michael.dormie.utils;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputLayout;

public class ValidationUtil {
    public static void validateEmailAndPassword(TextInputLayout textInputLayout, String text) {
        if (text == null) {
            textInputLayout.setError("No Input");
            return;
        }
        if (text.contains(" ")) {
            textInputLayout.setError("Cannot contain space");
            return;
        }
        if (text.trim().isEmpty()) {
            textInputLayout.setError("Cannot be empty");
            return;
        }
        textInputLayout.setError(null);
        textInputLayout.setErrorEnabled(false);
    }

    public static void validateBasic(TextInputLayout textInputLayout, String text) {
        if (text == null) {
            textInputLayout.setError("No Input");
            return;
        }
        if (text.trim().isEmpty()) {
            textInputLayout.setError("Cannot be empty");
            return;
        }
        textInputLayout.setError(null);
        textInputLayout.setErrorEnabled(false);
    }

    public static void validatePassword(@NonNull TextInputLayout password, @NonNull TextInputLayout confirmPassword) {
        String passwordStr = password.getEditText().getText().toString();
        String confirmPasswordStr = confirmPassword.getEditText().getText().toString();
        if (!passwordStr.equals(confirmPasswordStr)) {
            confirmPassword.setError("Confirm password does not matched");
        }
    }

    public static void resetValidation(@NonNull TextInputLayout textInputLayout) {
        textInputLayout.getEditText().setText("");
        textInputLayout.setError(null);
        textInputLayout.setErrorEnabled(false);
    }
}

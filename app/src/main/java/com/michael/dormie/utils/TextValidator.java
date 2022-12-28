package com.michael.dormie.utils;

import android.text.Editable;
import android.text.TextWatcher;

import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public abstract class TextValidator implements TextWatcher {
    private final TextInputLayout textInputLayout;

    public TextValidator(TextInputLayout textInputLayout) {
        this.textInputLayout = textInputLayout;
    }

    public abstract void validate(TextInputLayout textInputLayout, String text);

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

    @Override
    public void afterTextChanged(Editable editable) {
        String text = Objects.requireNonNull(textInputLayout.getEditText()).getText().toString();
        validate(textInputLayout, text);
    }
}

package com.michael.dormie.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.michael.dormie.R;
import com.michael.dormie.utils.FireBaseDBPath;
import com.michael.dormie.utils.NavigationUtil;
import com.michael.dormie.utils.RequestSignal;
import com.michael.dormie.utils.TextInputUtil;
import com.michael.dormie.utils.TextValidator;

import java.util.Optional;

public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = "SignUpActivity";
    private FirebaseAuth mAuth;
    private GoogleSignInClient gsc;
    private FirebaseFirestore mDB;

    private TextInputLayout email, password, confirmPassword;
    private MaterialButton signUpButton, signInButton, signUpButtonGoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mAuth = FirebaseAuth.getInstance();
        mDB = FirebaseFirestore.getInstance();
        googleSignUpInit();
        initVariables();
    }

    private void googleSignUpInit() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();
        gsc = GoogleSignIn.getClient(this, gso);
    }

    private void initVariables() {
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirm_password);
        signInButton = findViewById(R.id.sign_in_btn);
        signUpButton = findViewById(R.id.sign_up_btn);
        signUpButtonGoogle = findViewById(R.id.sign_up_btn_with_google);

        Optional.ofNullable(email.getEditText())
                .ifPresent(this::handleValidationEmail);
        Optional.ofNullable(password.getEditText())
                .ifPresent(this::handleValidationPassword);
        Optional.ofNullable(confirmPassword.getEditText())
                .ifPresent(this::handleValidationConfirmPassword);

        signInButton.setOnClickListener(v -> finish()); // Return to sign in activity
        signUpButton.setOnClickListener(this::signUp);
        signUpButtonGoogle.setOnClickListener(this::signUpWithGoogle);
    }

    private void handleValidationEmail(EditText editText) {
        editText.addTextChangedListener(new TextValidator(email) {
            @Override
            public void validate(TextInputLayout textInputLayout, String text) {
                TextInputUtil.basicValidation(textInputLayout, text);
            }
        });
    }

    private void handleValidationPassword(EditText editText) {
        editText.addTextChangedListener(new TextValidator(password) {
            @Override
            public void validate(TextInputLayout textInputLayout, String text) {
                TextInputUtil.basicValidation(textInputLayout, text);
            }
        });
    }

    private void handleValidationConfirmPassword(EditText editText) {
        editText.addTextChangedListener(new TextValidator(confirmPassword) {
            @Override
            public void validate(TextInputLayout textInputLayout, String text) {
                TextInputUtil.basicValidation(textInputLayout, text);
            }
        });
    }

    private void signUp(View view) {
        TextInputUtil.basicValidation(email, email.getEditText().getText().toString());
        TextInputUtil.basicValidation(password, password.getEditText().getText().toString());
        TextInputUtil.basicValidation(confirmPassword, confirmPassword.getEditText().getText().toString());
        TextInputUtil.validatePassword(password, confirmPassword);

        if (email.getError() != null || password.getError() != null || confirmPassword.getError() != null) {
            Log.i(TAG, "Input is not passed validation");
            return;
        }

        String email = this.email.getEditText().getText().toString();
        String password = this.password.getEditText().getText().toString();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(this::handleSuccessSignUpEmailPassword)
                .addOnFailureListener(this::handleFailureSignUpEmailPassword);
    }

    private void handleSuccessSignUpEmailPassword(AuthResult authResult) {
        Log.d(TAG, "createUserWithEmail:success");
        NavigationUtil.navigateActivity(
                SignUpActivity.this,
                this,
                SignUpFormActivity.class,
                RequestSignal.NAVIGATE_SIGNUP_FORM);
    }

    private void handleFailureSignUpEmailPassword(Exception e) {
        Log.e(TAG, "Fail to sign up because " + e.getLocalizedMessage());
        if (e.getLocalizedMessage().contains("email")) {
            email.setError(e.getLocalizedMessage());
            return;
        }

        if (e.getLocalizedMessage().contains("password")) {
            password.setError(e.getLocalizedMessage());
            return;
        }
    }

    private void signUpWithGoogle(View view) {
        Intent intent = gsc.getSignInIntent();
        startActivityForResult(intent, RequestSignal.SIGN_IN_WITH_GOOGLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestSignal.SIGN_IN_WITH_GOOGLE) {
            GoogleSignIn.getSignedInAccountFromIntent(data)
                    .addOnSuccessListener(this::handleSuccessSignUpGoogle)
                    .addOnFailureListener(this::handleFailureSignUpGoogle);
        }

        if (requestCode == RequestSignal.NAVIGATE_SIGNUP_FORM) {
            TextInputUtil.resetValidation(email);
            TextInputUtil.resetValidation(password);
            TextInputUtil.resetValidation(confirmPassword);
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) return;
            user.delete()
                    .addOnSuccessListener(unused -> Log.d(TAG, "User account deleted."))
                    .addOnFailureListener(e -> Log.e(TAG, "Cannot delete user because " + e.getLocalizedMessage()));
        }
    }

    private void handleSuccessSignUpGoogle(GoogleSignInAccount googleSignInAccount) {
        Log.d(TAG, "Google Account " + googleSignInAccount.getId() + " | " + googleSignInAccount.getIdToken() + " | " + googleSignInAccount.getDisplayName());
        String idToken = googleSignInAccount.getIdToken();
        if (idToken != null) {
            AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
            mAuth.signInWithCredential(firebaseCredential)
                    .addOnSuccessListener(authResult -> {
                        Log.d(TAG, "signInWithCredential: success");
                        handleNavigation(googleSignInAccount);
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "signInWithCredential: failure " + e.getLocalizedMessage());
                    });
        }
    }

    private void handleFailureSignUpGoogle(Exception e) {
        Log.e(TAG, "Fail to sign up with google because " + e.getLocalizedMessage());
        Toast.makeText(this, "Something went wrong. Please try again!", Toast.LENGTH_SHORT).show();
    }

    private void handleNavigation(GoogleSignInAccount googleSignInAccount) {
        mDB.collection(FireBaseDBPath.persons)
                .whereEqualTo("id", googleSignInAccount.getId())
                .get()
                .addOnSuccessListener(this::handleQuerySuccess)
                .addOnFailureListener(this::handleQueryFail);
    }

    private void handleQuerySuccess(QuerySnapshot queryDocumentSnapshots) {
        finish();
        if (queryDocumentSnapshots.getDocuments().isEmpty()) {
            handleNavigationOnNewUser();
            return;
        }
        handleNavigationOnExistingUser();
    }

    private void handleQueryFail(Exception e) {
        Log.e(TAG, "Fail to query user because " + e.getLocalizedMessage());
    }

    private void handleNavigationOnNewUser() {
        finish();
        NavigationUtil.navigateActivity(
                this,
                SignUpActivity.this,
                SignUpFormActivity.class,
                RequestSignal.NAVIGATE_SIGNUP_FORM
        );
    }

    private void handleNavigationOnExistingUser() {

    }
}
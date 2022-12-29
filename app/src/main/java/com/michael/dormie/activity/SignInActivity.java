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
import com.michael.dormie.utils.SignalCode;
import com.michael.dormie.utils.ValidationUtil;
import com.michael.dormie.utils.TextValidator;

import java.util.Optional;

public class SignInActivity extends AppCompatActivity {
    private static final String TAG = "SignInActivity";
    private FirebaseAuth mAuth;
    private GoogleSignInClient gsc;
    private FirebaseFirestore mDB;

    private TextInputLayout email, password;
    private MaterialButton signInButton, signInButtonGoogle, signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        mAuth = FirebaseAuth.getInstance();
        mDB = FirebaseFirestore.getInstance();
        googleSignInInit();
        initVariables();
    }

    private void googleSignInInit() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();
        gsc = GoogleSignIn.getClient(this, gso);
    }

    private void initVariables() {
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        signInButton = findViewById(R.id.sign_in_btn);
        signInButtonGoogle = findViewById(R.id.sign_in_btn_google);
        signUpButton = findViewById(R.id.sign_up_btn);

        Optional.ofNullable(email.getEditText())
                .ifPresent(this::handleValidationEmail);
        Optional.ofNullable(password.getEditText())
                .ifPresent(this::handleValidationPassword);

        signInButton.setOnClickListener(this::signIn);
        signInButtonGoogle.setOnClickListener(this::signInWithGoogle);
        signUpButton.setOnClickListener(this::signUp);
    }

    private void handleValidationEmail(EditText editText) {
        editText.addTextChangedListener(new TextValidator(email) {
            @Override
            public void validate(TextInputLayout textInputLayout, String text) {
                ValidationUtil.validateEmailAndPassword(textInputLayout, text);
            }
        });
    }

    private void handleValidationPassword(EditText editText) {
        editText.addTextChangedListener(new TextValidator(password) {
            @Override
            public void validate(TextInputLayout textInputLayout, String text) {
                ValidationUtil.validateEmailAndPassword(textInputLayout, text);
            }
        });
    }

    private void signIn(View view) {
        ValidationUtil.validateEmailAndPassword(email, email.getEditText().getText().toString());
        ValidationUtil.validateEmailAndPassword(password, password.getEditText().getText().toString());

        if (email.getError() != null || password.getError() != null) {
            Log.i(TAG, "Input is not passed validation");
            return;
        }

        String email = this.email.getEditText().getText().toString();
        String password = this.password.getEditText().getText().toString();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(this::handleSuccessSignInEmailPassword)
                .addOnFailureListener(this::handleFailureSignInEmailPassword);
    }

    private void handleSuccessSignInEmailPassword(AuthResult authResult) {
        Log.d(TAG, "signInUserWithEmail:success");
        NavigationUtil.navigateActivity(
                SignInActivity.this,
                this,
                MasterActivity.class,
                SignalCode.NAVIGATE_HOME);
    }

    private void handleFailureSignInEmailPassword(Exception e) {
        Log.e(TAG, "Fail to sign up because " + e.getLocalizedMessage());
        if (e.getLocalizedMessage().contains("email")) {
            email.setError(e.getLocalizedMessage());
            return;
        }

        if (e.getLocalizedMessage().contains("password")) {
            password.setError(e.getLocalizedMessage());
            return;
        }

        email.setError(e.getLocalizedMessage());
    }

    private void signInWithGoogle(View view) {
        Intent intent = gsc.getSignInIntent();
        startActivityForResult(intent, SignalCode.SIGN_IN_WITH_GOOGLE);
    }

    private void signUp(View view) {
        NavigationUtil.navigateActivity(
                this,
                SignInActivity.this,
                SignUpActivity.class,
                SignalCode.TEMPLATE_FORMAT
        );
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {

        } else {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SignalCode.SIGN_IN_WITH_GOOGLE) {
            GoogleSignIn.getSignedInAccountFromIntent(data)
                    .addOnSuccessListener(this::handleSuccessSignInGoogle)
                    .addOnFailureListener(this::handleFailureSignInGoogle);
        }
    }

    private void handleSuccessSignInGoogle(GoogleSignInAccount googleSignInAccount) {
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

    private void handleFailureSignInGoogle(Exception e) {
        Log.e(TAG, "Fail to sign in with google because " + e.getLocalizedMessage());
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
                SignInActivity.this,
                SignUpFormActivity.class,
                SignalCode.NAVIGATE_SIGNUP_FORM
        );
    }

    private void handleNavigationOnExistingUser() {
        NavigationUtil.navigateActivity(
                this,
                SignInActivity.this,
                MasterActivity.class,
                SignalCode.NAVIGATE_HOME
        );
    }
}
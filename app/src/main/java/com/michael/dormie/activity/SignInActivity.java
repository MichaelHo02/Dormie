package com.michael.dormie.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.michael.dormie.R;
import com.michael.dormie.utils.FireBaseDBPath;
import com.michael.dormie.utils.NavigationUtil;
import com.michael.dormie.utils.RequestSignal;

public class SignInActivity extends AppCompatActivity {
    private static final String TAG = "SignInActivity";
    private FirebaseAuth mAuth;
    private GoogleSignInClient gsc;
    private FirebaseFirestore mDB;

    TextInputLayout email, password;
    MaterialButton signInButton, signInButtonGoogle, signUpButton;

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

        signInButton.setOnClickListener(this::signIn);
        signInButtonGoogle.setOnClickListener(this::signInWithGoogle);
        signUpButton.setOnClickListener(this::signUp);
    }

    private void signIn(View view) {

    }

    private void signInWithGoogle(View view) {
        Intent intent = gsc.getSignInIntent();
        startActivityForResult(intent, RequestSignal.SIGN_IN_WITH_GOOGLE);
    }

    private void signUp(View view) {
        NavigationUtil.navigateActivity(
                this,
                SignInActivity.this,
                SignUpActivity.class,
                RequestSignal.TEMPLATE_FORMAT
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
        if (requestCode == RequestSignal.SIGN_IN_WITH_GOOGLE) {
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
                RequestSignal.NAVIGATE_SIGNUP_FORM
        );
    }

    private void handleNavigationOnExistingUser() {
        NavigationUtil.navigateActivity(
                this,
                SignInActivity.this,
                MasterActivity.class,
                RequestSignal.NAVIGATE_HOME
        );
    }
}
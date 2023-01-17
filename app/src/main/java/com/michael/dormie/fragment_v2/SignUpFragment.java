package com.michael.dormie.fragment_v2;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.progressindicator.CircularProgressIndicatorSpec;
import com.google.android.material.progressindicator.IndeterminateDrawable;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.michael.dormie.R;
import com.michael.dormie.databinding.FragmentSignUpBinding;
import com.michael.dormie.utils.FireBaseDBPath;
import com.michael.dormie.utils.SignalCode;
import com.michael.dormie.utils.TextValidator;
import com.michael.dormie.utils.ValidationUtil;

import java.util.Arrays;
import java.util.List;

public class SignUpFragment extends Fragment {
    private static final String TAG = "SignUpFragment";
    private FragmentSignUpBinding b;
    private GoogleSignInClient gsc;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDB;
    private IndeterminateDrawable loadIcon;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        b = FragmentSignUpBinding.inflate(inflater, container, false);
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

        mAuth = FirebaseAuth.getInstance();
        mDB = FirebaseFirestore.getInstance();
        googleSignUpInit();
        addListener();
    }

    private void googleSignUpInit() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();
        gsc = GoogleSignIn.getClient(this.requireContext(), gso);
    }

    private void addListener() {
        b.emailEditText.addTextChangedListener(new TextValidator(b.emailLayout) {
            @Override
            public void validate(TextInputLayout textInputLayout, String text) {
                ValidationUtil.validateEmailAndPassword(textInputLayout, text);
            }
        });

        b.passwordEditText.addTextChangedListener(new TextValidator(b.passwordLayout) {
            @Override
            public void validate(TextInputLayout textInputLayout, String text) {
                ValidationUtil.validateEmailAndPassword(textInputLayout, text);
            }
        });

        b.confirmPasswordEditText.addTextChangedListener(new TextValidator(b.confirmPasswordLayout) {
            @Override
            public void validate(TextInputLayout textInputLayout, String text) {
                ValidationUtil.validateEmailAndPassword(textInputLayout, text);
            }
        });

        b.signInBtn.setOnClickListener(this::signIn);
        b.signInBtnGoogle.setOnClickListener(this::signInWithGoogle);
        b.signUpBtn.setOnClickListener(this::signUp);
    }

    private void signIn(View view) {
        Navigation.findNavController(b.getRoot()).popBackStack();
    }

    private void signInWithGoogle(View view) {
        Intent intent = gsc.getSignInIntent();
        startActivityForResult(intent, SignalCode.SIGN_IN_WITH_GOOGLE);
    }

    private void signUp(View view) {
        ValidationUtil.validateEmailAndPassword(b.emailLayout, b.emailEditText.getText().toString());
        ValidationUtil.validateEmailAndPassword(b.passwordLayout, b.passwordEditText.getText().toString());
        ValidationUtil.validateEmailAndPassword(b.confirmPasswordLayout,
                b.confirmPasswordEditText.getText().toString());
        ValidationUtil.validatePassword(b.passwordLayout, b.confirmPasswordLayout);

        if (b.emailLayout.getError() != null || b.passwordLayout.getError() != null || b.confirmPasswordLayout.getError() != null) {
            Log.i(TAG, "Input is not passed validation");
            Snackbar.make(b.getRoot(), "Please try again!",
                    Snackbar.LENGTH_LONG).show();
            return;
        }

        b.signUpBtn.setIcon(loadIcon);
        loadingProcess();

        String email = b.emailEditText.getText().toString();
        String password = b.passwordEditText.getText().toString();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    b.signUpBtn.setIcon(null);
                    completeLoadingProcess();
                })
                .addOnSuccessListener(this::handleSuccessSignUpEmailPassword)
                .addOnFailureListener(this::handleFailureSignUpEmailPassword);
    }

    private void handleSuccessSignUpEmailPassword(AuthResult authResult) {
        Log.d(TAG, "Create user with email success");
        Navigation.findNavController(b.getRoot()).navigate(
                SignUpFragmentDirections.actionGlobalSignUpFormNavigation());
    }

    private void handleFailureSignUpEmailPassword(Exception e) {
        Log.e(TAG, "Fail to sign up because " + e.getLocalizedMessage());
        if (e.getLocalizedMessage().contains("email")) {
            b.emailLayout.setError(e.getLocalizedMessage());
            return;
        }

        if (e.getLocalizedMessage().contains("password")) {
            b.passwordLayout.setError(e.getLocalizedMessage());
            return;
        }

        b.emailLayout.setError(e.getLocalizedMessage());
    }

    @Override
    public void onStart() {
        super.onStart();
        ValidationUtil.resetValidation(b.emailLayout);
        ValidationUtil.resetValidation(b.passwordLayout);
        ValidationUtil.resetValidation(b.confirmPasswordLayout);
    }

    @Override
    public void onResume() {
        super.onResume();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        user.delete()
                .addOnSuccessListener(unused -> Log.d(TAG, "User account deleted."))
                .addOnFailureListener(e -> Log.e(TAG, "Cannot delete user because " + e.getLocalizedMessage()));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SignalCode.SIGN_IN_WITH_GOOGLE) {
            b.signInBtnGoogle.setIcon(loadIcon);
            loadingProcess();
            GoogleSignIn.getSignedInAccountFromIntent(data)
                    .addOnCompleteListener(task -> {
                        b.signInBtnGoogle.setIcon(null);
                        completeLoadingProcess();
                    })
                    .addOnSuccessListener(this::handleSuccessSignInGoogle)
                    .addOnFailureListener(this::handleFailureSignInGoogle);
        }
    }

    private void handleSuccessSignInGoogle(GoogleSignInAccount googleSignInAccount) {
        Log.d(TAG, "Successfully sign in with google");
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
                        Snackbar.make(b.getRoot(), "Something went wrong. Please try again!",
                                Snackbar.LENGTH_LONG).show();
                    });
        }
    }

    private void handleFailureSignInGoogle(Exception e) {
        Log.e(TAG, "Fail to sign up with google because " + e.getLocalizedMessage());
        Snackbar.make(b.getRoot(), "Something went wrong. Please try again!",
                Snackbar.LENGTH_LONG).show();
    }

    private void handleNavigation(GoogleSignInAccount googleSignInAccount) {
        if (googleSignInAccount.getId() == null) {
            handleQueryFail(new Exception());
            return;
        }
        mDB.collection(FireBaseDBPath.USERS)
                .document(googleSignInAccount.getId())
                .get()
                .addOnSuccessListener(this::handleQuerySuccess)
                .addOnFailureListener(this::handleQueryFail);
    }

    private void handleQuerySuccess(DocumentSnapshot documentSnapshot) {
        if (documentSnapshot.exists()) {
            handleNavigationOnNewUser();
            return;
        }
        handleNavigationOnExistingUser();
    }

    private void handleQueryFail(Exception e) {
        Log.e(TAG, "Fail to query user because " + e.getLocalizedMessage());
    }

    private void handleNavigationOnNewUser() {
        Navigation.findNavController(b.getRoot()).navigate(
                SignUpFragmentDirections.actionGlobalSignUpFormNavigation());
    }

    private void handleNavigationOnExistingUser() {
        Snackbar.make(b.getRoot(), "You already create your account!",
                Snackbar.LENGTH_LONG).show();
        Navigation.findNavController(b.getRoot()).navigate(
                SignUpFragmentDirections.actionGlobalMainLessorActivity());
    }

    private void loadingProcess() {
        b.linearProgressIndicator.setVisibility(View.VISIBLE);
        List<View> views = Arrays.asList(b.emailLayout, b.passwordLayout, b.confirmPasswordLayout,
                b.signInBtn,
                b.signInBtnGoogle, b.signUpBtn);
        for (View view : views) {
            view.setEnabled(false);
        }
    }

    private void completeLoadingProcess() {
        b.linearProgressIndicator.setVisibility(View.INVISIBLE);
        List<View> views = Arrays.asList(b.emailLayout, b.passwordLayout, b.confirmPasswordLayout,
                b.signInBtn,
                b.signInBtnGoogle, b.signUpBtn);
        for (View view : views) {
            view.setEnabled(true);
        }
    }
}
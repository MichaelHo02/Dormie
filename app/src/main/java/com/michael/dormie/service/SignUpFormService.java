package com.michael.dormie.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.ResultReceiver;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.michael.dormie.model.Tenant;
import com.michael.dormie.model.User;
import com.michael.dormie.utils.FireBaseDBPath;
import com.michael.dormie.utils.SignalCode;

public class SignUpFormService extends IntentService {
    private static final String TAG = "SignUpFormService";
    private static final String ACTION_UPDATE_ACCOUNT = "com.michael.dormie.service.action.UPDATE_ACCOUNT";
    private static final String ACTION_UPDATE_USER = "com.michael.dormie.service.action.UPDATE_USER";
    private static final String ACTION_UPDATE_TENANT = "com.michael.dormie.service.action.ACTION_UPDATE_TENANT";

    private static final String EXTRA_RECEIVER = "com.michael.dormie.service.extra.RECEIVER";
    private static final String EXTRA_NAME = "com.michael.dormie.service.extra.NAME";
    private static final String EXTRA_IMG = "com.michael.dormie.service.extra.EXTRA_IMG";
    private static final String EXTRA_ROLE = "com.michael.dormie.service.extra.EXTRA_ROLE";
    private static final String EXTRA_DOB = "com.michael.dormie.service.extra.EXTRA_DOB";
    private static final String EXTRA_TENANT = "com.michael.dormie.service.extra.TENANT";

    public static final String DATA = "data";

    public SignUpFormService() {
        super(TAG);
    }

    public static void startActionUpdateAccount(Context context, ResultReceiver param1, String param2, byte[] param3) {
        Intent intent = new Intent(context, SignUpFormService.class);
        intent.setAction(ACTION_UPDATE_ACCOUNT);
        intent.putExtra(EXTRA_RECEIVER, param1);
        intent.putExtra(EXTRA_NAME, param2);
        intent.putExtra(EXTRA_IMG, param3);
        context.startService(intent);
    }

    public static void startActionUpdateUser(Context context, ResultReceiver param1, String param2, String param3) {
        Intent intent = new Intent(context, SignUpFormService.class);
        intent.setAction(ACTION_UPDATE_USER);
        intent.putExtra(EXTRA_RECEIVER, param1);
        intent.putExtra(EXTRA_ROLE, param2);
        intent.putExtra(EXTRA_DOB, param3);
        context.startService(intent);
    }

    public static void startActionUpdateTenant(Context context, ResultReceiver param1, Tenant param2) {
        Intent intent = new Intent(context, SignUpFormService.class);
        intent.setAction(ACTION_UPDATE_TENANT);
        intent.putExtra(EXTRA_RECEIVER, param1);
        intent.putExtra(EXTRA_TENANT, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPDATE_ACCOUNT.equals(action)) {
                final ResultReceiver param1 = intent.getParcelableExtra(EXTRA_RECEIVER);
                final String param2 = intent.getStringExtra(EXTRA_NAME);
                byte[] param3 = intent.getByteArrayExtra(EXTRA_IMG);
                handleActionUpdateAccount(param1, param2, param3);
            } else if (ACTION_UPDATE_USER.equals(action)) {
                final ResultReceiver param1 = intent.getParcelableExtra(EXTRA_RECEIVER);
                final String param2 = intent.getStringExtra(EXTRA_ROLE);
                final String param3 = intent.getStringExtra(EXTRA_DOB);
                handleActionUpdateUser(param1, param2, param3);
            }
        }
    }

    private void handleActionUpdateAccount(ResultReceiver receiver, String name, byte[] bytes) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, "Cannot get user");
            receiver.send(SignalCode.UPDATE_ACCOUNT_ERROR, null);
            return;
        }

        UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder();
        if (name != null) {
            builder.setDisplayName(name);
        }

        if (bytes != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReference();
            StorageReference avtRef = storageReference.child(user.getUid() + "_avt.jpeg");
            UploadTask uploadTask = avtRef.putBytes(bytes);
            uploadTask.continueWithTask(task -> {
                        if (!task.isSuccessful()) throw task.getException();
                        return avtRef.getDownloadUrl();
                    })
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            builder.setPhotoUri(task.getResult());
                        } else {
                            Log.e(TAG, "Cannot upload the avatar to cloud");
                        }
                        user.updateProfile(builder.build())
                                .addOnSuccessListener(unused -> {
                                    receiver.send(SignalCode.UPDATE_ACCOUNT_SUCCESS, null);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Cannot update profile");
                                    receiver.send(SignalCode.UPDATE_ACCOUNT_ERROR, null);
                                });
                    });
        } else {
            user.updateProfile(builder.build())
                    .addOnSuccessListener(unused -> {
                        receiver.send(SignalCode.UPDATE_ACCOUNT_SUCCESS, null);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Cannot update profile");
                        receiver.send(SignalCode.UPDATE_ACCOUNT_ERROR, null);
                    });
        }
    }

    private void handleActionUpdateUser(ResultReceiver receiver, String role, String dob) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "Cannot get user");
            receiver.send(SignalCode.UPDATE_USER_ERROR, null);
            return;
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        User user = new User(currentUser.getUid(), currentUser.getDisplayName(),
                currentUser.getEmail(),
                currentUser.getPhotoUrl().toString(), role, dob, false, null);
        db.collection(FireBaseDBPath.USERS)
                .document(currentUser.getUid())
                .set(user, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "DocumentSnapshot added");
                    receiver.send(SignalCode.UPDATE_USER_SUCCESS, null);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error adding document", e);
                    receiver.send(SignalCode.UPDATE_USER_ERROR, null);
                });
    }
}
package com.michael.dormie.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.michael.dormie.model.Place;
import com.michael.dormie.utils.FireBaseDBPath;
import com.michael.dormie.utils.SignalCode;

public class PostCreationService extends IntentService {
    private static final String TAG = "PostCreationService";
    public static final String DATA = "data";


    private static final String ACTION_UPLOAD_IMAGE = "com.michael.dormie.service.action.UPLOAD_IMAGE";
    private static final String ACTION_UPLOAD_POST = "com.michael.dormie.service.action.BAZ";

    private static final String EXTRA_RECEIVER = "com.michael.dormie.service.extra.RECEIVER";
    private static final String EXTRA_PHOTO_ID = "com.michael.dormie.service.extra.PHOTO_ID";
    private static final String EXTRA_PHOTO = "com.michael.dormie.service.extra.PHOTO";
    private static final String EXTRA_PLACE = "com.michael.dormie.service.extra.PLACE";

    public PostCreationService() {
        super(TAG);
    }

    public static void startActionUploadImage(Context context, ResultReceiver param1, String param2, byte[] param3) {
        Intent intent = new Intent(context, PostCreationService.class);
        intent.setAction(ACTION_UPLOAD_IMAGE);
        intent.putExtra(EXTRA_RECEIVER, param1);
        intent.putExtra(EXTRA_PHOTO_ID, param2);
        intent.putExtra(EXTRA_PHOTO, param3);
        context.startService(intent);
    }

    public static void startActionUploadPost(Context context, ResultReceiver param1, Place param2) {
        Intent intent = new Intent(context, PostCreationService.class);
        intent.setAction(ACTION_UPLOAD_POST);
        intent.putExtra(EXTRA_RECEIVER, param1);
        intent.putExtra(EXTRA_PLACE, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPLOAD_IMAGE.equals(action)) {
                final ResultReceiver param1 = intent.getParcelableExtra(EXTRA_RECEIVER);
                final String param2 = intent.getStringExtra(EXTRA_PHOTO_ID);
                final byte[] param3 = intent.getByteArrayExtra(EXTRA_PHOTO);
                handleActionUploadImage(param1, param2, param3);
            } else if (ACTION_UPLOAD_POST.equals(action)) {
                final ResultReceiver param1 = intent.getParcelableExtra(EXTRA_RECEIVER);
                final Place param2 = (Place) intent.getSerializableExtra(EXTRA_PLACE);
                handleActionUploadPost(param1, param2);
            }
        }
    }

    private void handleActionUploadImage(ResultReceiver receiver, String photoID, byte[] photo) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "Cannot get user");
            return;
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();

        StorageReference postIMGs = storageReference.child(currentUser.getUid() + "_" + photoID + "_img.jpeg");
        UploadTask uploadTask = postIMGs.putBytes(photo);
        uploadTask.continueWithTask(task -> {
                    if (!task.isSuccessful()) throw task.getException();
                    return postIMGs.getDownloadUrl();
                })
                .addOnSuccessListener(uri -> {
                    Bundle bundle = new Bundle();
                    bundle.putString(DATA, uri.toString());
                    receiver.send(SignalCode.UPLOAD_IMG_SUCCESS, bundle);
                })
                .addOnFailureListener(e -> {
                    receiver.send(SignalCode.UPLOAD_IMG_ERROR, null);
                });
    }

    private void handleActionUploadPost(ResultReceiver receiver, Place place) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(FireBaseDBPath.PROPERTIES)
                .document(place.getUid())
                .set(place, SetOptions.merge())
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Document added");
                    receiver.send(SignalCode.UPLOAD_POST_SUCCESS, null);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error adding document", e);
                    receiver.send(SignalCode.UPLOAD_POST_ERROR, null);
                });
    }
}
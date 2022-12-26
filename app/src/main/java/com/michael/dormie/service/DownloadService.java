package com.michael.dormie.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.michael.dormie.utils.DataConverter;
import com.michael.dormie.utils.RequestSignal;

public class DownloadService extends IntentService {
    private static final String TAG = "DownloadService";
    private static final String ACTION_FETCH_AVT_BY_URL = "com.michael.dormie.service.action.FETCH_AVT_BY_URL";
    private static final String ACTION_BAZ = "com.michael.dormie.service.action.BAZ";

    private static final String EXTRA_URL = "com.michael.dormie.service.extra.URL";
    private static final String EXTRA_RECEIVER = "com.michael.dormie.service.extra.RECEIVER";

    public static final String DATA = "data";

    public DownloadService() {
        super("DownloadService");
    }

    public static void startActionFetchAvtByURL(Context context, String param1, ResultReceiver param2) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(ACTION_FETCH_AVT_BY_URL);
        intent.putExtra(EXTRA_URL, param1);
        intent.putExtra(EXTRA_RECEIVER, param2);
        context.startService(intent);
    }

    public static void startActionBaz(Context context, String param1, ResultReceiver param2) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_URL, param1);
        intent.putExtra(EXTRA_RECEIVER, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FETCH_AVT_BY_URL.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_URL);
                final ResultReceiver param2 = intent.getParcelableExtra(EXTRA_RECEIVER);
                handleActionFetchAvtByURL(param1, param2);
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_URL);
                final ResultReceiver param2 = intent.getParcelableExtra(EXTRA_RECEIVER);
                handleActionBaz(param1, param2);
            }
        }
    }

    private void handleActionFetchAvtByURL(String urlStr, ResultReceiver receiver) {
        Log.d(TAG, "Handle action fetch avt by url: " + urlStr);
        try {
            Bitmap bitmap = DataConverter.getImageBitmap(urlStr);
            Bundle bundle = new Bundle();
            bundle.putByteArray(DATA, DataConverter.convertImageToByteArr(bitmap));
            receiver.send(RequestSignal.DOWNLOAD_SUCCESS, bundle);
        } catch (Exception e) {
            Log.e(TAG, "Cannot fetch avt by url because " + e.getLocalizedMessage());
            receiver.send(RequestSignal.DOWNLOAD_ERROR, null);
        }
    }

    private void handleActionBaz(String urlStr, ResultReceiver receiver) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
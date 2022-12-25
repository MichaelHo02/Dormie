package com.michael.dormie.service;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import androidx.annotation.Nullable;

import com.michael.dormie.utils.DataConverter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadService extends IntentService {
    private static final String TAG = "DownloadService";
    public static final int DOWNLOAD_ERROR = 102;
    public static final int DOWNLOAD_SUCCESS = 101;

    String url;

    public DownloadService() {
        super("Hello");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        url = intent.getStringExtra("url");
        Log.e(TAG, url);
        final ResultReceiver receiver = intent.getParcelableExtra("receiver");
        try {
            Bitmap bitmap = DataConverter.getImageBitmap(url);
            Bundle bundle = new Bundle();
            bundle.putByteArray("data", DataConverter.convertImageToByteArr(bitmap));
            receiver.send(DOWNLOAD_SUCCESS, bundle);
        } catch (Exception e) {
            e.printStackTrace();
            receiver.send(DOWNLOAD_ERROR, null);
        }
    }
}
package com.michael.dormie.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class DataConverter {
    private static final String TAG = "DataConverter";

    public static byte[] convertImageToByteArr(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    public static Bitmap convertByteArrToBitmap(byte[] bytes) {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public static byte[] compressImage(byte[] imageToBeCompressed) {
        byte[] compressImage = imageToBeCompressed;
        while (compressImage.length > 500000) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(compressImage, 0, compressImage.length);
            Bitmap resized = Bitmap.createScaledBitmap(
                    bitmap,
                    (int) (bitmap.getWidth() * 0.8),
                    (int) (bitmap.getHeight() * 0.8), false
            );
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            resized.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            compressImage = stream.toByteArray();
        }
        return compressImage;
    }

    public static Bitmap getImageBitmap(String url) {
        Log.e(TAG, url);
        Bitmap bitmap = null;

        try {
            URL aURL = new URL(url);
            URLConnection conn = aURL.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            bitmap = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
        } catch (Exception e) {
            Log.e(TAG, "Error getting bitmap", e);
        }

        return bitmap;
    }
}

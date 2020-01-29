package com.optimove.sdk.optimove_sdk.main.tools;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLogger;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UiUtils {
    public static Bitmap getBitmapFromURL(String src) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(src);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();

            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }

        }
    }

}
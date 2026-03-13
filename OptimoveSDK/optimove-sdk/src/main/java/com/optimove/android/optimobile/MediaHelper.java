package com.optimove.android.optimobile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.net.MalformedURLException;
import java.net.URL;

public class MediaHelper {

    static @Nullable URL getCompletePictureUrl(@Nullable String mediaBaseUrl, @NonNull String pictureUrl, int width) throws MalformedURLException {
        if (pictureUrl.startsWith("https://") || pictureUrl.startsWith("http://")) {
            return new URL(pictureUrl);
        }

        if (mediaBaseUrl == null) {
            return null;
        }

        return new URL(mediaBaseUrl + "/" + width + "x/" + pictureUrl);
    }
}

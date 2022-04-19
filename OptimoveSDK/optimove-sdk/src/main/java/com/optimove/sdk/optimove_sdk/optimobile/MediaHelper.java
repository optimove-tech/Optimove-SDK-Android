package com.optimove.sdk.optimove_sdk.optimobile;

import androidx.annotation.NonNull;

import java.net.MalformedURLException;
import java.net.URL;

public class MediaHelper {

    static @NonNull URL getCompletePictureUrl(@NonNull String pictureUrl, int width) throws MalformedURLException{
        if (pictureUrl.substring(0, 8).equals("https://") || pictureUrl.substring(0, 7).equals("http://")){
            return new URL(pictureUrl);
        }

        return new URL(Optimobile.urlBuilder.urlForService(UrlBuilder.Service.MEDIA, "/" + width + "x/" + pictureUrl));
    }
}

package com.optimove.android.optimobile;

import android.content.Context;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.Response;

class InAppRequestService {

    private static final String TAG = InAppRequestService.class.getName();

    static List<InAppMessage> readInAppMessages(Context c, Date lastSyncTime) {
        OptimobileHttpClient httpClient = OptimobileHttpClient.getInstance();
        String userIdentifier = Optimobile.getCurrentUserIdentifier(c);

        String params = "";
        if (lastSyncTime != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            params = "?after=" + sdf.format(lastSyncTime);
        }
        String encodedIdentifier = Uri.encode(userIdentifier);
        String url = Optimobile.urlBuilder.urlForService(UrlBuilder.Service.PUSH, "/v1/users/" + encodedIdentifier + "/messages" + params);

        List<InAppMessage> messages = null;
        try (Response response = httpClient.getSync(url)) {
            if (!response.isSuccessful()) {
                logFailedResponse(response);
            } else {
                messages = getMessages(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Optimobile.PartialInitialisationException e) {
            // noop
        }

        return messages;
    }

    private static void logFailedResponse(Response response) {
        switch (response.code()) {
            case 404:
                Optimobile.log(TAG, "User not found");
                break;
            case 422:
                try {
                    Optimobile.log(TAG, response.body().string());
                } catch (NullPointerException | IOException e) {
                    Optimobile.log(TAG, e.getMessage());
                }
                break;
            default:
                Optimobile.log(TAG, response.message());
                break;
        }
    }

    private static List<InAppMessage> getMessages(Response response) {
        try {
            JSONArray result = new JSONArray(response.body().string());
            List<InAppMessage> inAppMessages = new ArrayList<>();
            int len = result.length();

            for (int i = 0; i < len; i++) {
                InAppMessage message = new InAppMessage(result.getJSONObject(i));
                inAppMessages.add(message);
            }

            return inAppMessages;
        } catch (NullPointerException | JSONException | ParseException | IOException e) {
            Optimobile.log(TAG, e.getMessage());
            return null;
        }
    }
}
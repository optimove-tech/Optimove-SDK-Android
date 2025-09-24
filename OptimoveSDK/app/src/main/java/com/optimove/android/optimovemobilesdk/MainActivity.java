package com.optimove.android.optimovemobilesdk;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.optimove.android.Optimove;
import com.optimove.android.main.events.OptimoveEvent;
import com.optimove.android.optimobile.InAppInboxItem;
import com.optimove.android.optimobile.InAppMessageInterceptor;
import com.optimove.android.optimobile.InAppMessageInterceptorCallback;
import com.optimove.android.optimobile.InAppMessageInfo;
import com.optimove.android.optimobile.OptimoveInApp;
import com.optimove.android.OptimoveConfig;
import com.optimove.android.preferencecenter.Channel;
import com.optimove.android.preferencecenter.OptimovePreferenceCenter;
import com.optimove.android.preferencecenter.PreferenceUpdate;
import com.optimove.android.preferencecenter.Preferences;
import com.optimove.android.preferencecenter.Topic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity {

    static final String TAG = "TestAppMainActvity";
    static final String PC_TAG = "OptimovePC";
    private static final int WRITE_EXTERNAL_PERMISSION_REQUEST_CODE = 169;

    private TextView outputTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        outputTv = findViewById(R.id.userIdTextView);

        this.hideIrrelevantInputs();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_PERMISSION_REQUEST_CODE);
        }

        //deferred deep links
        Optimove.getInstance().seeIntent(getIntent(), savedInstanceState);
        
        // Set up conditional in-app message display interceptor for demo
        setupInAppMessageInterceptor();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        //deferred deep links
        Optimove.getInstance().seeInputFocus(hasFocus);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
//    runFromWorker(() -> new DeepLinkHandler(intent).extractLinkData(new LinkDataExtractedListener() {
//      @Override
//      public void onDataExtracted(String screenName, Map<String, String> parameters) {
//        Toast.makeText(MainActivity.this, String.format("New Intent called with screed %s, and data: %s",
//            screenName, Arrays.deepToString(parameters.values().toArray(new String[0]))), Toast.LENGTH_SHORT).show();
//      }
//
//      @Override
//      public void onErrorOccurred(LinkDataError error) {
//        Toast.makeText(MainActivity.this, "New Intent called without deep link", Toast.LENGTH_SHORT).show();
//      }
//    }));

        //deferred deep links
        Optimove.getInstance().seeIntent(intent);
    }

    public void reportEvent(View view) {
        if (view == null)
            return;
        outputTv.setText("Reporting Custom Event for Visitor without optional value");
        runFromWorker(() -> Optimove.getInstance().reportEvent(new SimpleCustomEvent()));
        runFromWorker(() -> Optimove.getInstance().reportEvent("Event_No ParaMs     "));
    }

    public void updateUserId(View view) {
        EditText uidInput = findViewById(R.id.userIdInput);
        EditText emailInput = findViewById(R.id.userEmailInput);
        String userId = uidInput.getText().toString();
        String userEmail = emailInput.getText().toString();

        if (userEmail.isEmpty()) {
            outputTv.setText("Calling setUserId");
            Optimove.getInstance().setUserId(userId);
        } else if (userId.isEmpty()) {
            outputTv.setText("Calling setUserEmail");
            Optimove.getInstance().setUserEmail(userEmail);
        } else {
            outputTv.setText("Calling registerUser");
            Optimove.getInstance().registerUser(userId, userEmail);
        }
    }

    public void readInbox(View view) {
        List<InAppInboxItem> items = OptimoveInApp.getInstance().getInboxItems();
        if (items.size() == 0) {
            Log.d(TAG, "no inbox items!");
            return;
        }
        for (int i = 0; i < items.size(); i++) {
            InAppInboxItem item = items.get(i);
            Log.d(TAG, "title: " + item.getTitle() + ", isRead: " + item.isRead());
        }
    }

    public void markInboxAsRead(View view) {
        Log.d(TAG, "mark  all inbox read");

        OptimoveInApp.getInstance().markAllInboxItemsAsRead();
    }

    public void deleteInbox(View view) {

        List<InAppInboxItem> items = OptimoveInApp.getInstance().getInboxItems();
        if (items.size() == 0) {
            Log.d(TAG, "no inbox items!");
            return;
        }
        for (int i = 0; i < items.size(); i++) {
            OptimoveInApp.getInstance().deleteMessageFromInbox(items.get(i));
        }

    }

    // ******************** PC start *********************

    public void getPreferences(View view) {
        OptimovePreferenceCenter.getInstance().getPreferencesAsync((OptimovePreferenceCenter.ResultType result, Preferences preferences) -> {
            switch (result) {
                case ERROR_USER_NOT_SET:
                case ERROR:
                case ERROR_CREDENTIALS_NOT_SET:
                    Log.d(PC_TAG, result.toString());
                    break;
                case SUCCESS: {

                    Log.d(PC_TAG, "configured: " + preferences.getConfiguredChannels().toString());
                    List<Topic> topics = preferences.getCustomerPreferences();
                    for (int i = 0; i < topics.size(); i++) {
                        Topic topic = topics.get(i);
                        Log.d(PC_TAG, topic.getId() + " " + topic.getName() + " " + topic.getSubscribedChannels().toString());
                    }

                    break;
                }
                default:
                    Log.d(PC_TAG, "unknown res type");
            }
        });

    }

    public void setPreferences(View view) {
        OptimovePreferenceCenter.getInstance().getPreferencesAsync((OptimovePreferenceCenter.ResultType result, Preferences preferences) -> {
            switch (result) {
                case ERROR_USER_NOT_SET:
                case ERROR:
                case ERROR_CREDENTIALS_NOT_SET:
                    Log.d(PC_TAG, result.toString());
                    break;
                case SUCCESS: {
                    Log.d(PC_TAG, "loaded prefs for set: good");


                    List<Channel> configuredChannels = preferences.getConfiguredChannels();
                    List<Topic> topics = preferences.getCustomerPreferences();

                    List<PreferenceUpdate> updates = new ArrayList<>();
                    for (int i = 0; i < topics.size(); i++) {
                        updates.add(new PreferenceUpdate(topics.get(i).getId(), configuredChannels.subList(0, 1)));
                    }

                    OptimovePreferenceCenter.getInstance().setCustomerPreferencesAsync((OptimovePreferenceCenter.ResultType setResult) -> {
                        Log.d(PC_TAG, setResult.toString());
                    }, updates);

                    break;
                }
                default:
                    Log.d(PC_TAG, "unknown res type");
            }
        });
    }

    // ******************** PC end *********************

    public void viewEmbeddedMessaging(View view) {
        // Do nothing for now
        Intent intent = new Intent(this, EmbeddedMessagingActivity.class);
        startActivity(intent);
    }

    public void setCredentials(View view) {
        EditText optimoveCreds = findViewById(R.id.optimoveCredInput);
        EditText optimobileCreds = findViewById(R.id.optimobileCredInput);
        EditText pcCreds = findViewById(R.id.prefCenterCredInput);

        String optimoveCredentials = optimoveCreds.getText().toString();
        String optimobileCredentials = optimobileCreds.getText().toString();
        String prefCenterCredentials = pcCreds.getText().toString();

        if (optimoveCredentials.isEmpty() && optimobileCredentials.isEmpty()) {
            return;
        }

        if (optimoveCredentials.isEmpty()) {
            optimoveCredentials = null;
        }

        if (optimobileCredentials.isEmpty()) {
            optimobileCredentials = null;
        }

        if (prefCenterCredentials.isEmpty()) {
            prefCenterCredentials = null;
        }

        try {
            Optimove.setCredentials(optimoveCredentials, optimobileCredentials, prefCenterCredentials);
        } catch (Exception e) {
            outputTv.setText(e.getMessage());
            return;
        }

        outputTv.setText("Credentials submitted");
        Button setCredsBtn = (Button) findViewById(R.id.submitCredentialsBtn);
        setCredsBtn.setEnabled(false);
    }

    public void runFromWorker(Runnable runnable) {
        new Thread(runnable).start();
    }

    private static class SimpleCustomEvent extends OptimoveEvent {

        SimpleCustomEvent() {
        }

        @Override
        public String getName() {
            return "Simple cUSTOM_Event     ";
        }

        @Override
        public Map<String, Object> getParameters() {
            HashMap<String, Object> result = new HashMap<>();
            String val = "  some_string  ";
            result.put("strinG_param", val);
            result.put("number_param", 42);
            return result;
        }
    }

    private void hideIrrelevantInputs() {
        if (!Optimove.getConfig().isPreferenceCenterConfigured()) {
            Button getPrefsBtn = (Button) findViewById(R.id.getPreferences);
            getPrefsBtn.setVisibility(View.GONE);

            Button setPrefsBtn = (Button) findViewById(R.id.setPreferences);
            setPrefsBtn.setVisibility(View.GONE);
        }

        if (!Optimove.getConfig().isEmbeddedMessagingConfigured()) {
            Button viewEmbeddedMessagingBtn = findViewById(R.id.viewEmbeddedMessaging);
            viewEmbeddedMessagingBtn.setVisibility(View.GONE);
        }

        if (!Optimove.getConfig().usesDelayedConfiguration()) {
            EditText optimoveCredInput = findViewById(R.id.optimoveCredInput);
            optimoveCredInput.setVisibility(View.GONE);

            EditText optimobileCredInput = findViewById(R.id.optimobileCredInput);
            optimobileCredInput.setVisibility(View.GONE);

            EditText pcCredInput = findViewById(R.id.prefCenterCredInput);
            pcCredInput.setVisibility(View.GONE);

            Button setCredsBtn = (Button) findViewById(R.id.submitCredentialsBtn);
            setCredsBtn.setVisibility(View.GONE);


        }
    }
    
    private void setupInAppMessageInterceptor() {
        try {
            OptimoveInApp.getInstance().setDisplayMode(OptimoveConfig.InAppDisplayMode.INTERCEPTED);
            
            OptimoveInApp.getInstance().setInAppMessageInterceptor(new InAppMessageInterceptor() {
                @Override
                public void shouldDisplayMessage(@NonNull InAppMessageInfo message, @NonNull InAppMessageInterceptorCallback callback) {
                    Log.d(TAG, "Interceptor called for message ID: " + message.getMessageId());
                    
                    if (isUserInCheckoutFlow()) {
                        Log.d(TAG, "User in checkout - suppressing message");
                        callback.onInterceptResult(InAppMessageInterceptor.InterceptResult.SUPPRESS);
                        return;
                    }
                    
                    if (message.getData() != null) {
                        try {
                            if (message.getData().has("vip_only") && 
                                message.getData().getBoolean("vip_only") && 
                                !isUserVip()) {
                                Log.d(TAG, "VIP-only message for non-VIP user - suppressing");
                                callback.onInterceptResult(InAppMessageInterceptor.InterceptResult.SUPPRESS);
                                return;
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error checking message data", e);
                        }
                    }
                    
                    if (shouldCheckExternalEligibility(message)) {
                        Log.d(TAG, "Checking external eligibility...");
                        checkUserEligibilityAsync(message, callback);
                        return;
                    }
                    
                    Log.d(TAG, "Showing message");
                    callback.onInterceptResult(InAppMessageInterceptor.InterceptResult.SHOW);
                }
            });
            
            Log.d(TAG, "In-app message interceptor configured successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to configure in-app message interceptor", e);
        }
    }
    
    private boolean isUserInCheckoutFlow() {
        return Math.random() < 0.3;
    }
    
    private boolean isUserVip() {
        return Math.random() < 0.5;
    }
    
    private boolean shouldCheckExternalEligibility(InAppMessageInfo message) {
        if (message.getData() != null) {
            return message.getData().has("check_external");
        }
        return false;
    }
    
    private void checkUserEligibilityAsync(InAppMessageInfo message, InAppMessageInterceptorCallback callback) {
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                boolean eligible = Math.random() < 0.7;
                Log.d(TAG, "External eligibility check complete: " + eligible);
                callback.onInterceptResult(eligible ? 
                    InAppMessageInterceptor.InterceptResult.SHOW : 
                    InAppMessageInterceptor.InterceptResult.SUPPRESS);
                    
            } catch (InterruptedException e) {
                Log.e(TAG, "Eligibility check interrupted", e);
                // On error, suppress the message
                callback.onInterceptResult(InAppMessageInterceptor.InterceptResult.SUPPRESS);
            }
        }).start();
    }
}

package com.optimove.sdk.optimovemobilesdk;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.optimove.sdk.optimove_sdk.main.Optimove;
import com.optimove.sdk.optimove_sdk.main.OptimoveSuccessStateListener;
import com.optimove.sdk.optimove_sdk.main.SdkOperationListener;
import com.optimove.sdk.optimove_sdk.main.constants.SdkRequiredPermission;
import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;
import com.optimove.sdk.optimove_sdk.main.tools.FileUtils;
import com.optimove.sdk.optimove_sdk.optipush.deep_link.DeepLinkHandler;
import com.optimove.sdk.optimove_sdk.optipush.deep_link.LinkDataError;
import com.optimove.sdk.optimove_sdk.optipush.deep_link.LinkDataExtractedListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity implements OptimoveSuccessStateListener {

  private static final int WRITE_EXTERNAL_PERMISSION_REQUEST_CODE = 169;

  private TextView outputTv;
  private boolean isInTestMode;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    outputTv = findViewById(R.id.userIdTextView);

    if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_PERMISSION_REQUEST_CODE);
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    runFromWorker(() -> Optimove.registerSuccessStateListener(this));
  }

  @Override
  protected void onStop() {
    super.onStop();
    runFromWorker(() -> Optimove.unregisterSuccessStateListener(this));
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    runFromWorker(() -> new DeepLinkHandler(intent).extractLinkData(new LinkDataExtractedListener() {
      @Override
      public void onDataExtracted(String screenName, Map<String, String> parameters) {
        Toast.makeText(MainActivity.this, String.format("New Intent called with screed %s, and data: %s",
            screenName, Arrays.deepToString(parameters.values().toArray(new String[parameters.size()]))), Toast.LENGTH_SHORT).show();
      }

      @Override
      public void onErrorOccurred(LinkDataError error) {
        Toast.makeText(MainActivity.this, "New Intent called without deep link", Toast.LENGTH_SHORT).show();
      }
    }));
  }

  public void reportEvent(View view) {
    if (view == null)
      return;
    outputTv.setText("Reporting Custom Event for Visitor without optional value");
    runFromWorker(() -> Optimove.getInstance().reportEvent(new SimpleCustomEvent()));
    runFromWorker(() -> Optimove.getInstance().reportEvent("Event_No ParaMs     "));
  }

  public void sendLog(View view) {
    outputTv.setText("Wait please!!!");
    FileOptiLoggerOutputStream.getInstance(this, new FileUtils()).sendAndCleanLogs(success -> {
      if (success) {
        outputTv.setText("The logs are waiting for you in the downloads dir");
      } else {
        outputTv.setText("Failed to save logs, ask Noy WTF");
      }
    });
  }

  public void updateUserId(View view) {
    EditText uidInput = findViewById(R.id.userIdInput);
    EditText emailInput = findViewById(R.id.userEmailInput);
    String userId = uidInput.getText().toString();
    String userEmail = emailInput.getText().toString();

    if (userEmail.isEmpty()) {
      outputTv.setText("Calling setUserId");
//      runFromWorker(() -> Optimove.getInstance().setUserId("noy-dev"));
      Optimove.getInstance().setUserId(userId);
    } else if (userId.isEmpty()) {
      outputTv.setText("Calling setUserEmail");
//      runFromWorker(() -> Optimove.getInstance().setUserId("noy-dev"));
      Optimove.getInstance().setUserEmail(userEmail);
    } else {
      outputTv.setText("Calling registerUser");
//      runFromWorker(() -> Optimove.getInstance().setUserId("noy-dev"));
      Optimove.getInstance().registerUser(userId, userEmail);
    }
  }

  public void dispatchNow(View view) {
    // TODO: 2019-09-15 remove it
//    OptitrackManager optitrackManager = Optimove.getInstance().getOptitrackManager();
//    if (optitrackManager != null) optitrackManager.sendAllEventsNow();
  }

  public void toggleTestMode(View view) {
    SdkOperationListener testModeListener = success -> {
      if (success) {
        Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show();
        isInTestMode = !isInTestMode;
      } else {
        Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show();
      }
    };
    if (isInTestMode) {
      runFromWorker(() -> Optimove.getInstance().stopTestMode(testModeListener));
    } else {
      runFromWorker(() -> Optimove.getInstance().startTestMode(testModeListener));
    }
  }

  @Override
  public void onConfigurationSucceed(SdkRequiredPermission... sdkRequiredPermissions) {
//    runFromWorker(() -> Optimove.getInstance().setScreenVisit(this, "https://www.test%2Aeruo.com"));
//    runFromWorker(() -> Optimove.getInstance().setScreenVisit(this, "https://www.test%2Aeruo.com", "Serious"));
//    runFromWorker(() -> Optimove.getInstance().setScreenVisit("test/me/Now Please", "Test"));
//    runFromWorker(() -> Optimove.getInstance().setScreenVisit("test/me/Now Please/With-Category", "Test", "PoopFest"));
  }

  public void runFromWorker(Runnable runnable) {
    new Thread(runnable).start();
  }

  private class SimpleCustomEvent implements OptimoveEvent {

    SimpleCustomEvent() {
    }

    @Override
    public String getName() {
      return "Simple cUSTOM_Event     ";
    }

    @Override
    public Map<String, Object> getParameters() {
      HashMap<String, Object> result = new HashMap<>();
      String val = "  oaisjdoiajdsoiajsdoiajsdoiajsdoij  ";
      result.put("strinG_param", val);// + val + val + val + val + val + val + val + val + val + val + val + val + val);
//      if (withOptional)
      result.put("number_param", 42);
//      result.put("not_see", "not_see!");
      return result;
    }
  }

  private class RealtimeTestEvent implements OptimoveEvent {

    @Override
    public String getName() {
      return "realtime_test_event";
    }

    @Override
    public Map<String, Object> getParameters() {
      HashMap<String, Object> result = new HashMap<>();
      result.put("dePosit", "10");
      return result;
    }
  }
}

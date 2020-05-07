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
import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;
import com.optimove.sdk.optimove_sdk.main.tools.FileUtils;
import com.optimove.sdk.optimove_sdk.optipush.deep_link.DeepLinkHandler;
import com.optimove.sdk.optimove_sdk.optipush.deep_link.LinkDataError;
import com.optimove.sdk.optimove_sdk.optipush.deep_link.LinkDataExtractedListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity {

  private static final int WRITE_EXTERNAL_PERMISSION_REQUEST_CODE = 169;

  private TextView outputTv;

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
      Optimove.getInstance().setUserId(userId);
    } else if (userId.isEmpty()) {
      outputTv.setText("Calling setUserEmail");
      Optimove.getInstance().setUserEmail(userEmail);
    } else {
      outputTv.setText("Calling registerUser");
      Optimove.getInstance().registerUser(userId, userEmail);
    }
  }

  public void runFromWorker(Runnable runnable) {
    new Thread(runnable).start();
  }

  private class SimpleCustomEvent extends OptimoveEvent {

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
      result.put("strinG_param", val);
      result.put("number_param", 42);
      return result;
    }
  }
}

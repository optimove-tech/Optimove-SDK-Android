package com.optimove.android.optimovemobilesdk;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.optimove.android.Optimove;
import com.optimove.android.main.events.OptimoveEvent;

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

    this.setCredInitialisationType();

    if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_PERMISSION_REQUEST_CODE);
    }
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

public void setCredentials(View view) {
     EditText optimoveCreds  = findViewById(R.id.optimoveCredInput);
     EditText optimobileCreds = findViewById(R.id.optimobileCredInput);

     String optimoveCredentials = optimoveCreds.getText().toString();
     String optimobileCredentials = optimobileCreds.getText().toString();

     if (optimoveCredentials.isEmpty() && optimobileCredentials.isEmpty()){
       return;
     }

     if (optimoveCredentials.isEmpty()){
       optimoveCredentials = null;
     }

      if (optimobileCredentials.isEmpty()){
        optimobileCredentials = null;
      }




      Optimove.setCredentials(optimoveCredentials, optimobileCredentials);

      outputTv.setText("Credentials submitted");
      Button setCredsBtn = (Button) findViewById(R.id.submitCredentialsBtn);
      setCredsBtn.setEnabled(false);

  }

  public void runFromWorker(Runnable runnable) {
    new Thread(runnable).start();
  }

  private static class SimpleCustomEvent extends OptimoveEvent {

    SimpleCustomEvent(){}

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

  private void setCredInitialisationType() {
    if (!Optimove.getConfig().usesDelayedOptimoveConfiguration() && !Optimove.getConfig().usesDelayedOptimobileConfiguration()){
      EditText optimoveCredInput = findViewById(R.id.optimoveCredInput);
      optimoveCredInput.setVisibility(View.GONE);

      EditText optimobileCredInput = findViewById(R.id.optimobileCredInput);
      optimobileCredInput.setVisibility(View.GONE);

      Button setCredsBtn = (Button) findViewById(R.id.submitCredentialsBtn);
      setCredsBtn.setVisibility(View.GONE);
    }
  }
}

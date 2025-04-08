package com.optimove.android.optimovemobilesdk;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.optimove.android.embeddedmessaging.Container;
import com.optimove.android.embeddedmessaging.ContainerMessageRequest;
import com.optimove.android.embeddedmessaging.OptimoveEmbeddedMessaging;

public class EmbeddedMessagingActivity  extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.embedded_messaging);
    }

    public void getMessages(View view) {
        ContainerMessageRequest[] request = new ContainerMessageRequest[1];
        request[0] = new ContainerMessageRequest("stuart", 10);
         OptimoveEmbeddedMessaging.getInstance().getEmbeddedMessagesAsync((OptimoveEmbeddedMessaging.ResultType result, Container[] containers) -> {
             TextView containersRetrievedAmt  = findViewById(R.id.containersRetrievedAmt);
             switch(result) {
                 case ERROR:
                     containersRetrievedAmt.setText("Generic error");
                 case ERROR_USER_NOT_SET:
                     containersRetrievedAmt.setText("User not set error");
                 case ERROR_CONFIG_NOT_SET:
                     // Show an error message somewhere
                     containersRetrievedAmt.setText("Config not set error");
                     break;
                 case SUCCESS:
                     // do something with containers
                     if(containers == null) return;

                     containersRetrievedAmt.setText(String.format("%d containers retrieved", containers.length));
                     break;
             }
         }, request);
    }
}

package com.optimove.android.optimovemobilesdk;

import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.optimove.android.embeddedmessaging.Container;
import com.optimove.android.embeddedmessaging.ContainerRequestOptions;
import com.optimove.android.embeddedmessaging.EmbeddedMessage;
import com.optimove.android.embeddedmessaging.EmbeddedMessagesResponse;
import com.optimove.android.embeddedmessaging.OptimoveEmbeddedMessaging;

import java.util.Map;

public class EmbeddedMessagingActivity extends AppCompatActivity {

    public EmbeddedMessage[] listMessages;
    private String selectedMessageId;
    private EmbeddedMessage selectedMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.embedded_messaging);
        ListView listView = findViewById(R.id.messageListView);
        listView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            Log.d("DEBUG", String.format("Item clicked at position: %s", listMessages[i].getId()));
            selectedMessageId = listMessages[i].getId();
            selectedMessage = listMessages[i];
            return false;
        });
        registerForContextMenu(listView);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Choose an Action");
        menu.add(0, v.getId(), 0, "Toggle Set as Read");
        menu.add(0, v.getId(), 0, "Click Metric");
        menu.add(0, v.getId(), 0, "Delete");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        String title = item.getTitle().toString();
        Log.d("DEBUG", String.format("Selected Item clicked with Id: %s", selectedMessageId));
        switch (title) {
            case "Toggle Set as Read":
                setAsRead();
                break;
            case "Click Metric":
                sendClickMetrics();
                break;
            case "Delete":
                deleteMessage();
                break;
        }
        return true;
    }

    public void getMessages(View view) {
        refreshMessages();
    }

    private void refreshMessages() {
        ContainerRequestOptions[] request = getRequestBody();
        OptimoveEmbeddedMessaging.getInstance().getMessagesAsync(request, (OptimoveEmbeddedMessaging.ResultType result, EmbeddedMessagesResponse response) -> {
            TextView containersRetrievedAmt = findViewById(R.id.containersRetrievedAmt);
            if (result == OptimoveEmbeddedMessaging.ResultType.SUCCESS) {
                if (response.getContainersMap() == null) return;
                updateViewFromResponse(response.getContainersMap(), containersRetrievedAmt);
            } else {
                handleErrors(result, containersRetrievedAmt);
            }
        });
    }

    private void sendClickMetrics() {
        OptimoveEmbeddedMessaging.getInstance().reportClickMetricAsync(selectedMessage, (OptimoveEmbeddedMessaging.ResultType result) -> {
            TextView containersRetrievedAmt = findViewById(R.id.containersRetrievedAmt);
            if (result == OptimoveEmbeddedMessaging.ResultType.SUCCESS) {
                containersRetrievedAmt.setText("Click Metrics Sent");
            } else {
                handleErrors(result, containersRetrievedAmt);
            }
        });
    }

    private void setAsRead() {
        boolean unreadMessage = selectedMessage.getReadAt() == null;
        OptimoveEmbeddedMessaging.getInstance().setAsReadASync(
                selectedMessage, unreadMessage,
                (OptimoveEmbeddedMessaging.ResultType result) -> {
                    TextView containersRetrievedAmt = findViewById(R.id.containersRetrievedAmt);
                    if (result == OptimoveEmbeddedMessaging.ResultType.SUCCESS) {
                        refreshMessages();
                        containersRetrievedAmt.setText("Message marked as " + (unreadMessage ? "read" : "unread"));
                    } else {
                        handleErrors(result, containersRetrievedAmt);
                    }
                });
    }

    private void deleteMessage() {
        OptimoveEmbeddedMessaging.getInstance().deleteMessageAsync(selectedMessage, (OptimoveEmbeddedMessaging.ResultType result) -> {
            TextView containersRetrievedAmt = findViewById(R.id.containersRetrievedAmt);
            if (result == OptimoveEmbeddedMessaging.ResultType.SUCCESS) {
                refreshMessages();
                containersRetrievedAmt.setText("Message Deleted");
            } else {
                handleErrors(result, containersRetrievedAmt);
            }
        });
    }

    private void handleErrors(OptimoveEmbeddedMessaging.ResultType result, TextView containersRetrievedAmt) {
        switch (result) {
            case ERROR:
                containersRetrievedAmt.setText("Generic error");
                break;
            case ERROR_USER_NOT_SET:
                containersRetrievedAmt.setText("User not set error");
                break;
            case ERROR_CONFIG_NOT_SET:
                containersRetrievedAmt.setText("Config not set error");
                break;
        }
    }

    private ContainerRequestOptions[] getRequestBody() {
        EditText containerEdit = findViewById(R.id.containerEdit);
        String containerString = containerEdit.getText().toString();
        EditText limitEdit = findViewById(R.id.limitEdit);

        if (containerString.isEmpty()) {
            return new ContainerRequestOptions[]{};
        }

        String[] containerIds = containerString.split(";");
        String[] limits = limitEdit.getText().toString().split(";");

        ContainerRequestOptions[] request = new ContainerRequestOptions[containerIds.length];
        for (int i = 0; i < containerIds.length; i++) {
            int limit;
            try {
                limit = (i > limits.length - 1) ? 50 : Integer.parseInt(limits[i]);
            } catch (Exception e) {
                limit = 50;
            }
            request[i] = new ContainerRequestOptions(containerIds[i], limit);
        }
        return request;
    }

    private void updateViewFromResponse(Map<String, Container> response, TextView containersRetrievedAmt) {
        containersRetrievedAmt.setText(String.format("%d containers retrieved", response.size()));
        ListView listView = findViewById(R.id.messageListView);
        ArrayAdapter<String> adaptor = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(adaptor);
        for (Map.Entry<String, Container> entry : response.entrySet()) {
            listMessages = entry.getValue().getMessages();
            for (EmbeddedMessage message : listMessages) {
                boolean messageUnread = message.getReadAt() == null;
                adaptor.add(String.format("%s: %s %s", message.getTitle(), message.getContent(), messageUnread ? "•": ""));
            }
        }
    }
}

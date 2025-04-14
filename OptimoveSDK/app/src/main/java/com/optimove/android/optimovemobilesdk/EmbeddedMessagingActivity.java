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
import com.optimove.android.embeddedmessaging.ContainerMessageRequest;
import com.optimove.android.embeddedmessaging.EmbeddedMessage;
import com.optimove.android.embeddedmessaging.EmbeddedMessageMetricsRequest;
import com.optimove.android.embeddedmessaging.EmbeddedMessageStatusRequest;
import com.optimove.android.embeddedmessaging.MetricEvent;
import com.optimove.android.embeddedmessaging.OptimoveEmbeddedMessaging;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
        menu.add(0, v.getId(), 0, "Mark as Read");
        menu.add(0, v.getId(), 0, "Click Metric");
        menu.add(0, v.getId(), 0, "Delete");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        String title = item.getTitle().toString();
        Log.d("DEBUG", String.format("Selected Item clicked with Id: %s", selectedMessageId));
        switch (title) {
            case "Mark as Read":
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
        ContainerMessageRequest[] request = getRequestBody();
        if (request.length < 1) {
            OptimoveEmbeddedMessaging.getInstance().getEmbeddedMessagesAsync((OptimoveEmbeddedMessaging.ResultType result, List<Container> containers) -> {
                TextView containersRetrievedAmt = findViewById(R.id.containersRetrievedAmt);
                if (result == OptimoveEmbeddedMessaging.ResultType.SUCCESS) {
                    if (containers == null) return;
                    updateViewFromContainers(containers, containersRetrievedAmt);
                } else {
                    handleErrors(result, containersRetrievedAmt);
                }
            });
        } else {
            OptimoveEmbeddedMessaging.getInstance().getEmbeddedMessagesAsync(request, (OptimoveEmbeddedMessaging.ResultType result, List<Container> containers) -> {
                TextView containersRetrievedAmt = findViewById(R.id.containersRetrievedAmt);
                if (result == OptimoveEmbeddedMessaging.ResultType.SUCCESS) {
                    if (containers == null) return;
                    updateViewFromContainers(containers, containersRetrievedAmt);
                } else {
                    handleErrors(result, containersRetrievedAmt);
                }
            });
        }
    }

    private void sendClickMetrics() {
        Date justNow = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        EmbeddedMessageMetricsRequest request = new EmbeddedMessageMetricsRequest(
                sdf.format(justNow), MetricEvent.CLICK, selectedMessage.getEngagementId(), sdf.format(justNow),
                selectedMessage.getCampaignKind());
        OptimoveEmbeddedMessaging.getInstance().reportClickMetricAsync(request, (OptimoveEmbeddedMessaging.ResultType result) -> {
            TextView containersRetrievedAmt = findViewById(R.id.containersRetrievedAmt);
            if (result == OptimoveEmbeddedMessaging.ResultType.SUCCESS) {
                containersRetrievedAmt.setText("Click Metrics Sent");
            } else {
                handleErrors(result, containersRetrievedAmt);
            }
        });
    }

    private void setAsRead() {
        Date justNow = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        EmbeddedMessageStatusRequest request = new EmbeddedMessageStatusRequest(
                sdf.format(justNow), selectedMessage.getEngagementId(), selectedMessage.getCampaignKind(),
                selectedMessageId, justNow);
        OptimoveEmbeddedMessaging.getInstance().setAsReadASync(request, (OptimoveEmbeddedMessaging.ResultType result) -> {
            TextView containersRetrievedAmt = findViewById(R.id.containersRetrievedAmt);
            if (result == OptimoveEmbeddedMessaging.ResultType.SUCCESS) {
                containersRetrievedAmt.setText("Message marked as Read");
            } else {
                handleErrors(result, containersRetrievedAmt);
            }
        });
    }

    private void deleteMessage() {
        OptimoveEmbeddedMessaging.getInstance().deleteEmbeddedMessageAsync(selectedMessageId, (OptimoveEmbeddedMessaging.ResultType result) -> {
            TextView containersRetrievedAmt = findViewById(R.id.containersRetrievedAmt);
            if (result == OptimoveEmbeddedMessaging.ResultType.SUCCESS) {
                containersRetrievedAmt.setText("Message Deleted");
                refreshMessages();
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

    private ContainerMessageRequest[] getRequestBody() {
        EditText containerEdit = findViewById(R.id.containerEdit);
        String containerString = containerEdit.getText().toString();
        EditText limitEdit = findViewById(R.id.limitEdit);

        if (containerString.isEmpty()) {
            return new ContainerMessageRequest[]{};
        }

        String[] containerIds = containerString.split(";");
        String[] limits = limitEdit.getText().toString().split(";");

        ContainerMessageRequest[] request = new ContainerMessageRequest[containerIds.length];
        for (int i = 0; i < containerIds.length; i++) {
            int limit;
            try {
                limit = (i > limits.length - 1) ? 50 : Integer.parseInt(limits[i]);
            } catch (Exception e) {
                limit = 50;
            }
            request[i] = new ContainerMessageRequest(containerIds[i], limit);
        }
        return request;
    }

    private void updateViewFromContainers(List<Container> containers, TextView containersRetrievedAmt) {
        containersRetrievedAmt.setText(String.format("%d containers retrieved", containers.toArray().length));
        ListView listView = findViewById(R.id.messageListView);
        ArrayAdapter<String> adaptor = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(adaptor);
        for (Container container : containers) {
            listMessages = container.getMessages();
            for (EmbeddedMessage message : listMessages) {
                adaptor.add(String.format("%s: %s", message.getTitle(), message.getContent()));
            }
        }

    }
}

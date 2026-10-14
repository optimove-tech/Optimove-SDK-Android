package com.optimove.android.optimobile;

import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.os.Looper;

import androidx.test.core.app.ApplicationProvider;

import com.optimove.android.OptimoveConfig;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class InAppMessageTests {

    private static final long ONE_HOUR_MS = 60 * 60 * 1000;

    private InAppMessagePresenter presenter;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        presenter = new InAppMessagePresenter(context, OptimoveConfig.InAppDisplayMode.AUTOMATIC);
    }

    private static Date hoursFromNow(int hours) {
        return new Date(System.currentTimeMillis() + hours * ONE_HOUR_MS);
    }

    private InAppMessage messageExpiringAt(Date expiresAt) {
        return messageExpiringAt(1, expiresAt);
    }

    private InAppMessage messageExpiringAt(int id, Date expiresAt) {
        return new InAppMessage(id, "immediately", new JSONObject(), null, null, null, expiresAt);
    }

    private void present(InAppMessage... messages) {
        presenter.presentMessages(new ArrayList<>(Arrays.asList(messages)), null);
        shadowOf(Looper.getMainLooper()).idle();
    }

    private void triggerPresentation() {
        presenter.retryPresentingMessages();
        shadowOf(Looper.getMainLooper()).idle();
    }

    @SuppressWarnings("unchecked")
    private List<Integer> queuedIds() throws Exception {
        Field field = InAppMessagePresenter.class.getDeclaredField("messageQueue");
        field.setAccessible(true);
        List<Integer> ids = new ArrayList<>();
        for (InAppMessage message : (List<InAppMessage>) field.get(presenter)) {
            ids.add(message.getInAppId());
        }
        return ids;
    }

    @Test
    public void messageWithoutExpiryIsNotExpired() {
        Assert.assertFalse(messageExpiringAt(null).isExpired());
    }

    @Test
    public void messageWithFutureExpiryIsNotExpired() {
        Assert.assertFalse(messageExpiringAt(hoursFromNow(1)).isExpired());
    }

    @Test
    public void messageWithPastExpiryIsExpired() {
        Assert.assertTrue(messageExpiringAt(hoursFromNow(-1)).isExpired());
    }

    @Test
    public void clearingExpiryMakesMessageNotExpired() {
        InAppMessage message = messageExpiringAt(hoursFromNow(-1));

        message.setExpiresAt(null);

        Assert.assertFalse(message.isExpired());
    }

    @Test
    public void expiredMessagesAreDroppedFromTheQueueBeforePresentation() throws Exception {
        present(
                messageExpiringAt(1, hoursFromNow(-2)),
                messageExpiringAt(2, hoursFromNow(-1)),
                messageExpiringAt(3, hoursFromNow(1)));

        Assert.assertEquals(Arrays.asList(3), queuedIds());
    }

    @Test
    public void messageThatExpiresWhileQueuedIsDroppedOnNextPresentation() throws Exception {
        InAppMessage message = messageExpiringAt(1, hoursFromNow(1));
        present(message);
        Assert.assertEquals(Arrays.asList(1), queuedIds());

        message.setExpiresAt(hoursFromNow(-1));
        triggerPresentation();

        Assert.assertTrue(queuedIds().isEmpty());
    }
}

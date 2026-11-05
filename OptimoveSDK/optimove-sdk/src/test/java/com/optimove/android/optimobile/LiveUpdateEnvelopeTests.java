package com.optimove.android.optimobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

public class LiveUpdateEnvelopeTests {

    private static JSONObject start() throws JSONException {
        return new JSONObject()
                .put("event", "start")
                .put("name", "spain-vs-argentina")
                .put("type", "SportsActivityAttributes")
                .put("timestamp", 1786024800L)
                .put("topic", "lu_1_3f2a")
                .put("content_state", new JSONObject().put("homeScore", 0));
    }

    private static String custom(Object liveUpdate) throws JSONException {
        return new JSONObject().put("a", new JSONObject().put(LiveUpdateEnvelope.KEY, liveUpdate)).toString();
    }

    @Test
    public void extractRecognisesLiveUpdatesAndNothingElse() throws JSONException {
        String normalPush = new JSONObject()
                .put("a", new JSONObject().put("k.message", new JSONObject().put("data", new JSONObject().put("id", 1))))
                .toString();

        assertFalse(LiveUpdateEnvelope.looksLikeLiveUpdate(normalPush));
        assertNull(LiveUpdateEnvelope.extract(normalPush));
        assertNull(LiveUpdateEnvelope.extract(null));
        assertNull(LiveUpdateEnvelope.extract("k.liveUpdate but not json"));
        assertNotNull(LiveUpdateEnvelope.extract(custom(start())));
        assertEquals("garbage", LiveUpdateEnvelope.extract(custom("garbage")));
    }

    @Test
    public void parsesFullStartEnvelope() throws JSONException {
        LiveUpdateEnvelope envelope = LiveUpdateEnvelope.parse(start()
                .put("alert", new JSONObject().put("title", "Match starting").put("body", "Spain vs Argentina")));

        assertEquals(LiveUpdateEvent.START, envelope.event);
        assertEquals("spain-vs-argentina", envelope.name);
        assertEquals("SportsActivityAttributes", envelope.type);
        assertEquals(1786024800L, envelope.timestamp);
        assertEquals("lu_1_3f2a", envelope.topic);
        assertEquals(0, envelope.contentState.getInt("homeScore"));
        assertEquals("Match starting", envelope.alertTitle);
        assertEquals("Spain vs Argentina", envelope.alertBody);
        assertNull(envelope.dismissalDateSeconds);
    }

    @Test
    public void endNeedsOnlyNameAndTimestampAndMayCarryDismissalDate() throws JSONException {
        LiveUpdateEnvelope envelope = LiveUpdateEnvelope.parse(new JSONObject()
                .put("event", "end")
                .put("name", "spain-vs-argentina")
                .put("timestamp", 4_300_000_000L)
                .put("dismissal_date", 4_300_003_600L));

        assertEquals(LiveUpdateEvent.END, envelope.event);
        assertNull(envelope.type);
        assertFalse(envelope.hasContentState);
        assertEquals(0, envelope.contentState.length());
        assertEquals(4_300_000_000L, envelope.timestamp);
        assertEquals(Long.valueOf(4_300_003_600L), envelope.dismissalDateSeconds);
    }

    @Test
    public void rejectsMalformedEnvelopes() throws JSONException {
        JSONObject update = new JSONObject()
                .put("event", "update")
                .put("name", "spain-vs-argentina")
                .put("timestamp", 1786024801L)
                .put("content_state", new JSONObject());

        Object[] malformed = {
                null,
                "garbage",
                start().put("event", "pause"),
                start().put("name", ""),
                start().put("timestamp", JSONObject.NULL),
                start().put("timestamp", -1),
                start().put("type", JSONObject.NULL),
                start().put("topic", JSONObject.NULL),
                start().put("content_state", JSONObject.NULL),
                update.put("content_state", "not an object"),
        };

        for (Object raw : malformed) {
            assertThrows(String.valueOf(raw), JSONException.class, () -> LiveUpdateEnvelope.parse(raw));
        }
        assertTrue(LiveUpdateEnvelope.looksLikeLiveUpdate(custom(start().put("event", "pause"))));
    }
}

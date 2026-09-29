package com.optimove.android.optimobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

public class LiveUpdateEnvelopeTests {

    private static JSONObject envelope(String event, long sequence) throws JSONException {
        return new JSONObject()
                .put("event", event)
                .put("activity_id", "7c9e6679-7425-40de-944b-e07fc1f90ae7")
                .put("type", "notification")
                .put("sequence", sequence)
                .put("content", new JSONObject().put("home_score", 1));
    }

    private static String custom(Object liveUpdate) throws JSONException {
        return new JSONObject().put("a", new JSONObject().put(LiveUpdateEnvelope.KEY, liveUpdate)).toString();
    }

    // ========================= extract =========================

    @Test
    public void extractReturnsNullForNormalPush() throws JSONException {
        String normalPush = new JSONObject()
                .put("a", new JSONObject().put("k.message", new JSONObject().put("type", 1)))
                .toString();

        assertNull(LiveUpdateEnvelope.extract(normalPush));
    }

    @Test
    public void extractReturnsNullForInvalidJsonOrMissingMap() {
        assertNull(LiveUpdateEnvelope.extract(null));
        assertNull(LiveUpdateEnvelope.extract("not json"));
        assertNull(LiveUpdateEnvelope.extract("{}"));
    }

    @Test
    public void extractReturnsValueEvenWhenNotAnObject() throws JSONException {
        assertEquals("garbage", LiveUpdateEnvelope.extract(custom("garbage")));
    }

    // ========================= parse =========================

    @Test
    public void parsesFullStartEnvelope() throws JSONException {
        JSONObject json = envelope("start", 0)
                .put("name", "spain-vs-argentina")
                .put("alert", new JSONObject().put("title", "Match starting").put("body", "Spain vs Argentina"));

        LiveUpdateEnvelope parsed = LiveUpdateEnvelope.parse(json);

        assertEquals(LiveUpdateEvent.START, parsed.event);
        assertEquals("7c9e6679-7425-40de-944b-e07fc1f90ae7", parsed.activityId);
        assertEquals("notification", parsed.type);
        assertEquals("spain-vs-argentina", parsed.name);
        assertEquals(0, parsed.sequence);
        assertEquals(1, parsed.content.getInt("home_score"));
        assertEquals("Match starting", parsed.alertTitle);
        assertEquals("Spain vs Argentina", parsed.alertBody);
    }

    @Test
    public void typeIsOptionalOnUpdate() throws JSONException {
        JSONObject json = envelope("update", 3);
        json.remove("type");

        LiveUpdateEnvelope parsed = LiveUpdateEnvelope.parse(json);

        assertNull(parsed.type);
        assertNull(parsed.name);
        assertNull(parsed.alertTitle);
    }

    @Test
    public void rejectsMalformedEnvelopes() throws JSONException {
        JSONObject startWithoutType = envelope("start", 0);
        startWithoutType.remove("type");

        JSONObject withoutActivityId = envelope("update", 1);
        withoutActivityId.remove("activity_id");

        JSONObject withoutSequence = envelope("update", 1);
        withoutSequence.remove("sequence");

        JSONObject withoutContent = envelope("update", 1);
        withoutContent.remove("content");

        Object[] malformed = {
                null,
                "garbage",
                envelope("pause", 1),
                startWithoutType,
                withoutActivityId,
                withoutSequence,
                withoutContent,
                envelope("update", 1).put("content", "not an object"),
                envelope("update", -1),
        };

        for (Object raw : malformed) {
            assertThrows(String.valueOf(raw), JSONException.class, () -> LiveUpdateEnvelope.parse(raw));
        }
    }

    @Test
    public void rejectedEnvelopeIsStillRecognisedAsLiveUpdate() throws JSONException {
        assertTrue(LiveUpdateEnvelope.extract(custom(new JSONObject())) instanceof JSONObject);
    }
}

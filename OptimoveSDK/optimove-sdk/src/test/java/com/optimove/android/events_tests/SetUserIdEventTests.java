package com.optimove.android.events_tests;

import com.optimove.android.main.events.core_events.SetUserIdEvent;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class SetUserIdEventTests {

    @Test
    public void setUserIdShouldContainTheRightName(){
        String originalVisitor = "some_or_visitor";
        String userId = "some_us_id";
        String updateVisitorId = "some_visitor_id";

        SetUserIdEvent setUserIdEvent = new SetUserIdEvent(originalVisitor,userId,updateVisitorId);

        Assert.assertEquals(SetUserIdEvent.EVENT_NAME,setUserIdEvent.getName());
    }
    @Test
    public void setUserIdShouldContainRightParams(){
        String originalVisitor = "some_or_visitor";
        String userId = "some_us_id";
        String updateVisitorId = "some_visitor_id";

        SetUserIdEvent setUserIdEvent = new SetUserIdEvent(originalVisitor,userId,updateVisitorId);
        Map<String, Object> parameters = setUserIdEvent.getParameters();

        Assert.assertEquals(parameters.get(SetUserIdEvent.UPDATED_VISITOR_ID_PARAM_KEY),updateVisitorId);
        Assert.assertEquals(parameters.get(SetUserIdEvent.USER_ID_PARAM_KEY),userId);
        Assert.assertEquals(parameters.get(SetUserIdEvent.ORIGINAL_VISITOR_ID_PARAM_KEY),originalVisitor);

    }

}

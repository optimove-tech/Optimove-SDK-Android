package com.optimove.android.events_tests;

import com.optimove.android.main.events.core_events.SetEmailEvent;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class SetEmailEventTests {


    @Test
    public void setEmailShouldContainTheRightName(){
        String email = "some_email";

        SetEmailEvent setEmailEvent = new SetEmailEvent(email);

        Assert.assertEquals(SetEmailEvent.EVENT_NAME,setEmailEvent.getName());
    }
    @Test
    public void setEmailShouldContainRightParams(){
        String email = "some_email";

        SetEmailEvent setEmailEvent = new SetEmailEvent(email);
        Map<String, Object> parameters = setEmailEvent.getParameters();

        Assert.assertEquals(parameters.get(SetEmailEvent.EMAIL_PARAM_KEY),email);
    }
}

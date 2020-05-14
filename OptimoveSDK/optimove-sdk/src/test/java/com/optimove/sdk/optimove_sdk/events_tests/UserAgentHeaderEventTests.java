package com.optimove.sdk.optimove_sdk.events_tests;

import com.optimove.sdk.optimove_sdk.main.events.core_events.UserAgentHeaderEvent;
import com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

import static com.optimove.sdk.optimove_sdk.main.events.core_events.UserAgentHeaderEvent.USER_AGENT_VALUE_MAX_LENGTH;

public class UserAgentHeaderEventTests {

    @Test
    public void userAgentHeaderShouldContainTheRightName(){
        String userAgent = "some_user_agent";

        UserAgentHeaderEvent userAgentHeaderEvent = new UserAgentHeaderEvent(userAgent);

        Assert.assertEquals(UserAgentHeaderEvent.EVENT_NAME,userAgentHeaderEvent.getName());
    }
    @Test
    public void userAgentHeaderShouldContainOnlyOneHeaderParam(){
        String userAgent = "some_user_agent";

        UserAgentHeaderEvent userAgentHeaderEvent = new UserAgentHeaderEvent(userAgent);
        Map<String, Object> parameters = userAgentHeaderEvent.getParameters();

        Assert.assertEquals(parameters.get(UserAgentHeaderEvent.USER_AGENT_HEADER1_PARAM_KEY),userAgent);
        Assert.assertFalse(parameters.containsKey(UserAgentHeaderEvent.USER_AGENT_HEADER2_PARAM_KEY));
    }
    @Test
    public void userAgentHeaderShouldContainTwoHeaderParams(){
        String userAgent = "some_user_agentsome_user_agentsome_user_agentsome_user_agentsome_user_agentsome_" +
                "user_agentsome_user_agentsome_user_agentsome_user_agentso" +
                "me_user_agentsome_user_agentsome_user_agentsome_user_age" +
                "ntsome_user_agentsome_user_agentsome_user_agentsome_user_agen" +
                "tsome_user_agentsome_user_agentsome_user_agentsome_user_agentsom" +
                "e_user_agentsome_user_agentsome_user_agent";

        UserAgentHeaderEvent userAgentHeaderEvent = new UserAgentHeaderEvent(userAgent);
        Map<String, Object> parameters = userAgentHeaderEvent.getParameters();

        Assert.assertEquals(parameters.get(UserAgentHeaderEvent.USER_AGENT_HEADER1_PARAM_KEY),userAgent.substring(0, USER_AGENT_VALUE_MAX_LENGTH));
        Assert.assertEquals(parameters.get(UserAgentHeaderEvent.USER_AGENT_HEADER2_PARAM_KEY),userAgent.substring(USER_AGENT_VALUE_MAX_LENGTH));

    }
}

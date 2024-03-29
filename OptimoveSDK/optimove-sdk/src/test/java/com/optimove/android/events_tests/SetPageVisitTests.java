package com.optimove.android.events_tests;

import com.optimove.android.main.events.core_events.SetPageVisitEvent;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class SetPageVisitTests {

    @Test
    public void setPageVisitShouldContainTheRightName(){
        String pageTitle = "page_title";
        String pageCategory = "page_category";

        SetPageVisitEvent setPageVisitEvent = new SetPageVisitEvent(pageTitle,pageCategory);

        Assert.assertEquals(SetPageVisitEvent.EVENT_NAME,setPageVisitEvent.getName());
    }
    @Test
    public void setPageVisitShouldContainRightParams(){
        String pageTitle = "page_title";
        String pageCategory = "page_category";

        SetPageVisitEvent setPageVisitEvent = new SetPageVisitEvent(pageTitle,pageCategory);
        Map<String, Object> parameters = setPageVisitEvent.getParameters();

        Assert.assertEquals(parameters.get(SetPageVisitEvent.CATEGORY_PARAM_KEY),pageCategory);
        Assert.assertEquals(parameters.get(SetPageVisitEvent.PAGE_TITLE_PARAM_KEY),pageTitle);

    }
}

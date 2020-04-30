package com.optimove.sdk.optimove_sdk;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.optimove.sdk.optimove_sdk.fixtures.ConfigProvider;
import com.optimove.sdk.optimove_sdk.main.LifecycleObserver;
import com.optimove.sdk.optimove_sdk.main.UserInfo;
import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.SetPageVisitEvent;
import com.optimove.sdk.optimove_sdk.main.events.core_events.SetUserIdEvent;
import com.optimove.sdk.optimove_sdk.main.events.decorators.OptimoveEventDecorator;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.configs.Configs;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.configs.OptitrackConfigs;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.reused_configs.EventConfigs;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.LAST_OPT_REPORTED_KEY;
import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.OPTITRACK_SP_NAME;
import static com.optimove.sdk.optimove_sdk.optitrack.OptitrackConstants.OPTITRACK_USER_ID_KEY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class OptitrackTests {

    @Mock
    Context context;
    @Mock
    SharedPreferences optitrackPreferences;
    @Mock
    SharedPreferences.Editor editor;
    @Mock
    UserInfo userInfo;
    @Mock
    MatomoAdapter matomoAdapter;

    LifecycleObserver lifecycleObserver;
    //fixtures
    private OptitrackConfigs optitrackConfigs;
    private Map<String, EventConfigs> eventConfigsMap;
    private int tenantId;

    //user
    private String visitorId = "some_random_visitor_id";
    private String initialVisitorId = UUID.randomUUID()
            .toString()
            .replaceAll("-", "")
            .substring(0, 16);

    private String packageName = "package_name";

    private OptitrackManager optitrackManager;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        //shared prefs
        when(context.getSharedPreferences(OPTITRACK_SP_NAME, Context.MODE_PRIVATE)).thenReturn(optitrackPreferences);
        when(optitrackPreferences.edit()).thenReturn(editor);
        when(optitrackPreferences.getInt(eq(LAST_OPT_REPORTED_KEY), anyInt())).thenReturn(-1);
        when(editor.putLong(anyString(), anyLong())).thenReturn(editor);
        when(editor.putString(anyString(), any())).thenReturn(editor);
        when(editor.putInt(anyString(), anyInt())).thenReturn(editor);

        //userinfo
        when(userInfo.getVisitorId()).thenReturn(visitorId);
        when(userInfo.getInitialVisitorId()).thenReturn(initialVisitorId);
        when(userInfo.getAdvertisingId()).thenReturn("asdgsdfg");


        Configs configs = ConfigProvider.getConfigs(packageName);
        optitrackConfigs = configs.getOptitrackConfigs();
        eventConfigsMap = configs.getEventsConfigs();
        tenantId = configs.getTenantId();

        lifecycleObserver = new LifecycleObserver();
        optitrackManager = new OptitrackManager(matomoAdapter, this.optitrackConfigs, userInfo,
                eventConfigsMap, lifecycleObserver,
                context);
    }

    @Test
    public void matomoAdapterSetupShouldBeCalledWhenInitialized() {
        optitrackManager.setTimeout(444);
        verify(matomoAdapter).setup(this.optitrackConfigs.getOptitrackEndpoint(), tenantId, context);
    }

    @Test
    public void visitorIdShouldBeSyncedWhenInitialized() {
        optitrackManager.setTimeout(444);
        verify(matomoAdapter).setVisitorId(visitorId);
    }

    @Test
    public void userIdShouldBeSetToNullWhenAbsentInUserInfoWhenInitialized() {
        when(userInfo.getUserId()).thenReturn(null);
        optitrackManager.setTimeout(444);
        verify(matomoAdapter).setUserId(null);
    }

    @Test
    public void userIdShouldBeSetIfTrackerUserIdAbsentWhenInitialized() {
        String userInfoUserId = "some_user_id";

        when(userInfo.getUserId()).thenReturn(userInfoUserId);
        when(optitrackPreferences.getString(eq(OPTITRACK_USER_ID_KEY), any())).thenReturn(null);
        ArgumentCaptor<List<CustomDimension>> customDimensionsArguments = ArgumentCaptor.forClass(List.class);

        optitrackManager.setTimeout(444);

        verify(matomoAdapter).setUserId(userInfoUserId);
        verify(matomoAdapter, timeout(1000)).track(eq(this.optitrackConfigs.getEventCategoryName()), eq(SetUserIdEvent.EVENT_NAME),
                customDimensionsArguments.capture(),
                eq(initialVisitorId));

        List<CustomDimension> setUserIdEventDimensions = customDimensionsArguments.getAllValues()
                .get(0);

        EventConfigs setUserIdEventConfig = eventConfigsMap.get(SetUserIdEvent.EVENT_NAME);
        if (setUserIdEventConfig == null) {
            Assert.fail();
        }

        Assert.assertTrue(customDimensionsAndDecorationCreatedTheRightWay(setUserIdEventDimensions,
                setUserIdEventConfig, new OptimoveEventDecorator(new SetUserIdEvent(userInfo.getInitialVisitorId(), userInfoUserId, visitorId),
                        setUserIdEventConfig)));
    }


    @Test
    public void userIdShouldBeSetIfTrackerUserIdAndUserInfoUserIdAreDifferentWhenInitialized() {
        String userInfoUserId = "some_user_id";
        String trackerUserId = "another_user_id";

        when(userInfo.getUserId()).thenReturn(userInfoUserId);
        when(optitrackPreferences.getString(eq(OPTITRACK_USER_ID_KEY), any())).thenReturn(trackerUserId);
        ArgumentCaptor<List<CustomDimension>> customDimensionsArguments = ArgumentCaptor.forClass(List.class);

        optitrackManager.setTimeout(444);

        verify(matomoAdapter).setUserId(userInfoUserId);
        verify(matomoAdapter, timeout(1000)).track(eq(this.optitrackConfigs.getEventCategoryName()), eq(SetUserIdEvent.EVENT_NAME),
                customDimensionsArguments.capture(),
                eq(initialVisitorId));

        List<CustomDimension> setUserIdEventDimensions = customDimensionsArguments.getAllValues()
                .get(0);

        EventConfigs setUserIdEventConfig = eventConfigsMap.get(SetUserIdEvent.EVENT_NAME);
        if (setUserIdEventConfig == null) {
            Assert.fail();
        }

        Assert.assertTrue(customDimensionsAndDecorationCreatedTheRightWay(setUserIdEventDimensions, setUserIdEventConfig,
                new OptimoveEventDecorator(new SetUserIdEvent(userInfo.getInitialVisitorId(), userInfoUserId, visitorId),
                        setUserIdEventConfig)));
    }


    @Test
    public void setUserIdShouldBeCalledAndSetToPrefs() {
        String userId = "some_user_id";
        ArgumentCaptor<List<CustomDimension>> customDimensionsArguments = ArgumentCaptor.forClass(List.class);
        SetUserIdEvent setUserIdEvent = new SetUserIdEvent("sfdggf", userId, "sdfgdfg");

        optitrackManager.reportEvent(setUserIdEvent, eventConfigsMap.get(SetUserIdEvent.EVENT_NAME));

        verify(matomoAdapter).setUserId(userId);
        verify(matomoAdapter, timeout(1000)).track(eq(this.optitrackConfigs.getEventCategoryName()),
                eq(setUserIdEvent.getName()),
                customDimensionsArguments.capture(), eq(initialVisitorId));

        List<CustomDimension> setUserIdEventDimensions = customDimensionsArguments.getAllValues()
                .get(0);

        Assert.assertTrue(customDimensionsAndDecorationCreatedTheRightWay(setUserIdEventDimensions,
                eventConfigsMap.get(SetUserIdEvent.EVENT_NAME), setUserIdEvent));

        InOrder inOrder = inOrder(editor);
        inOrder.verify(editor)
                .putString(OPTITRACK_USER_ID_KEY, userId);
        inOrder.verify(editor)
                .apply();

    }

    @Test
    public void reportScreenVisitFuncAndEventShouldBeCalled() {
        String screenTitle = "some_screen_title";
        String screenCategory = "some_screen_category";

        SetPageVisitEvent setPageVisitEvent = new SetPageVisitEvent(screenTitle, screenCategory);
        ArgumentCaptor<List<CustomDimension>> customDimensionsArguments = ArgumentCaptor.forClass(List.class);

        optitrackManager.reportEvent(new OptimoveEventDecorator(setPageVisitEvent), eventConfigsMap.get(SetPageVisitEvent.EVENT_NAME));

        verify(matomoAdapter).reportScreenVisit(eq(userInfo.getInitialVisitorId()), anyString(), eq(screenTitle));
        verify(matomoAdapter, timeout(1000)).track(eq(this.optitrackConfigs.getEventCategoryName()),
                eq(SetPageVisitEvent.EVENT_NAME),
                customDimensionsArguments.capture(),
                eq(initialVisitorId));

        List<CustomDimension> screenEventDimensions = customDimensionsArguments.getAllValues()
                .get(0);

        Assert.assertTrue(customDimensionsAndDecorationCreatedTheRightWay(screenEventDimensions,
                eventConfigsMap.get(SetPageVisitEvent.EVENT_NAME), setPageVisitEvent));
    }
    @Test
    public void eventsShouldBeSentWhenActivityStopped(){
        OptimoveEvent optimoveEvent = mock(OptimoveEvent.class);
        EventConfigs eventConfigs = mock(EventConfigs.class);
        when(optimoveEvent.getName()).thenReturn("some_random_name");
        optitrackManager.reportEvent(optimoveEvent, eventConfigs);
        lifecycleObserver.onActivityStopped(mock(Activity.class));
        verify(matomoAdapter).dispatch();
    }

    private boolean customDimensionsAndDecorationCreatedTheRightWay(List<CustomDimension> customDimensions,
                                                                    EventConfigs eventConfigs,
                                                                    OptimoveEvent optimoveEvent) {

        if (!customDimensions.contains(new CustomDimension(this.optitrackConfigs.getCustomDimensionIds().eventIdCustomDimensionId,
                String.valueOf(eventConfigs.getId())))) {
            return false;
        }
        if (!customDimensions.contains(new CustomDimension(this.optitrackConfigs.getCustomDimensionIds().eventNameCustomDimensionId,
                String.valueOf(optimoveEvent.getName())))) {
            return false;
        }

        for (String parameter : optimoveEvent.getParameters()
                .keySet()) {
            CustomDimension customDimension;
            customDimension = new CustomDimension(eventConfigs.getParameterConfigs()
                    .get(parameter)
                    .getDimensionId(),
                    String.valueOf(optimoveEvent.getParameters()
                            .get(parameter)));
            if (!customDimensions.contains(customDimension)) {
                return false;
            }
        }
        return true;

    }

    @Test
    public void setTimeoutShouldBeCalled() {
        int timeout = 45646;
        optitrackManager.setTimeout(timeout);
        verify(matomoAdapter).setDispatchTimeout(timeout);
    }


}
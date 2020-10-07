package com.optimove.sdk.optimove_sdk.optipush_tests.deep_link_tests;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import com.optimove.sdk.optimove_sdk.optipush.deep_link.DeepLinkHandler;
import com.optimove.sdk.optimove_sdk.optipush.deep_link.LinkDataError;
import com.optimove.sdk.optimove_sdk.optipush.deep_link.LinkDataExtractedListener;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P)
public class DeepLinkHandlerTests {


    @Mock
    LinkDataExtractedListener mockedLinkDataExtractedListener;


    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldReturnNoDeepLinkErrorWhenIntentDataIsNull(){
        Intent mockedIntent = mock(Intent.class);
        when(mockedIntent.getData()).thenReturn(null);

        DeepLinkHandler deepLinkHandler = new DeepLinkHandler(mockedIntent);
        deepLinkHandler.extractLinkData(mockedLinkDataExtractedListener);

        verify(mockedLinkDataExtractedListener).onErrorOccurred(eq(LinkDataError.NO_DEEP_LINK));
    }
    @Test
    public void shouldExtractScreeNameAndDataCorrectly(){
        Intent mockedIntent = mock(Intent.class);
        Uri mockedUri = mock(Uri.class);
        String someScreenName = "some_screen";
        String someParamName = "some_param_name";
        String someParamValue = "some_param_value";
        Set<String> queryParamNames = new HashSet<>();
        queryParamNames.add(someParamName);



        when(mockedIntent.getData()).thenReturn(mockedUri);
        when(mockedUri.getLastPathSegment()).thenReturn("some_screen");
        when(mockedUri.getQueryParameterNames()).thenReturn(queryParamNames);
        when(mockedUri.getQueryParameter(someParamName)).thenReturn(someParamValue);

        DeepLinkHandler deepLinkHandler = new DeepLinkHandler(mockedIntent);
        deepLinkHandler.extractLinkData(mockedLinkDataExtractedListener);

        ArgumentCaptor<Map<String, String>> parameters = ArgumentCaptor.forClass(Map.class);
        verify(mockedLinkDataExtractedListener).onDataExtracted(eq(someScreenName), parameters.capture());

        Map<String, String> values = parameters.getValue();
        Assert.assertEquals(values.size() ,1);
        Assert.assertEquals(values.get(someParamName),someParamValue);
    }

}

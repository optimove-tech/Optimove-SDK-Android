package com.optimove.sdk.optimove_sdk.tools_tests;

import android.util.ArrayMap;

import com.google.gson.annotations.SerializedName;
import com.optimove.sdk.optimove_sdk.main.tools.JsonUtils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Map;


@RunWith(RobolectricTestRunner.class)
//@Config(sdk = Build.VERSION_CODES.P)
public class JsonUtilsTests {

    @Test
    public void nestedObjectShouldBeParsedFromMapCorrectly(){
        String outerProperty = "outer_property";
        String innerObject = "{\"media_type\":\"image\",\"url\":\"https://e-simon" +
                ".com/sites/default/files/post/picture3.jpg\"}";
        Map<String, String> nestedMap = new ArrayMap<>(2);


        nestedMap.put("first_value", outerProperty);
        nestedMap.put("second_value", innerObject);

        OuterHelperClass outerHelperClass = JsonUtils.parseJsonMap(nestedMap, OuterHelperClass.class);

        Assert.assertEquals(outerHelperClass.firstValue, outerProperty);
        Assert.assertEquals(outerHelperClass.secondValue.url, "https://e-simon" +
                                ".com/sites/default/files/post/picture3.jpg");
        Assert.assertEquals(outerHelperClass.secondValue.mediaType, "image");
    }

    private class OuterHelperClass {
        @SerializedName("first_value")
        private String firstValue;
        @SerializedName("second_value")
        private InnerHelperClass secondValue;

        private class InnerHelperClass {
            @SerializedName("media_type")
            private String mediaType;
            private String url;
        }
    }
}

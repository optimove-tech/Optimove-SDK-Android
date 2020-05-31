package com.optimove.sdk.optimove_sdk.main.tools;

import com.google.gson.Gson;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Map;

public class JsonUtils {

    public static <T> T parseJsonMap(Map<String, String> stringsMap, Class<T> classOfT) {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        JsonParser jsonParser = new JsonParser();
        String val;

        for (String key : stringsMap.keySet()) {
            val = stringsMap.get(key);
            try {
                jsonObject.add(key, jsonParser.parse(val));
            } catch (Exception e) {
                jsonObject.addProperty(key, val);
            } finally {
                if (String.valueOf(jsonObject.get(key)).equals("null")){
                    jsonObject.addProperty(key, val);
                }
            }
        }
        try {
            return gson.fromJson(jsonObject, classOfT);
        } catch (Exception e) {
            return null;
        }
    }
}

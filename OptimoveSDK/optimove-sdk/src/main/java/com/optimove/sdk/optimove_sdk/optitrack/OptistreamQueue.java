package com.optimove.sdk.optimove_sdk.optitrack;

import android.content.Context;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.optimove.sdk.optimove_sdk.main.tools.FileUtils;
import com.optimove.sdk.optimove_sdk.main.tools.opti_logger.OptiLoggerStreamsContainer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Note that this implementation is not thread safe.
 */
public class OptistreamQueue {

    private boolean initialized = false;
    private List<OptistreamEvent> optistreamEventList;
    private FileUtils fileUtils;
    private Context context;
    private Gson gson;

    public final static class Constants {
        public static final String EVENTS_FILE_NAME = "optistream_queue";
    }


    public OptistreamQueue(FileUtils fileUtils, Context context) {
        this.fileUtils = fileUtils;
        this.context = context;
        this.gson = new Gson();
    }

    private synchronized void ensureInit() {
        if (!initialized) {
            loadEventsIntoMemory();
            this.initialized = true;
        }
    }

    public void enqueue(List<OptistreamEvent> optistreamEvents) {
        ensureInit();
        optistreamEventList.addAll(optistreamEvents);
        persistEvents(optistreamEventList);
    }
    public void enqueue(OptistreamEvent optistreamEvent) {
        ensureInit();
        optistreamEventList.add(optistreamEvent);
        persistEvents(optistreamEventList);
    }

    public void remove(List<OptistreamEvent> optistreamEvents) {
        ensureInit();
        optistreamEventList.removeAll(optistreamEvents);
        persistEvents(optistreamEventList);
    }
    @Nullable
    public List<OptistreamEvent> first(int limit){
        ensureInit();
        return optistreamEventList.subList(0, Math.min(limit, optistreamEventList.size()));
    }

    public int size(){
        return optistreamEventList.size();
    }

    private void loadEventsIntoMemory() {
        Type listType = new TypeToken<List<OptistreamEvent>>() {
        }.getType();
        try {
            this.optistreamEventList = new Gson().fromJson(fileUtils.readFile(context)
                    .from(FileUtils.SourceDir.INTERNAL)
                    .named(Constants.EVENTS_FILE_NAME)
                    .asString(), listType);
        } catch (JsonSyntaxException e) {
            OptiLoggerStreamsContainer.error("Local event file is corrupted");
        }

        if(optistreamEventList == null) {
            optistreamEventList = new ArrayList<>();
        }
    }

    private void persistEvents(List<OptistreamEvent> optistreamEvents) {
        fileUtils.write(context, gson.toJson(optistreamEvents))
                .to(Constants.EVENTS_FILE_NAME)
                .in(FileUtils.SourceDir.INTERNAL)
                .now();
    }
}

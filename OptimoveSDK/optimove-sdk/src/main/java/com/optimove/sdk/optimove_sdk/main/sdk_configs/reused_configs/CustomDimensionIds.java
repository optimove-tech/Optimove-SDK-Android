package com.optimove.sdk.optimove_sdk.main.sdk_configs.reused_configs;

import com.google.gson.annotations.SerializedName;

public class CustomDimensionIds {

    @SerializedName("event_id_custom_dimension_id")
    public int eventIdCustomDimensionId;
    @SerializedName("event_name_custom_dimension_id")
    public int eventNameCustomDimensionId;
    @SerializedName("visit_custom_dimensions_start_id")
    public int visitCustomDimensionsStartId;
    @SerializedName("max_visit_custom_dimensions")
    public int maxVisitCustomDimensions;
    @SerializedName("action_custom_dimensions_start_id")
    public int actionCustomDimensionsStartId;
    @SerializedName("max_action_custom_dimensions")
    public int maxActionCustomDimensions;

    public int getEventIdCustomDimensionId() {
        return eventIdCustomDimensionId;
    }

    public void setEventIdCustomDimensionId(Integer eventIdCustomDimensionId) {
        this.eventIdCustomDimensionId = eventIdCustomDimensionId;
    }

    public int getEventNameCustomDimensionId() {
        return eventNameCustomDimensionId;
    }

    public void setEventNameCustomDimensionId(int eventNameCustomDimensionId) {
        this.eventNameCustomDimensionId = eventNameCustomDimensionId;
    }

    public int getVisitCustomDimensionsStartId() {
        return visitCustomDimensionsStartId;
    }

    public void setVisitCustomDimensionsStartId(int visitCustomDimensionsStartId) {
        this.visitCustomDimensionsStartId = visitCustomDimensionsStartId;
    }

    public int getMaxVisitCustomDimensions() {
        return maxVisitCustomDimensions;
    }

    public void setMaxVisitCustomDimensions(int maxVisitCustomDimensions) {
        this.maxVisitCustomDimensions = maxVisitCustomDimensions;
    }

    public int getActionCustomDimensionsStartId() {
        return actionCustomDimensionsStartId;
    }

    public void setActionCustomDimensionsStartId(int actionCustomDimensionsStartId) {
        this.actionCustomDimensionsStartId = actionCustomDimensionsStartId;
    }

    public int getMaxActionCustomDimensions() {
        return maxActionCustomDimensions;
    }

    public void setMaxActionCustomDimensions(int maxActionCustomDimensions) {
        this.maxActionCustomDimensions = maxActionCustomDimensions;
    }
}
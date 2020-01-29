package com.optimove.sdk.optimove_sdk.optipush.campaigns;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class TriggeredCampaign implements Parcelable {

    public static final Creator<TriggeredCampaign> CREATOR = new Creator<TriggeredCampaign>() {
        @Override
        public TriggeredCampaign createFromParcel(Parcel in) {
            return new TriggeredCampaign(in);
        }

        @Override
        public TriggeredCampaign[] newArray(int size) {
            return new TriggeredCampaign[size];
        }
    };

    @SerializedName("action_serial")
    private int actionSerial;
    @SerializedName("action_id")
    private int actionId;
    @SerializedName("template_id")
    private int templateId;

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(actionId);
        dest.writeInt(templateId);
        dest.writeInt(actionSerial);
    }

    protected TriggeredCampaign(Parcel in) {
        actionId = in.readInt();
        templateId = in.readInt();
        actionSerial = in.readInt();
    }
    @Override
    public int describeContents() {
        return 0;
    }

    public int getActionSerial() {
        return actionSerial;
    }

    public void setActionSerial(int actionSerial) {
        this.actionSerial = actionSerial;
    }

    public int getActionId() {
        return actionId;
    }

    public void setActionId(int actionId) {
        this.actionId = actionId;
    }

    public int getTemplateId() {
        return templateId;
    }

    public void setTemplateId(int templateId) {
        this.templateId = templateId;
    }

    @NonNull
    @Override
    public String toString() {
        return "TriggeredCampaign{ " + "templateId=" + templateId +
                ", actionSerial=" + actionSerial +
                ", actionId=" + actionId +
                '}';
    }
}

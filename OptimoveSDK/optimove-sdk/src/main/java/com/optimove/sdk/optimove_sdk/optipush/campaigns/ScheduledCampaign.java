package com.optimove.sdk.optimove_sdk.optipush.campaigns;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class ScheduledCampaign implements Parcelable {

    public static final Creator<ScheduledCampaign> CREATOR = new Creator<ScheduledCampaign>() {
        @Override
        public ScheduledCampaign createFromParcel(Parcel in) {
            return new ScheduledCampaign(in);
        }

        @Override
        public ScheduledCampaign[] newArray(int size) {
            return new ScheduledCampaign[size];
        }
    };
    @SerializedName("action_serial")
    private int actionSerial;
    @SerializedName("campaign_id")
    private int campaignId;
    @SerializedName("template_id")
    private int templateId;
    @SerializedName("engagement_id")
    private int engagementId;
    @SerializedName("campaign_type")
    private int campaignType;

    protected ScheduledCampaign(Parcel in) {
        campaignId = in.readInt();
        templateId = in.readInt();
        actionSerial = in.readInt();
        engagementId = in.readInt();
        campaignType = in.readInt();
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(campaignId);
        dest.writeInt(templateId);
        dest.writeInt(actionSerial);
        dest.writeInt(engagementId);
        dest.writeInt(campaignType);
    }

    public int getActionSerial() {
        return actionSerial;
    }

    public void setActionSerial(int actionSerial) {
        this.actionSerial = actionSerial;
    }

    public int getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(int campaignId) {
        this.campaignId = campaignId;
    }

    public int getTemplateId() {
        return templateId;
    }

    public void setTemplateId(int templateId) {
        this.templateId = templateId;
    }

    public int getEngagementId() {
        return engagementId;
    }

    public void setEngagementId(int engagementId) {
        this.engagementId = engagementId;
    }

    public int getCampaignType() {
        return campaignType;
    }

    public void setCampaignType(int campaignType) {
        this.campaignType = campaignType;
    }

    @NonNull
    @Override
    public String toString() {
        return "ScheduledCampaign{ " + "campaignId=" + campaignId +
                ", templateId=" + templateId +
                ", actionSerial=" + actionSerial +
                ", engagementId=" + engagementId +
                ", campaignType=" + campaignType +
                '}';
    }
}

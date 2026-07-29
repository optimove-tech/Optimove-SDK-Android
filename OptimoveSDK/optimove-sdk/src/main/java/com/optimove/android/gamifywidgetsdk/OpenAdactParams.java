package com.optimove.android.gamifywidgetsdk;

import androidx.annotation.Nullable;

/**
 * Parameters for opening an Adact embedded campaign.
 *
 * <p>Maps to the Web SDK {@code openAdactCampaign} contract:
 * {@code {adactUrl}/embedded/{campaignId}?cid=&customerIdToken=}
 */
public class OpenAdactParams {

    @Nullable public Integer campaignId;
    @Nullable public String cid;
    @Nullable public String token;

    public OpenAdactParams() {
    }

    public OpenAdactParams(@Nullable Integer campaignId,
                           @Nullable String cid,
                           @Nullable String token) {
        this.campaignId = campaignId;
        this.cid = cid;
        this.token = token;
    }
}

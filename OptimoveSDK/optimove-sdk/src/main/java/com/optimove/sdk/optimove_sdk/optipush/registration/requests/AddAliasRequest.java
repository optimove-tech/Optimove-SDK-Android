package com.optimove.sdk.optimove_sdk.optipush.registration.requests;

import com.google.gson.annotations.SerializedName;

import java.util.Set;

public class AddAliasRequest {

    @SerializedName("new_aliases")
    private Set<String> newUserAliases;

    public AddAliasRequest(Set<String> newUserAliases) {
        this.newUserAliases = newUserAliases;
    }
}

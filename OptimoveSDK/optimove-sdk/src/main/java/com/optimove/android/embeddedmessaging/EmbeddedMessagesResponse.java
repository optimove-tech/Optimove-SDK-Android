package com.optimove.android.embeddedmessaging;


import java.util.Map;

public class EmbeddedMessagesResponse {
    private Map<String, Container> containersMap;

    // Constructor
    public EmbeddedMessagesResponse(Map<String, Container> containersMap) {
        this.containersMap = containersMap;
    }

    // Getter and setter
    public Map<String, Container> getContainersMap() {
        return containersMap;
    }

    public void setContainersMap(Map<String, Container> containersMap) {
        this.containersMap = containersMap;
    }
}
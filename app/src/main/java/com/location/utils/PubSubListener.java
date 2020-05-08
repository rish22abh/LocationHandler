package com.location.utils;

public interface PubSubListener {
    void onPubSubDataReceive(int reqType, Object data);
}

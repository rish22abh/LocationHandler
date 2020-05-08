package com.location.location_manager;

import android.location.Location;

public interface LocationCallBack {
    void onSuccess(int reqType, Location location);

    void onFailure(int reqType, String errorMessage);
}

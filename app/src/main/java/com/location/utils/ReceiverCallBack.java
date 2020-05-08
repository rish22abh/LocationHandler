package com.location.utils;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public class ReceiverCallBack extends ResultReceiver {
    public ReceiverCallBack(Handler handler) {
        super(handler);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        super.onReceiveResult(resultCode, resultData);
        Location location = resultData.getParcelable("location");
        //Perform background task for location here

    }
}

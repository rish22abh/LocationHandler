package com.location.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.location.permission_manager.PermissionUtils;

public class PubSubForPermission extends Handler {
    private static PubSubForPermission mInstance;
    private PubSubListener mListener;

    private PubSubForPermission(Looper mainLooper) {
        super(mainLooper);
    }

    public static PubSubForPermission getInstance() {
        if (mInstance == null)
            mInstance = new PubSubForPermission(Looper.getMainLooper());
        return mInstance;
    }

    public void publish(int requestCode, PermissionUtils.PermissionObject permissionObject) {
        Message message = Message.obtain(this, requestCode, permissionObject);
        sendMessage(message);
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);
        if (mListener != null)
            mListener.onPubSubDataReceive(msg.what, msg.obj);
    }

    public void addListener(PubSubListener listener) {
        mListener = listener;
    }

    public void removeListener() {
        mListener = null;
        mInstance = null;
    }
}

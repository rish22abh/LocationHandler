package com.location.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.ResultReceiver;

import androidx.annotation.Nullable;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.location.utils.EnumClass;

public class BackgroundLocationService extends Service {
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback locationCallback;

    public BackgroundLocationService() {
        super();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }
        final boolean isStop = intent.getBooleanExtra("isStop", false);
        if (isStop && mFusedLocationClient!=null && locationCallback!=null){
            mFusedLocationClient.removeLocationUpdates(locationCallback);
            stopSelf();
            return START_NOT_STICKY;
        }
        if (mFusedLocationClient == null && locationCallback == null) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
            LocationRequest mLocationRequest = new LocationRequest();
            long mIntervalTime = intent.getLongExtra("intervalTime", 5000);
            long mFasterIntervalTime = intent.getLongExtra("fasterIntervalTime", 5000);
            int mLocationPriority = intent.getIntExtra("locationPriority", EnumClass.LocationPriority.PRIORITY_BALANCED_POWER_ACCURACY);
            mLocationRequest.setInterval(mIntervalTime);
            mLocationRequest.setFastestInterval(mFasterIntervalTime);
            mLocationRequest.setPriority(mLocationPriority);


            ResultReceiver receiver = null;
            final Bundle bundle = new Bundle();
            if (intent.hasExtra("receiver")) {
                receiver = intent.getParcelableExtra("receiver");
            } else {
                stopSelf();
                return START_NOT_STICKY;
            }

            final ResultReceiver finalReceiver = receiver;
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        return;
                    }
                    if (locationResult.getLocations().size() > 0 && finalReceiver != null) {
                        bundle.putParcelable("location", locationResult.getLocations().get(0));
                        finalReceiver.send(0, bundle);
                    }
                }
            };
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    locationCallback,
                    Looper.getMainLooper());
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFusedLocationClient != null && locationCallback != null)
            mFusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

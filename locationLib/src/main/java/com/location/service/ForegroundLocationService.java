package com.location.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.ResultReceiver;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.location.utils.EnumClass;
import com.location.R;

public class ForegroundLocationService extends Service {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback locationCallback;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null)
            return START_NOT_STICKY;
        if (intent.getAction() != null && intent.getAction().equals("stop")) {
            final boolean isStop = intent.getBooleanExtra("isStop", false);
            if (isStop) {
                stopForeground(true);
                stopSelf();
                if (mFusedLocationClient != null && locationCallback != null)
                    mFusedLocationClient.removeLocationUpdates(locationCallback);
            }
            return START_NOT_STICKY;
        }
        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentText("Fetching Current Location")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();
        startForeground(1, notification);
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
        return START_NOT_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final PackageManager pm = getApplicationContext().getPackageManager();
            ApplicationInfo ai;
            try {
                ai = pm.getApplicationInfo( this.getPackageName(), 0);
            } catch (final PackageManager.NameNotFoundException e) {
                ai = null;
            }
            final String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : "Background Task");

            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    applicationName,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }
}

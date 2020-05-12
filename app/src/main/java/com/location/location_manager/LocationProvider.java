package com.location.location_manager;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.location.service.BackgroundLocationService;
import com.location.utils.EnumClass;
import com.location.service.ForegroundLocationService;
import com.location.utils.PubSubForPermission;
import com.location.utils.PubSubListener;
import com.location.utils.ReceiverCallBack;
import com.location.permission_manager.PermissionUtils;

public class LocationProvider implements PubSubListener {

    private Intent mbackgroundService;
    private final String TAG = LocationProvider.class.getSimpleName();
    private int mIntervalTime = 5000;
    private int mFasterIntervalTime = 5000;
    private int mLocationPriority = EnumClass.LocationPriority.PRIORITY_BALANCED_POWER_ACCURACY;
    private Activity mActivity;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private int mTaskEnableConst;
    private LocationCallBack mLocationCallBack;

    public LocationProvider(@NonNull Activity activity) {
        this.mActivity = activity;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mActivity);
        mLocationRequest = LocationRequest.create();
        PubSubForPermission.getInstance().addListener(this);
    }

    @Override
    public void onPubSubDataReceive(int reqType, Object data) {
        if (reqType == EnumClass.PermissionConst.PERMISSIONS_LAST_LOCATION && data instanceof PermissionUtils.PermissionObject) {
            PermissionUtils.PermissionObject permissionObject = (PermissionUtils.PermissionObject) data;
            boolean isAllPermissionGranted = true;
            for (int i = 0; i < permissionObject.getGrantResults().length; i++) {
                if (permissionObject.getGrantResults()[i] == PackageManager.PERMISSION_DENIED) {
                    isAllPermissionGranted = false;
                    break;
                }
            }
            if (isAllPermissionGranted)
                performPendingTask();
        }
    }

    public void setIntervalTime(int intervalTime) {
        this.mIntervalTime = intervalTime;
    }

    public void setFasterIntervalTime(int fasterIntervalTime) {
        this.mFasterIntervalTime = fasterIntervalTime;
    }

    public void setLocationPriority(@EnumClass.LocationPriority int locationPriority) {
        this.mLocationPriority = locationPriority;
    }

    public void setLocationCallBack(LocationCallBack mLocationCallBack) {
        this.mLocationCallBack = mLocationCallBack;
    }

    public void getLastLocation() {
        if (isAnyParamNull()) return;
        mTaskEnableConst = EnumClass.TaskDialogEnableConst.DIALOG_LAST_LOCATION;
        getLocation();
    }

    public void getLocationContinues() {
        if (isAnyParamNull()) return;
        mTaskEnableConst = EnumClass.TaskDialogEnableConst.DIALOG_CONTINUE_LOCATION;
        getLocation();
    }

    public void getLocationContinuesInBackground() {
        if (isAnyParamNull()) return;
        mTaskEnableConst = EnumClass.TaskDialogEnableConst.DIALOG_CONTINUE_LOCATION_BACKGROUND;
        getLocation();
    }

    private void getLocation() {
        if (PermissionUtils.isLocationPermissionGranted(mActivity)) {
            PermissionUtils.requestForLocationPermission(mActivity, EnumClass.PermissionConst.PERMISSIONS_LAST_LOCATION);
            return;
        }
        enableLocationSetting();
    }

    private boolean isAnyParamNull() {
        if (mActivity == null) return true;
        if (mFusedLocationClient == null) return true;
        return mLocationRequest == null;
    }


    private void enableLocationSetting() {
        if (isAnyParamNull()) return;
        setLocationRequest();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .setAlwaysShow(true).addLocationRequest(mLocationRequest);
        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(mActivity).checkLocationSettings(builder.build());
        result.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(mActivity, EnumClass.ActivityResultConst.DIALOG_ENABLE_LOCATION);
                    } catch (IntentSender.SendIntentException sendEx) {
                        Log.v(TAG, "" + sendEx.getMessage());
                    }
                }
            }
        }).addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                //if last location cache is null
                requestLocationUpdate();
            }
        });
    }

    private void requestLocationUpdate() {
        if (mTaskEnableConst == EnumClass.TaskDialogEnableConst.DIALOG_CONTINUE_LOCATION_BACKGROUND) {
            startService(false);
            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        if (locationResult == null) {
                            return;
                        }
                        switch (mTaskEnableConst) {
                            case EnumClass.TaskDialogEnableConst.DIALOG_LAST_LOCATION:
                                if (locationResult.getLastLocation() != null) {
                                    Log.v(TAG, "Location retrieved");
                                    mLocationCallBack.onSuccess(EnumClass.CallbackRequestType.LAST_LOCATION, locationResult.getLastLocation());
                                    mFusedLocationClient.removeLocationUpdates(this);
                                } else {
                                    Log.v(TAG, "Location is null.");
                                }
                                break;
                            case EnumClass.TaskDialogEnableConst.DIALOG_CONTINUE_LOCATION:
                                if (locationResult.getLocations().size() > 0) {
                                    Log.v(TAG, "Location retrieved");
                                    mLocationCallBack.onSuccess(EnumClass.CallbackRequestType.CONTINUE_LOCATION, locationResult.getLocations().get(0));
                                }
                                break;
                        }
                    }

                    @Override
                    public void onLocationAvailability(LocationAvailability locationAvailability) {
                        super.onLocationAvailability(locationAvailability);
                        if (!locationAvailability.isLocationAvailable()) {
                            Log.v(TAG, "Location not available.");
                            mFusedLocationClient.removeLocationUpdates(this);
                            mLocationCallBack.onFailure(mTaskEnableConst,"Location not available");
                        }
                    }
                }, Looper.getMainLooper());
    }

    private void startService(boolean isStop) {
        ReceiverCallBack receiverCallBack = new ReceiverCallBack(new Handler());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mbackgroundService = new Intent(mActivity, ForegroundLocationService.class);
            mbackgroundService.putExtra("receiver", receiverCallBack);
            if (isStop) {
                mbackgroundService.putExtra("isStop", isStop);
                mbackgroundService.setAction("stop");
            }
            mActivity.startForegroundService(mbackgroundService);
            if (isStop) mbackgroundService = null;
        } else {
            mbackgroundService = new Intent(mActivity, BackgroundLocationService.class);
            mbackgroundService.putExtra("receiver", receiverCallBack);
            if (isStop && mbackgroundService != null) {
                mbackgroundService.putExtra("isStop", isStop);
                mActivity.stopService(mbackgroundService);
            } else {
                mActivity.startService(mbackgroundService);
            }
        }
    }

    private void setLocationRequest() {
        if (mLocationRequest == null) return;
        if (mIntervalTime != 0) mLocationRequest.setInterval(mIntervalTime);
        if (mFasterIntervalTime != 0) mLocationRequest.setFastestInterval(mFasterIntervalTime);
        mLocationRequest.setPriority(mLocationPriority);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EnumClass.ActivityResultConst.DIALOG_ENABLE_LOCATION && resultCode == Activity.RESULT_OK) {
            performPendingTask();
        }
    }

    private void performPendingTask() {
        switch (mTaskEnableConst) {
            case EnumClass.TaskDialogEnableConst.DIALOG_LAST_LOCATION:
                getLastLocation();
                break;
            case EnumClass.TaskDialogEnableConst.DIALOG_CONTINUE_LOCATION:
                getLocationContinues();
                break;
        }
    }

    //Very mandatory to avoid memory leak
    public void removeCallback() {
        mActivity = null;
        mFusedLocationClient = null;
        mLocationRequest = null;
        PubSubForPermission.getInstance().removeListener();
    }

    public void stopLocationFromBackground() {
        if (mbackgroundService != null) {
            startService(true);
        }
    }
}

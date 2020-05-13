package com.location.permission_manager;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.location.utils.PubSubForPermission;

public class PermissionUtils {
    public static void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        PermissionObject permissionObject = new PermissionObject();
        permissionObject.setGrantResults(grantResults);
        permissionObject.setPermissions(permissions);
        PubSubForPermission.getInstance().publish(requestCode, permissionObject);
    }

    public static boolean isLocationPermissionGranted(Activity mActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED;
        }else
            return ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;
    }

    public static void requestForLocationPermission(Activity mActivity, int permissionsReqType) {
        if (PermissionUtils.isLocationPermissionGranted(mActivity)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ActivityCompat.requestPermissions(mActivity,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                        permissionsReqType);
            }else{
                ActivityCompat.requestPermissions(mActivity,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                        permissionsReqType);
            }
        }
    }

    public static class PermissionObject {
        String[] permissions;
        int[] grantResults;

        public String[] getPermissions() {
            return permissions;
        }

        public void setPermissions(String[] permissions) {
            this.permissions = permissions;
        }

        public int[] getGrantResults() {
            return grantResults;
        }

        public void setGrantResults(int[] grantResults) {
            this.grantResults = grantResults;
        }
    }

}
